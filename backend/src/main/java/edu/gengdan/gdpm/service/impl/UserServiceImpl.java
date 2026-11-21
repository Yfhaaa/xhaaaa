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

/** UserService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class UserServiceImpl extends DomainSupport<UserMapper, User> implements UserService {

    /** 查询系统用户及角色信息。 */
    public Object users() {
        admin();
        return reports.users();
    }

    /** 查询当前身份数据范围内的学生。 */
    public List<Map<String, Object>> students() {
        return reports.students(auth.scope());
    }

    /** 维护用户、角色及令牌失效状态。 */
    @OperLog("维护用户")
    @Transactional
    public Object user(Long userId, Map<String, Object> body) {
        admin();
        long roleId = Params.number(body, "roleId"),
            majorId = Params.number(body, "majorId"),
            classId = Params.number(body, "classId");
        require(roleId >= 1 && roleId <= 3, "请选择系统内置的三种角色");
        required(majors.selectById(majorId));
        require(
            classes.selectCount(
                query(SchoolClass.class).eq(SchoolClass::getId, classId).eq(SchoolClass::getMajorId, majorId)
            ) > 0,
            "班级与专业不匹配"
        );
        String cohort = Params.text(body, "cohort", 20);
        required(cohorts.selectOne(query(Cohort.class).eq(Cohort::getName, cohort)));
        String name = Params.text(body, "name", 80),
            password = str(body, "password");
        int enabled = body.get("enabled") == null ? 1 : Integer.parseInt(body.get("enabled").toString());
        require(enabled == 0 || enabled == 1, "账号状态不合法");
        User user = userId == null ? new User() : required(getById(userId));
        if (userId == null) {
            user.setUsername(Params.text(body, "username", 60));
            require(user.getUsername().matches("[A-Za-z0-9_]{3,60}"), "用户名请使用3至60位字母数字下划线");
            require(!password.isEmpty(), "新用户必须设置密码");
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
                password.length() >= 6 && password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72,
                "密码需6至72字节"
            );
            user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password));
        }
        user.setName(name);
        user.setMajorId(majorId);
        user.setClassId(classId);
        user.setCohort(cohort);
        user.setEnabled(enabled);
        saveOrUpdate(user);
        userRoles.delete(query(SysUserRole.class).eq(SysUserRole::getUserId, user.getId()));
        userRoles.insert(entity(SysUserRole.class, "user_id", user.getId(), "role_id", roleId));
        if (userId != null) {
            sessions.delete(query(SysSession.class).eq(SysSession::getUserId, userId));
        }
        return Map.of("id", user.getId());
    }
}
