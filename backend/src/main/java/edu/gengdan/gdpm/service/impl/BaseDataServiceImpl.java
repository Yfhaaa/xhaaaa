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

/** BaseDataService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class BaseDataServiceImpl extends DomainSupport<MajorMapper, Major> implements BaseDataService {

    /** 查询页面所需基础下拉数据。 */
    public Object reference() {
        auth.uid();
        List<Map<String, Object>> classViews = new ArrayList<>();
        for (SchoolClass row : classes.selectList(query(SchoolClass.class).orderByAsc(SchoolClass::getId))) {
            classViews.add(EntityViews.view(row));
        }
        return fields(
            "majors",
            lambdaQuery().orderByAsc(Major::getId).list(),
            "classes",
            classViews,
            "cohorts",
            cohorts.selectList(query(Cohort.class).orderByAsc(Cohort::getId)),
            "teachers",
            auth.role("ADMIN") ? reports.teachers() : List.of()
        );
    }

    /** 校验基础数据类型白名单。 */
    private void type(String type) {
        require(List.of("majors", "classes", "cohorts").contains(type), "基础数据类型不存在");
    }

    /** 维护专业、班级或届次。 */
    @OperLog("维护基础数据")
    @Transactional
    public Object base(String type, Long baseId, Map<String, Object> body) {
        admin();
        type(type);
        String name = Params.text(body, "name", type.equals("cohorts") ? 20 : 100);
        if (type.equals("classes")) {
            long majorId = Params.number(body, "majorId");
            required(majors.selectById(majorId));
            SchoolClass row = baseId == null ? new SchoolClass() : required(classes.selectById(baseId));
            if (baseId != null) {
                require(
                    users.selectCount(query(User.class).eq(User::getClassId, baseId).ne(User::getMajorId, majorId)) ==
                        0,
                    "班级已有用户，不能变更所属专业"
                );
            }
            row.setName(name);
            row.setMajorId(majorId);
            if (baseId == null) {
                classes.insert(row);
            } else {
                classes.updateById(row);
            }
            baseId = row.getId();
        } else if (type.equals("cohorts")) {
            Cohort row = baseId == null ? new Cohort() : required(cohorts.selectById(baseId));
            if (baseId != null) {
                users.update(
                    null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User>()
                        .eq(User::getCohort, row.getName())
                        .set(User::getCohort, name)
                );
            }
            row.setName(name);
            if (baseId == null) {
                cohorts.insert(row);
            } else {
                cohorts.updateById(row);
            }
            baseId = row.getId();
        } else {
            Major row = baseId == null ? new Major() : required(getById(baseId));
            row.setName(name);
            saveOrUpdate(row);
            baseId = row.getId();
        }
        return Map.of("id", baseId);
    }

    /** 校验引用后删除基础数据。 */
    @OperLog("删除基础数据")
    @Transactional
    public Object removeBase(String type, long baseId) {
        admin();
        type(type);
        if (type.equals("cohorts")) {
            Cohort row = required(cohorts.selectById(baseId));
            require(users.selectCount(query(User.class).eq(User::getCohort, row.getName())) == 0, "届次已有用户引用");
            cohorts.deleteById(baseId);
        } else if (type.equals("classes")) {
            classes.deleteById(baseId);
        } else {
            removeById(baseId);
        }
        return Map.of("message", "已删除");
    }
}
