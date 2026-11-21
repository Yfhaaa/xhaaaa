package edu.gengdan.gdpm;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.gengdan.gdpm.auth.BearerToken;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.mapper.*;
import edu.gengdan.gdpm.service.TaskBookService;
import java.util.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** 原有数据库契约迁移到 MyBatis-Plus 后仍须全部成立。 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:contracts;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "app.upload-dir=../run/unit-uploads",
    }
)
@Transactional
@AutoConfigureMockMvc
@Import(DatabaseContractTest.SecurityFixtures.class)
class DatabaseContractTest {

    @Autowired
    SysLogMapper logs;

    @Autowired
    StudentAssignmentMapper assignments;

    @Autowired
    UserMapper users;

    /** 验证自动主键与默认时间戳能够同时返回。 */
    @Test
    void generatedIdWorksAlongsideDefaultTimestamp() {
        SysLog log = new SysLog();
        log.setUserId(1L);
        log.setAction("数据库契约测试");
        log.setDetail("带默认时间的主键返回");
        logs.insert(log);
        assertTrue(log.getId() > 0);
        assertNotNull(logs.selectById(log.getId()).getCreatedAt());
    }

    /** 验证学生只能存在一条确定选题关系。 */
    @Test
    void confirmedStudentHasUniqueAssignment() {
        StudentAssignment assignment = new StudentAssignment();
        assignment.setStudentId(5L);
        assignment.setTopicId(3L);
        assignment.setTeacherId(2L);
        assertThrows(DataIntegrityViolationException.class, () -> assignments.insert(assignment));
    }

    /** 验证 BCrypt 密码与实体字段映射。 */
    @Test
    void demoPasswordIsBcryptAndMapperMapsColumns() {
        User user = users.selectById(4L);
        assertEquals("student1", user.getUsername());
        assertEquals(1L, user.getMajorId());
        assertNotEquals("123456", user.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("123456", user.getPassword()));
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    org.apache.shiro.mgt.SecurityManager securityManager;

    @Autowired
    GuardProbe probe;

    @Autowired
    SysSessionMapper sessions;

    @Autowired
    SysRolePermissionMapper rolePermissions;

    @Autowired
    TaskBookService taskService;

    /** 不含业务守卫的探针，证明 Shiro Spring 注解拦截实际执行。 */
    public static class GuardProbe {

        /** 仅通过角色注解保护探针方法。 */
        @RequiresRoles("ADMIN")
        public String roleProtected() {
            return "允许";
        }

        /** 仅通过权限注解保护探针方法。 */
        @RequiresPermissions("system:manage")
        public String permissionProtected() {
            return "允许";
        }
    }

    @TestConfiguration
    static class SecurityFixtures {

        /** 创建无手写业务守卫的鉴权探针。 */
        @Bean
        GuardProbe guardProbe() {
            return new GuardProbe();
        }
    }

    /** 清理测试线程绑定的身份。 */
    @AfterEach
    void clearSubject() {
        ThreadContext.remove();
    }

    /** 验证账号后签发限时随机令牌。 */
    private String login(String username) throws Exception {
        String response = mvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(Map.of("username", username, "password", "123456")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.user.password").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return json.readTree(response).path("data").path("token").asText();
    }

    /** 为服务层鉴权测试绑定真实令牌身份。 */
    private void bind(String token) {
        Subject subject = new Subject.Builder(securityManager).sessionCreationEnabled(false).buildSubject();
        subject.login(new BearerToken(token));
        ThreadContext.bind(subject);
    }

    /** 验证 Shiro 注解能独立执行角色和权限拦截。 */
    @Test
    void shiroAnnotationsRejectRoleAndPermissionWithoutManualGuards() throws Exception {
        bind(login("student1"));
        assertThrows(AuthorizationException.class, () -> probe.roleProtected());
        assertThrows(AuthorizationException.class, () -> probe.permissionProtected());
        ThreadContext.remove();
        bind(login("admin"));
        assertEquals("允许", probe.roleProtected());
        assertEquals("允许", probe.permissionProtected());
    }

    /** 验证撤销权限立即影响既有令牌。 */
    @Test
    void existingTokenImmediatelyObservesPermissionRevocation() throws Exception {
        String token = login("teacher1");
        mvc.perform(get("/api/topics").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        rolePermissions.delete(
            new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, 2L)
                .eq(SysRolePermission::getPermissionId, 2L)
        );
        mvc.perform(get("/api/topics").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403));
    }

    /** 验证分页和伪造请求参数不能扩大教师范围。 */
    @Test
    void scopeCannotBeExpandedByPagingOrForgedStudentParameters() throws Exception {
        String token = login("teacher1");
        mvc.perform(
            get("/api/students?page=1&size=1&studentId=6&teacherId=3").header("Authorization", "Bearer " + token)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(2))
            .andExpect(jsonPath("$.data.records.length()").value(1))
            .andExpect(jsonPath("$.data.records[0].student_id").value(5));
        mvc.perform(get("/api/students?page=0&size=1").header("Authorization", "Bearer " + token)).andExpect(
            status().isBadRequest()
        );
        mvc.perform(get("/api/students?page=1&size=201").header("Authorization", "Bearer " + token)).andExpect(
            status().isBadRequest()
        );
        mvc.perform(get("/api/students?page=x&size=20").header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
        mvc.perform(get("/api/students?page=2147483647&size=200").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(2))
            .andExpect(jsonPath("$.data.records.length()").value(0));
    }

    /** 验证成功操作仅记录一次且失败操作不记成功日志。 */
    @Test
    void annotatedMutationWritesOneLogAndFailedMutationWritesNone() throws Exception {
        long before = logs.selectCount(new LambdaQueryWrapper<SysLog>().eq(SysLog::getAction, "发布通知"));
        String admin = login("admin");
        mvc.perform(
            post("/api/notices")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"AOP验收\",\"content\":\"操作日志验收\"}")
        ).andExpect(status().isOk());
        List<SysLog> recorded = logs.selectList(new LambdaQueryWrapper<SysLog>().eq(SysLog::getAction, "发布通知"));
        assertEquals(before + 1, recorded.size());
        assertTrue(recorded.stream().allMatch(row -> row.getUserId() == 1L));
        assertTrue(recorded.stream().noneMatch(row -> row.getDetail().contains("123456")));
        String student = login("student1");
        mvc.perform(
            post("/api/notices")
                .header("Authorization", "Bearer " + student)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"越权\",\"content\":\"越权\"}")
        ).andExpect(status().isForbidden());
        assertEquals(before + 1, logs.selectCount(new LambdaQueryWrapper<SysLog>().eq(SysLog::getAction, "发布通知")));
    }

