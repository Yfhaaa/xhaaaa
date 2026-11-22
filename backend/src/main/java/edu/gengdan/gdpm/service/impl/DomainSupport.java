package edu.gengdan.gdpm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.gengdan.gdpm.auth.Auth;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.entity.User;
import edu.gengdan.gdpm.mapper.ArchiveRecordMapper;
import edu.gengdan.gdpm.mapper.CohortMapper;
import edu.gengdan.gdpm.mapper.DefenseGroupMapper;
import edu.gengdan.gdpm.mapper.DefenseInfoMapper;
import edu.gengdan.gdpm.mapper.FileRecordMapper;
import edu.gengdan.gdpm.mapper.GuidanceRecordMapper;
import edu.gengdan.gdpm.mapper.MajorMapper;
import edu.gengdan.gdpm.mapper.MidtermCheckMapper;
import edu.gengdan.gdpm.mapper.NoticeMapper;
import edu.gengdan.gdpm.mapper.PlagiarismCheckMapper;
import edu.gengdan.gdpm.mapper.ProposalReportMapper;
import edu.gengdan.gdpm.mapper.ReportMapper;
import edu.gengdan.gdpm.mapper.ReviewRecordMapper;
import edu.gengdan.gdpm.mapper.SchoolClassMapper;
import edu.gengdan.gdpm.mapper.ScoreInfoMapper;
import edu.gengdan.gdpm.mapper.StudentAssignmentMapper;
import edu.gengdan.gdpm.mapper.SysPermissionMapper;
import edu.gengdan.gdpm.mapper.SysRoleMapper;
import edu.gengdan.gdpm.mapper.SysRolePermissionMapper;
import edu.gengdan.gdpm.mapper.SysSessionMapper;
import edu.gengdan.gdpm.mapper.SysUserRoleMapper;
import edu.gengdan.gdpm.mapper.TaskBookMapper;
import edu.gengdan.gdpm.mapper.ThesisInfoMapper;
import edu.gengdan.gdpm.mapper.ThesisReviewMapper;
import edu.gengdan.gdpm.mapper.TopicChangeMapper;
import edu.gengdan.gdpm.mapper.TopicInfoMapper;
import edu.gengdan.gdpm.mapper.TopicSelectionMapper;
import edu.gengdan.gdpm.mapper.UserMapper;
import edu.gengdan.gdpm.mapper.WorkflowStepMapper;
import edu.gengdan.gdpm.service.MessageService;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;

/** 服务共用的数据访问依赖、记录存在性检查与学生行锁，权限由具体业务校验。 */
public abstract class DomainSupport<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    @Autowired
    private Validator validator;

    /** HTTP 与直接服务调用共享字段约束；业务权限和状态仍由具体服务检查。 */
    protected void validate(Object request, Class<?>... groups) {
        Set<Class<?>> validationGroups = new HashSet<>(Arrays.asList(groups));
        validationGroups.add(Default.class);
        Set<ConstraintViolation<Object>> errors = validator.validate(
            request, validationGroups.toArray(new Class<?>[0])
        );
        if (!errors.isEmpty()) {
            String message = errors.stream()
                .sorted(Comparator.comparing(value -> value.getPropertyPath().toString()))
                .map(ConstraintViolation::getMessage)
                .findFirst().orElse("请求参数格式不正确");
            throw new ApiException(400, message);
        }
    }

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

    @Autowired
    protected TopicChangeMapper changes;

    @Autowired
    protected ThesisReviewMapper thesisReviews;

    @Autowired
    protected PlagiarismCheckMapper checks;

    @Autowired
    protected MessageService messages;

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

}
