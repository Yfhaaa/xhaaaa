package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;
import static edu.gengdan.gdpm.common.Values.*;

import edu.gengdan.gdpm.common.*;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.mapper.*;
import edu.gengdan.gdpm.service.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** DefenseService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class DefenseServiceImpl extends DomainSupport<DefenseInfoMapper, DefenseInfo> implements DefenseService {

    @Autowired
    private MaterialService materials;

    /** 查询数据范围内的答辩组。 */
    public Object groups() {
        auth.permission("defense:read");
        return reports.groups(auth.scope());
    }

    /** 创建答辩组并验证教师身份。 */
    @OperLog("创建答辩组")
    @Transactional
    public Object group(Map<String, Object> body) {
        auth.roles("ADMIN");
        auth.permission("defense:write");
        Object raw = body.get("teacherIds");
        require(raw instanceof List && !((List<?>) raw).isEmpty(), "请选择答辩组教师");
        Set<Long> ids = new LinkedHashSet<>();
        for (Object value : (List<?>) raw) {
            long teacherId = id(value);
            User teacher = required(users.selectById(teacherId));
            require(
                teacher.getEnabled() == 1 && reports.roles(teacherId).contains("TEACHER"),
                "答辩组成员必须为有效教师"
            );
            ids.add(teacherId);
        }
        String joined = ids.stream().map(Object::toString).collect(java.util.stream.Collectors.joining(","));
        DefenseGroup group = entity(
            DefenseGroup.class,
            "name",
            Params.text(body, "name", 100),
            "teacher_ids",
            joined,
            "location",
            Params.text(body, "location", 100),
            "defense_at",
            Params.time(body, "defenseAt").toLocalDateTime()
        );
        defenseGroups.insert(group);
        return Map.of("id", group.getId());
    }

    /** 查询数据范围内的答辩安排。 */
    public Object defenses() {
        auth.permission("defense:read");
        return reports.defenses(auth.scope());
    }

    /** 校验定稿后安排学生答辩。 */
    @OperLog("安排答辩")
    @Transactional
    public Object arrange(Map<String, Object> body) {
        auth.roles("ADMIN");
        auth.permission("defense:write");
        long studentId = Params.number(body, "studentId"),
            groupId = Params.number(body, "groupId");
        lockStudent(studentId);
        required(defenseGroups.selectById(groupId));
        require(materials.passed(studentId, "final"), "定稿审核通过后才能安排答辩");
        require(
            scores.selectCount(query(ScoreInfo.class).eq(ScoreInfo::getStudentId, studentId)) == 0,
            "已录入成绩，不能调整答辩"
        );
        DefenseInfo defense = lambdaQuery().eq(DefenseInfo::getStudentId, studentId).one();
        if (defense == null) {
            defense = entity(
                DefenseInfo.class,
                "student_id",
                studentId,
                "status",
                BusinessStatus.NOT_SUBMITTED.label()
            );
        }
        defense.setGroupId(groupId);
        saveOrUpdate(defense);
        return Map.of("id", defense.getId());
    }
}
