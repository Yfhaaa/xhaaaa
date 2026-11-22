package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.DefenseArrangeRequest;
import edu.gengdan.gdpm.dto.DefenseGroupRequest;
import edu.gengdan.gdpm.entity.DefenseGroup;
import edu.gengdan.gdpm.entity.DefenseInfo;
import edu.gengdan.gdpm.entity.ScoreInfo;
import edu.gengdan.gdpm.entity.ThesisReview;
import edu.gengdan.gdpm.entity.User;
import edu.gengdan.gdpm.mapper.DefenseInfoMapper;
import edu.gengdan.gdpm.service.DefenseService;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.vo.IdVO;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 持 defense:read 权限查询角色范围内答辩分组。 管理员创建由有效教师组成的答辩组，记录时间与地点。 持 defense:read 权限读取本人或指导范围内答辩安排。
 * 管理员仅为定稿通过且评阅同意的学生安排答辩，录分后禁止调整。
 */
@Service
public class DefenseServiceImpl
    extends DomainSupport<DefenseInfoMapper, DefenseInfo>
    implements DefenseService
{

    @Autowired
    private MaterialService materials;

    /** 持 defense:read 权限查询角色范围内答辩分组。 */
    public List<Map<String, Object>> groups() {
        auth.permission("defense:read");
        return reports.groups(auth.scope());
    }

    /** 管理员创建由有效教师组成的答辩组，记录时间与地点。 */
    @OperLog("创建答辩组")
    @Transactional
    public IdVO group(DefenseGroupRequest body) {
        validate(body);
        auth.roles("ADMIN");
        auth.permission("defense:write");
        List<Long> teacherIds = body.getTeacherIds();
        Set<Long> ids = new LinkedHashSet<>();
        for (long teacherId : teacherIds) {
            User teacher = required(users.selectById(teacherId));
            require(
                teacher.getEnabled() == 1 && reports.roles(teacherId).contains("TEACHER"),
                "答辩组成员必须为有效教师"
            );
            ids.add(teacherId);
        }
        String joined = ids.stream().map(Object::toString).collect(Collectors.joining(","));
        DefenseGroup group = new DefenseGroup();
        group.setName(body.getName());
        group.setTeacherIds(joined);
        group.setLocation(body.getLocation());
        group.setDefenseAt(LocalDateTime.parse(body.getDefenseAt().replace(" ", "T")));
        defenseGroups.insert(group);
        return new IdVO(group.getId());
    }

    /** 持 defense:read 权限读取本人或指导范围内答辩安排。 */
    public List<Map<String, Object>> defenses() {
        auth.permission("defense:read");
        return reports.defenses(auth.scope());
    }

    /** 管理员仅为定稿通过且评阅同意的学生安排答辩，录分后禁止调整。 */
    @OperLog("安排答辩")
    @Transactional
    public IdVO arrange(DefenseArrangeRequest body) {
        validate(body);
        auth.roles("ADMIN");
        auth.permission("defense:write");
        long studentId = body.getStudentId(),
            groupId = body.getGroupId();
        lockStudent(studentId);
        required(defenseGroups.selectById(groupId));
        require(materials.passed(studentId, "final"), "定稿审核通过后才能安排答辩");
        require(
            thesisReviews.selectCount(
                query(ThesisReview.class)
                    .eq(ThesisReview::getStudentId, studentId)
                    .eq(ThesisReview::getStatus, BusinessStatus.REVIEWED.label())
                    .eq(ThesisReview::getAgreeDefense, 1)
            ) == 1,
            "论文评阅须同意答辩后才能安排"
        );
        require(
            scores.selectCount(query(ScoreInfo.class).eq(ScoreInfo::getStudentId, studentId)) == 0,
            "已录入成绩，不能调整答辩"
        );
        DefenseInfo defense = lambdaQuery().eq(DefenseInfo::getStudentId, studentId).one();
        if (defense == null) {
            defense = new DefenseInfo();
            defense.setStudentId(studentId);
            defense.setStatus(BusinessStatus.NOT_SUBMITTED.label());
        }
        defense.setGroupId(groupId);
        saveOrUpdate(defense);
        messages.send(studentId, "答辩安排已更新", "请查看答辩时间、地点与分组。", "/defense", null);
        return new IdVO(defense.getId());
    }
}