    /** 验证绕过控制器时服务层仍拒绝跨教师数据。 */
    @Test
    void serviceRejectsCrossTeacherAccessWithoutController() throws Exception {
        bind(login("teacher2"));
        ApiException error = assertThrows(ApiException.class, () ->
            taskService.submit("task", Map.of("studentId", 5L, "title", "越权任务书", "fileId", 2L))
        );
        assertEquals(403, error.status);
    }

    /** 验证删除会话后原令牌无法认证。 */
    @Test
    void deletedSessionCannotAuthenticateAgain() throws Exception {
        String token = login("student1");
        sessions.delete(new LambdaQueryWrapper<SysSession>().eq(SysSession::getUserId, 4L));
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401));
    }

    /** 验证 XML 聚合与范围内逐学生九节点进度一致。 */
    @Test
    void xmlStatisticsMatchScopedNineStepProgress() throws Exception {
        for (String who : List.of("admin", "teacher1", "student4")) {
            String token = login(who);
            for (String by : List.of("major", "class", "teacher")) {
                String response = mvc
                    .perform(
                        get("/api/statistics")
                            .param("groupBy", by)
                            .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
                JsonNode data = json.readTree(response).path("data");
                for (JsonNode group : data.path("groups")) {
                    String name = group.path("name").asText();
                    int count = 0;
                    double progress = 0;
                    for (JsonNode row : data.path("students")) {
                        if (name.equals(row.path(by + "_name").asText("未分配"))) {
                            count++;
                            progress += row.path("progress").asDouble();
                        }
                    }
                    assertEquals(count, group.path("student_count").asInt());
                    assertEquals(Math.round(progress / count), group.path("average_progress").asLong());
                }
            }
        }
    }
}
