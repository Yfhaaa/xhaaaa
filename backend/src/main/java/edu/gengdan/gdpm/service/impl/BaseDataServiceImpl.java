package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.BaseDataSaveRequest;
import edu.gengdan.gdpm.entity.Cohort;
import edu.gengdan.gdpm.entity.Major;
import edu.gengdan.gdpm.entity.SchoolClass;
import edu.gengdan.gdpm.entity.User;
import edu.gengdan.gdpm.mapper.MajorMapper;
import edu.gengdan.gdpm.service.BaseDataService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.ReferenceVO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 已登录用户获取专业、班级和届次，教师列表仅管理员可见。 管理员维护专业、班级和届次，班级专业变更须与已有用户一致，届次改名同步用户。 管理员删除未被引用的基础数据，引用冲突由持久化约束拦截。 */
@Service
public class BaseDataServiceImpl extends DomainSupport<MajorMapper, Major> implements BaseDataService {

    /** 已登录用户获取专业、班级和届次，教师列表仅管理员可见。 */
    public ReferenceVO reference() {
        auth.uid();
        List<Map<String, Object>> classViews = new ArrayList<>();
        for (SchoolClass row : classes.selectList(query(SchoolClass.class).orderByAsc(SchoolClass::getId))) {
            classViews.add(EntityViews.view(row));
        }
        return new ReferenceVO(
            lambdaQuery().orderByAsc(Major::getId).list(),
            classViews,
            cohorts.selectList(query(Cohort.class).orderByAsc(Cohort::getId)),
            auth.role("ADMIN") ? reports.teachers() : List.of()
        );
    }

    /** 校验基础数据类型白名单。 */
    private void type(String type) {
        require(List.of("majors", "classes", "cohorts").contains(type), "基础数据类型不存在");
    }

    /** 管理员维护专业、班级和届次，班级专业变更须与已有用户一致，届次改名同步用户。 */
    @OperLog("维护基础数据")
    @Transactional
    public IdVO base(String type, Long baseId, BaseDataSaveRequest body) {
        validate(body);
        admin();
        type(type);
        String name = body.getName();
        if (type.equals("cohorts")) {
            validate(body, BaseDataSaveRequest.Cohort.class);
        }
        if (type.equals("classes")) {
            validate(body, BaseDataSaveRequest.SchoolClass.class);
            long majorId = body.getMajorId();
            required(majors.selectById(majorId));
            SchoolClass row = baseId == null ? new SchoolClass() : required(classes.selectById(baseId));
            if (baseId != null) {
                require(
                    users.selectCount(
                        query(User.class).eq(User::getClassId, baseId).ne(User::getMajorId, majorId)
                    ) == 0,
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
                    new LambdaUpdateWrapper<User>()
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
        return new IdVO(baseId);
    }

    /** 管理员删除未被引用的基础数据，引用冲突由持久化约束拦截。 */
    @OperLog("删除基础数据")
    @Transactional
    public MessageVO removeBase(String type, long baseId) {
        admin();
        type(type);
        if (type.equals("cohorts")) {
            Cohort row = required(cohorts.selectById(baseId));
            require(
                users.selectCount(query(User.class).eq(User::getCohort, row.getName())) == 0,
                "届次已有用户引用"
            );
            cohorts.deleteById(baseId);
        } else if (type.equals("classes")) {
            classes.deleteById(baseId);
        } else {
            removeById(baseId);
        }
        return new MessageVO("已删除");
    }
}
