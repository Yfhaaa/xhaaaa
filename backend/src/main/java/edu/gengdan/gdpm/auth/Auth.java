package edu.gengdan.gdpm.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.dto.DataScope;
import edu.gengdan.gdpm.entity.StudentAssignment;
import edu.gengdan.gdpm.entity.WorkflowStep;
import edu.gengdan.gdpm.mapper.ReportMapper;
import edu.gengdan.gdpm.mapper.StudentAssignmentMapper;
import edu.gengdan.gdpm.mapper.UserMapper;
import edu.gengdan.gdpm.mapper.WorkflowStepMapper;
import edu.gengdan.gdpm.vo.UserVO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 当前令牌身份与服务层的数据范围校验。 */
@Component
public class Auth {

    @Autowired
    private StudentAssignmentMapper assignments;

    @Autowired
    private UserMapper users;

    @Autowired
    private WorkflowStepMapper steps;

    @Autowired
    private ReportMapper reports;

    /** 取得当前已认证用户编号。 */
    public long uid() {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            throw new ApiException(401, "请先登录");
        }
        return ((Number) subject.getPrincipal()).longValue();
    }

    /** 维护角色权限关联。 */
    public boolean role(String code) {
        return SecurityUtils.getSubject().hasRole(code);
    }

    /** 查询角色及其权限列表。 */
    public void roles(String... codes) {
        uid();
        for (String code : codes) {
            if (role(code)) {
                return;
            }
        }
        throw new ApiException(403, "当前角色无操作权限");
    }

    /** 维护权限名称与编码。 */
    public void permission(String permission) {
        uid();
        if (!SecurityUtils.getSubject().isPermitted(permission)) {
            throw new ApiException(403, "缺少操作权限：" + permission);
        }
    }

    /** 生成服务端可信的数据范围。 */
    public DataScope scope() {
        return new DataScope(role("ADMIN"), role("STUDENT"), uid());
    }

    /** 学生限本人，教师限其指导关系；控制器参数不能扩展此范围。 */
    public void student(long studentId) {
        long userId = uid();
        if (role("ADMIN") || (role("STUDENT") && studentId == userId)) {
            return;
        }
        if (
            role("TEACHER") &&
            assignments.selectCount(
                new LambdaQueryWrapper<StudentAssignment>()
                    .eq(StudentAssignment::getStudentId, studentId)
                    .eq(StudentAssignment::getTeacherId, userId)
            ) == 1
        ) {
            return;
        }
        throw new ApiException(403, "不能访问其他学生或其他教师指导的数据");
    }

    /** 已登录用户读取本人脱敏资料、角色与实时权限。 */
    public UserVO me() {
        return new UserVO(users.selectById(uid()), reports.roles(uid()), reports.permissions(uid()));
    }

    /** 检查业务节点截止时间。 */
    public void deadline(String stage) {
        WorkflowStep step = steps.selectOne(
            new LambdaQueryWrapper<WorkflowStep>().eq(WorkflowStep::getCode, stage)
        );
        if (step == null) {
            throw new ApiException(400, "流程节点不存在");
        }
        ApiException.require(
            step.getDeadline().isAfter(LocalDateTime.now()),
            "已超过" + stage + "节点截止时间，请联系管理员"
        );
    }

    /** 计算令牌的单向摘要。 */
    public static String hash(String token) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(
                token.getBytes(StandardCharsets.UTF_8)
            );
            StringBuilder result = new StringBuilder();
            for (byte value : bytes) {
                result.append(String.format("%02x", value & 255));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
