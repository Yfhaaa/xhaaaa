package edu.gengdan.gdpm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.gengdan.gdpm.auth.Auth;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.mapper.*;
import java.util.*;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;

/** 模块服务共用的实体构造与存在性检查，不执行 SQL。 */
public abstract class DomainSupport<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    @Autowired
    protected Auth auth;

    @Autowired
    protected UserMapper users;

    @Autowired
    protected SysRoleMapper roles;

    @Autowired
    protected SysPermissionMapper permissions;

    @Autowired
    protected SysUserRoleMapper userRoles;

    @Autowired
    protected SysRolePermissionMapper rolePermissions;

    @Autowired
    protected SysSessionMapper sessions;

    @Autowired
    protected MajorMapper majors;

    @Autowired
    protected SchoolClassMapper classes;

    @Autowired
    protected CohortMapper cohorts;

    @Autowired
    protected WorkflowStepMapper workflowSteps;

    @Autowired
    protected TopicInfoMapper topics;

    @Autowired
    protected TopicSelectionMapper selections;

    @Autowired
    protected StudentAssignmentMapper assignments;

    @Autowired
    protected TaskBookMapper tasks;

    @Autowired
    protected ProposalReportMapper proposals;

    @Autowired
    protected MidtermCheckMapper midterms;

    @Autowired
    protected ThesisInfoMapper theses;

    @Autowired
    protected FileRecordMapper files;

    @Autowired
    protected ReviewRecordMapper reviews;

    @Autowired
    protected GuidanceRecordMapper guidanceRecords;

    @Autowired
    protected DefenseGroupMapper defenseGroups;

    @Autowired
    protected DefenseInfoMapper defenses;

    @Autowired
    protected ScoreInfoMapper scores;

    @Autowired
    protected ArchiveRecordMapper archives;

    @Autowired
    protected NoticeMapper notices;

    @Autowired
    protected ReportMapper reports;

    /** 创建指定实体的 Lambda 查询条件。 */
    protected <E> LambdaQueryWrapper<E> query(Class<E> type) {
        return new LambdaQueryWrapper<>(type);
    }

    /** 将记录不存在转换为业务异常。 */
    protected <E> E required(E value) {
        if (value == null) {
            throw new ApiException(404, "记录不存在");
        }
        return value;
    }

    /** 与确认选题、提交材料和评分一致的学生行锁。 */
    protected User lockStudent(long studentId) {
        return required(users.selectOne(query(User.class).eq(User::getId, studentId).last("FOR UPDATE")));
    }

    /** 校验管理员角色和系统管理权限。 */
    protected void admin() {
        auth.roles("ADMIN");
        auth.permission("system:manage");
    }

    /** 使用实体 setter 创建对象，保留数据库默认字段。 */
    protected <E> E entity(Class<E> type, Object... pairs) {
        try {
            E result = type.getDeclaredConstructor().newInstance();
            BeanWrapperImpl bean = new BeanWrapperImpl(result);
            for (int i = 0; i < pairs.length; i += 2) {
                String[] words = pairs[i].toString().split("_");
                StringBuilder key = new StringBuilder(words[0]);
                for (int j = 1; j < words.length; j++) {
                    key.append(Character.toUpperCase(words[j].charAt(0))).append(words[j].substring(1));
                }
                bean.setPropertyValue(key.toString(), pairs[i + 1]);
            }
            return result;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
