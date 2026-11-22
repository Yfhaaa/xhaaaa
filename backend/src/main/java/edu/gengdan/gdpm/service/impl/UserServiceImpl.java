package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.UserSaveRequest;
import edu.gengdan.gdpm.entity.Cohort;
import edu.gengdan.gdpm.entity.SchoolClass;
import edu.gengdan.gdpm.entity.StudentAssignment;
import edu.gengdan.gdpm.entity.SysSession;
import edu.gengdan.gdpm.entity.SysUserRole;
import edu.gengdan.gdpm.entity.TopicInfo;
import edu.gengdan.gdpm.entity.User;
import edu.gengdan.gdpm.mapper.UserMapper;
import edu.gengdan.gdpm.service.UserService;
import edu.gengdan.gdpm.vo.IdVO;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 管理员读取系统用户和角色信息，不返回密码。 学生仅查询本人，教师仅查询指导学生，管理员查询全部学生。 管理员维护用户与角色；禁止破坏自身管理员身份，停用或修改使既有令牌失效。 */
@Service
public class UserServiceImpl extends DomainSupport<UserMapper, User> implements UserService {

    /** 管理员读取系统用户和角色信息，不返回密码。 */
    public List<Map<String, Object>> users() {
        admin();
        return reports.users();
    }

    /** 学生仅查询本人，教师仅查询指导学生，管理员查询全部学生。 */
    public List<Map<String, Object>> students() {
        return reports.students(auth.scope());
    }

    /** 管理员维护用户与角色；禁止破坏自身管理员身份，停用或修改使既有令牌失效。 */
    @OperLog("维护用户")
    @Transactional
    public IdVO user(Long userId, UserSaveRequest body) {
        validate(body);
        admin();
        long roleId = body.getRoleId(),
            majorId = body.getMajorId(),
            classId = body.getClassId();
        required(majors.selectById(majorId));
        require(
            classes.selectCount(
                query(SchoolClass.class).eq(SchoolClass::getId, classId).eq(SchoolClass::getMajorId, majorId)
            ) > 0,
            "班级与专业不匹配"
        );
        String cohort = body.getCohort();
        required(cohorts.selectOne(query(Cohort.class).eq(Cohort::getName, cohort)));
        String name = body.getName(),
            password = body.getPassword();
        int enabled = body.getEnabled() == null ? 1 : body.getEnabled();
        User user = userId == null ? new User() : required(getById(userId));
        if (userId == null) {
            validate(body, UserSaveRequest.Create.class);
            user.setUsername(body.getUsername());
        } else {
            if (userId == auth.uid()) {
                require(roleId == 1 && enabled == 1, "不能停用自己或修改自己的管理员身份");
            }
            SysUserRole previous = required(
                userRoles.selectOne(query(SysUserRole.class).eq(SysUserRole::getUserId, userId))
            );
            if (previous.getRoleId() != roleId) {
                require(
                    assignments.selectCount(
                        query(StudentAssignment.class)
                            .eq(StudentAssignment::getStudentId, userId)
                            .or()
                            .eq(StudentAssignment::getTeacherId, userId)
                    ) == 0 &&
                        topics.selectCount(query(TopicInfo.class).eq(TopicInfo::getTeacherId, userId)) == 0,
                    "用户已有课题业务，不能变更角色"
                );
            }
        }
        if (!password.isEmpty()) {
            require(
                password.length() >= 6 &&
                    password.getBytes(StandardCharsets.UTF_8).length <= 72,
                "密码需6至72字节"
            );
            user.setPassword(
                new BCryptPasswordEncoder().encode(password)
            );
        }
        user.setName(name);
        user.setMajorId(majorId);
        user.setClassId(classId);
        user.setCohort(cohort);
        user.setEnabled(enabled);
        saveOrUpdate(user);
        userRoles.delete(query(SysUserRole.class).eq(SysUserRole::getUserId, user.getId()));
        SysUserRole record6 = new SysUserRole();
        record6.setUserId(user.getId());
        record6.setRoleId(roleId);
        userRoles.insert(record6);
        if (userId != null) {
            sessions.delete(query(SysSession.class).eq(SysSession::getUserId, userId));
        }
        return new IdVO(user.getId());
    }
}
