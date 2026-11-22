package edu.gengdan.gdpm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.gengdan.gdpm.config.UpgradeMigration;
import edu.gengdan.gdpm.entity.ArchiveRecord;
import edu.gengdan.gdpm.entity.DefenseInfo;
import edu.gengdan.gdpm.entity.FileRecord;
import edu.gengdan.gdpm.entity.PlagiarismCheck;
import edu.gengdan.gdpm.entity.ScoreInfo;
import edu.gengdan.gdpm.entity.ThesisInfo;
import edu.gengdan.gdpm.entity.ThesisReview;
import edu.gengdan.gdpm.entity.TopicInfo;
import edu.gengdan.gdpm.entity.WorkflowStep;
import edu.gengdan.gdpm.mapper.ArchiveRecordMapper;
import edu.gengdan.gdpm.mapper.DefenseInfoMapper;
import edu.gengdan.gdpm.mapper.FileRecordMapper;
import edu.gengdan.gdpm.mapper.PlagiarismCheckMapper;
import edu.gengdan.gdpm.mapper.ScoreInfoMapper;
import edu.gengdan.gdpm.mapper.StudentAssignmentMapper;
import edu.gengdan.gdpm.mapper.SysMessageMapper;
import edu.gengdan.gdpm.mapper.ThesisInfoMapper;
import edu.gengdan.gdpm.mapper.ThesisReviewMapper;
import edu.gengdan.gdpm.mapper.TopicChangeMapper;
import edu.gengdan.gdpm.mapper.TopicInfoMapper;
import edu.gengdan.gdpm.mapper.WorkflowStepMapper;
import edu.gengdan.gdpm.service.MessageService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/** 第三轮业务约束的真实 Spring Boot、Shiro、数据库集成验收。 */
@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:advanced;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "app.upload-dir=../run/advanced-uploads",
    }
)
@AutoConfigureMockMvc
@Transactional
class AdvancedContractTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    TopicInfoMapper topics;

    @Autowired
    TopicChangeMapper changes;

    @Autowired
    StudentAssignmentMapper assignments;

    @Autowired
    ThesisReviewMapper thesisReviews;

    @Autowired
    ThesisInfoMapper theses;

    @Autowired
    PlagiarismCheckMapper checks;

    @Autowired
    FileRecordMapper files;

    @Autowired
    WorkflowStepMapper steps;

    @Autowired
    ScoreInfoMapper scores;

    @Autowired
    DefenseInfoMapper defenses;

    @Autowired
    ArchiveRecordMapper archives;

    @Autowired
    MessageService messages;

    @Autowired
    SysMessageMapper messageMapper;

    private final Map<String, String> tokens = new HashMap<>();

    /** DTO 字段约束失败必须是 400、统一响应和精确中文提示。 */
    private void invalid(String path, String who, Object body, String message) throws Exception {
        String response = mvc.perform(
            post("/api" + path)
                .header("Authorization", "Bearer " + token(who))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(body))
        ).andExpect(status().isBadRequest()).andReturn().getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        JsonNode value = json.readTree(response);
        assertEquals(400, value.path("code").asInt());
        assertEquals(message, value.path("message").asText());
        assertTrue(value.path("data").isNull());
    }

    /** 覆盖必填、长度、正数、上限和请求文本去空白后的校验。 */
    @Test
    void requestDtosRejectInvalidFieldsWithChineseMessages() throws Exception {
        Map<String, Object> valid = Map.of("title", "课题", "description", "说明", "majorId", 1, "capacity", 1);
        for (Map.Entry<String, Object> entry : Map.<String, Object>of(
            "title", "  ", "description", "x".repeat(10001), "majorId", 0, "capacity", 21
        ).entrySet()) {
            Map<String, Object> body = new HashMap<>(valid);
            body.put(entry.getKey(), entry.getValue());
            String message = entry.getKey().equals("title") ? "title不能为空且长度不能超过200"
                : entry.getKey().equals("description") ? "description不能为空且长度不能超过10000"
                : entry.getKey().equals("majorId") ? "majorId必须为正整数" : "每个课题最多20人";
            invalid("/topics", "teacher1", body, message);
        }
        invalid("/defense/groups", "admin", Map.of(
            "name", "测试", "location", "教室", "defenseAt", "2027-06-01T09:00:00", "teacherIds", List.of()
        ), "请选择答辩组教师");
        invalid("/archives", "admin", Map.of("studentId", 0), "studentId必须为正整数");
        invalid("/archives", "admin", Map.of("studentId", 7.9), "请求参数格式不正确");
    }

    /** 禁止把字符串、数字、集合或空值强制转换成审核布尔值。 */
    @Test
    void reviewDtoKeepsStrictBooleanContract() throws Exception {
        for (Object value : List.of("true", "false", 1, 0, List.of(true))) {
            invalid("/topics/1/review", "admin", Map.of("approved", value, "comment", "通过"),
                "approved必须为布尔值");
        }
        invalid("/topics/1/review", "admin", Map.of("comment", "通过"), "approved必须为布尔值");
    }

    /** 兼容更新时省略账号和密码；创建仍要求账号和密码。 */
    @Test
    void userDtoSeparatesCreationAndUpdateConstraints() throws Exception {
        Map<String, Object> body = Map.of(
            "name", "修改姓名", "roleId", 3, "majorId", 1, "classId", 1, "cohort", "2026", "enabled", 1
        );
        JsonNode result = send("PUT", "/system/users/4", "admin", body, 200);
        assertEquals(4, result.path("id").asInt());
        invalid("/system/users", "admin", body, "新用户必须设置密码");
    }

    /** 分数范围、权重范围及兼容输入的评阅分精度仍须受约束。 */
    @Test
    void scoreDtoRejectsRangeAndPrecisionBeforeWriting() throws Exception {
        Map<String, Object> valid = Map.of(
            "studentId", 7, "guidanceScore", 90, "reviewScore", 86, "defenseScore", 95,
            "guidanceWeight", 0.3, "reviewWeight", 0.3, "defenseWeight", 0.4
        );
        for (Map.Entry<String, Object> entry : Map.<String, Object>of(
            "guidanceScore", 101, "reviewScore", 80.123, "defenseScore", -1, "guidanceWeight", 1.1
        ).entrySet()) {
            Map<String, Object> body = new HashMap<>(valid);
            body.put(entry.getKey(), entry.getValue());
            invalid("/scores", "teacher1", body,
                entry.getKey() + (entry.getKey().equals("reviewScore") ? "最多保留两位小数" : "超出范围"));
        }
    }

    /** 评阅缺失时保留明确中文提示，不抛空指针，也不读取客户端评阅分。 */
    @Test
    void scoreWithoutCompletedReviewHasExplicitBusinessError() throws Exception {
        archives.delete(new LambdaQueryWrapper<ArchiveRecord>().eq(ArchiveRecord::getStudentId, 7L));
        scores.delete(new LambdaQueryWrapper<ScoreInfo>().eq(ScoreInfo::getStudentId, 7L));
        thesisReviews.delete(new LambdaQueryWrapper<ThesisReview>().eq(ThesisReview::getStudentId, 7L));
        invalid("/scores", "teacher1", Map.of(
            "studentId", 7, "guidanceScore", 90, "reviewScore", 100, "defenseScore", 95,
            "guidanceWeight", 0.3, "reviewWeight", 0.3, "defenseWeight", 0.4
        ), "该学生论文评阅尚未完成或评阅未同意答辩，不能录入成绩");
    }

    /** 获取真实登录令牌，按测试方法隔离。 */
    private String token(String who) throws Exception {
        if (!tokens.containsKey(who)) {
            String response = mvc
                .perform(
                    post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("username", who, "password", "123456")))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
            tokens.put(who, json.readTree(response).path("data").path("token").asText());
        }
        return tokens.get(who);
    }

    /** 发送真实 JSON 请求并校验状态码。 */
    private JsonNode send(
        String method,
        String path,
        String who,
        Object body,
        int expected
    ) throws Exception {
        MockHttpServletRequestBuilder request = method.equals(
            "GET"
        )
            ? get("/api" + path)
            : method.equals("PUT")
              ? put("/api" + path)
              : post("/api" + path);
        request.header("Authorization", "Bearer " + token(who));
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(body));
        }
        String response = mvc
            .perform(request)
            .andExpect(status().is(expected))
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        return json.readTree(response).path("data");
    }

    /** 创建独立的可变更目标，跨教师但同专业。 */
    private long target() {
        TopicInfo topic = new TopicInfo();
        topic.setTitle("变更集成测试");
        topic.setDescription("测试");
        topic.setMajorId(1L);
        topic.setTeacherId(3L);
        topic.setCapacity(1);
        topic.setStatus("已通过");
        topics.insert(topic);
        return topic.getId();
    }

    /** 证明顺序审批、留痕、分配和名额原子更新。 */
    @Test
    void topicChangeRequiresThreeOrderedApprovals() throws Exception {
        long target = target();
        long id = send(
            "POST",
            "/topic-changes",
            "student2",
            Map.of("newTopicId", target, "reason", "研究调整"),
            200
        )
            .path("id")
            .asLong();
        send("POST", "/topic-changes", "student2", Map.of("newTopicId", target, "reason", "重复"), 400);
        Map<String, Object> approval = Map.of("approved", true, "comment", "同意");
        send("POST", "/topic-changes/" + id + "/approve", "admin", approval, 403);
        assertEquals(1L, assignments.selectById(5L).getTopicId());
        for (String who : List.of("teacher1", "teacher2", "admin")) {
            send("POST", "/topic-changes/" + id + "/approve", who, approval, 200);
        }
        assertEquals(target, assignments.selectById(5L).getTopicId());
        assertEquals(3L, assignments.selectById(5L).getTeacherId());
        JsonNode dossier = send("GET", "/students/5/dossier", "teacher2", null, 200);
        assertEquals("已通过", dossier.path("timeline").get(0).path("status").asText());
        assertEquals(1, dossier.path("guidance").size());
        send("GET", "/students/5/dossier", "teacher1", null, 403);
        JsonNode all = send("GET", "/topic-changes", "admin", null, 200);
        assertEquals(3, all.get(0).path("history").size());
        send("POST", "/topic-changes/" + id + "/approve", "admin", approval, 400);
    }

    /** 拒绝任何一级后不改变原分配，历史仍可读取。 */
    @Test
    void rejectedTopicChangePreservesAssignment() throws Exception {
        long id = send(
            "POST",
            "/topic-changes",
            "student2",
            Map.of("newTopicId", target(), "reason", "调整"),
            200
        )
            .path("id")
            .asLong();
        send(
            "POST",
            "/topic-changes/" + id + "/approve",
            "teacher1",
            Map.of("approved", false, "comment", "研究方向不匹配"),
            200
        );
        assertEquals(1L, assignments.selectById(5L).getTopicId());
        assertEquals("已退回", changes.selectById(id).getStatus());
    }

    /** 评阅不得自评、跳过或被其他教师填写，不同意则禁止答辩。 */
    @Test
    void independentReviewControlsDefenseAdmission() throws Exception {
        thesisReviews.delete(new LambdaQueryWrapper<ThesisReview>().eq(ThesisReview::getStudentId, 7L));
        scores.delete(new LambdaQueryWrapper<ScoreInfo>().eq(ScoreInfo::getStudentId, 7L));
        defenses.delete(new LambdaQueryWrapper<DefenseInfo>().eq(DefenseInfo::getStudentId, 7L));
        send("POST", "/thesis-reviews/assign", "admin", Map.of("studentId", 7, "reviewerId", 2), 400);
        send("POST", "/thesis-reviews/assign", "admin", Map.of("studentIds", List.of(7)), 200);
        JsonNode row = send("GET", "/thesis-reviews", "teacher2", null, 200).get(0);
        long id = row.path("id").asLong();
        send(
            "POST",
            "/thesis-reviews/" + id + "/review",
            "teacher1",
            Map.of("score", 80, "approved", true, "comment", "越权"),
            403
        );
        send(
            "POST",
            "/thesis-reviews/" + id + "/review",
            "teacher2",
            Map.of("score", 55, "approved", false, "comment", "需补充实验"),
            200
        );
        send("POST", "/defenses", "admin", Map.of("studentId", 7, "groupId", 1), 400);
    }

    /** 评分忽略客户端伪造评阅分，采用论文评阅中的86分。 */
    @Test
    void scoreUsesCompletedIndependentReview() throws Exception {
        archives.delete(new LambdaQueryWrapper<ArchiveRecord>().eq(ArchiveRecord::getStudentId, 7L));
        scores.delete(new LambdaQueryWrapper<ScoreInfo>().eq(ScoreInfo::getStudentId, 7L));
        JsonNode result = send(
            "POST",
            "/scores",
            "teacher1",
            Map.of(
                "studentId",
                7,
                "guidanceScore",
                90,
                "reviewScore",
                1,
                "defenseScore",
                92,
                "guidanceWeight",
                0.3,
                "reviewWeight",
                0.3,
                "defenseWeight",
                0.4
            ),
            200
        );
        assertEquals(89.6, result.path("totalScore").asDouble(), 0.001);
    }

    /** 查重绑定定稿版本，阈值修改即时生效且其他教师不得登记。 */
    @Test
    void plagiarismVersionAndConfigurableThresholdGateFinalApproval() throws Exception {
        ThesisInfo thesis = theses.selectOne(
            new LambdaQueryWrapper<ThesisInfo>()
                .eq(ThesisInfo::getStudentId, 7L)
                .eq(ThesisInfo::getStage, "final")
        );
        thesis.setStatus("待审核");
        theses.updateById(thesis);
        checks.delete(new LambdaQueryWrapper<PlagiarismCheck>().eq(PlagiarismCheck::getStudentId, 7L));
        Map<String, Object> approval = Map.of("approved", true, "comment", "通过");
        String path = "/materials/final/" + thesis.getId() + "/review";
        send("POST", path, "teacher1", approval, 400);
        FileRecord report = new FileRecord();
        report.setOwnerId(2L);
        report.setOriginalName("查重.txt");
        report.setStoredName("check.txt");
        report.setSizeBytes(10L);
        files.insert(report);
        Map<String, Object> body = Map.of(
            "studentId",
            7,
            "platform",
            "知网测试",
            "rate",
            31,
            "fileId",
            report.getId()
        );
        send("POST", "/plagiarism-checks", "teacher2", body, 403);
        send("POST", "/plagiarism-checks", "teacher1", body, 200);
        send("POST", path, "teacher1", approval, 400);
        send(
            "PUT",
            "/workflow/7",
            "admin",
            Map.of("deadline", "2027-07-28T23:59:59", "plagiarismThreshold", 35),
            200
        );
        send("POST", path, "teacher1", approval, 200);
    }

    /** 定时提醒仅针对未完成节点，重复执行不重复发送。 */
    @Test
    void scheduledMessagesArePrivateAndIdempotent() throws Exception {
        WorkflowStep step = steps.selectById(2L);
        step.setDeadline(LocalDate.now().plusDays(3).atTime(23, 59));
        steps.updateById(step);
        messages.reminders();
        long count = messageMapper.selectCount(null);
        messages.reminders();
        assertEquals(count, messageMapper.selectCount(null));
        JsonNode unread = send("GET", "/messages?unread=true", "student1", null, 200);
        assertTrue(unread.size() > 0);
        long id = unread.get(0).path("id").asLong();
        send("POST", "/messages/" + id + "/read", "student2", null, 403);
        send("POST", "/messages/read-all", "student1", null, 200);
        assertEquals(0, send("GET", "/messages?unread=true", "student1", null, 200).size());
    }

    /** 个人资料字段白名单、旧密码、强度和令牌撤销都生效。 */
    @Test
    void profileAndPasswordAreValidatedAndRevokeSessions() throws Exception {
        send("PUT", "/profile", "student1", Map.of("name", "刘冉", "email", "bad", "phone", ""), 400);
        send(
            "PUT",
            "/profile",
            "student1",
            Map.of(
                "name",
                "刘冉",
                "email",
                "liuran@example.edu",
                "phone",
                "13800000000",
                "enabled",
                0,
                "roleId",
                1
            ),
            200
        );
        JsonNode me = send("GET", "/auth/me", "student1", null, 200);
        assertEquals("liuran@example.edu", me.path("email").asText());
        assertFalse(me.has("password"));
        assertEquals("STUDENT", me.path("roles").get(0).asText());
        send(
            "POST",
            "/profile/password",
            "student1",
            Map.of("oldPassword", "bad", "newPassword", "Changed1234"),
            400
        );
        send(
            "POST",
            "/profile/password",
            "student1",
            Map.of("oldPassword", "123456", "newPassword", "12345678"),
            400
        );
        send(
            "POST",
            "/profile/password",
            "student1",
            Map.of("oldPassword", "123456", "newPassword", "Changed1234"),
            200
        );
        send("GET", "/auth/me", "student1", null, 401);
    }

    /** 档案含九节点并严格隔离；大屏管理员专用且提供30天趋势。 */
    @Test
    void dossierAndBoardRespectScope() throws Exception {
        assertEquals(9, send("GET", "/students/7/dossier", "student4", null, 200).path("timeline").size());
        send("GET", "/students/7/dossier", "student2", null, 403);
        send("GET", "/students/7/dossier", "teacher2", null, 403);
        send("GET", "/statistics/board", "student4", null, 403);
        assertEquals(30, send("GET", "/statistics/board", "admin", null, 200).path("trend").size());
    }

    /** ZIP 内容与 Excel 模板必须是真实可解析的文件。 */
    @Test
    void archiveAndExcelContainExpectedFilesAndImportRowErrors() throws Exception {
        byte[] bytes = mvc
            .perform(
                get("/api/archives/download?studentId=7").header("Authorization", "Bearer " + token("admin"))
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
        List<String> names = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                names.add(entry.getName());
            }
        }
        assertEquals(8, names.size());
        assertTrue(names.contains("student-7/成绩单.xlsx"));
        assertTrue(names.contains("student-7/归档清单.txt"));
        send("GET", "/archives/download?studentId=7", "student2", null, 403);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        EasyExcel.write(output)
            .head(
                List.of(
                    List.of("账号"),
                    List.of("姓名"),
                    List.of("初始密码"),
                    List.of("角色编号"),
                    List.of("专业编号"),
                    List.of("班级编号"),
                    List.of("届次")
                )
            )
            .sheet("用户")
            .doWrite(
                List.of(
                    List.of("import_ok", "导入学生", "Import1234", "3", "1", "1", "2026"),
                    List.of("import_bad", "班级错误", "Import1234", "3", "1", "2", "2026"),
                    List.of("import_blank", "", "Import1234", "3", "1", "1", "2026")
                )
            );
        String response = mvc
            .perform(
                multipart("/api/system/users/import")
                    .file(
                        new MockMultipartFile(
                            "file",
                            "users.xlsx",
                            "application/octet-stream",
                            output.toByteArray()
                        )
                    )
                    .header("Authorization", "Bearer " + token("admin"))
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        JsonNode imported = json.readTree(response).path("data");
        assertEquals(1, imported.path("success").asInt());
        assertEquals(2, imported.path("failed").asInt());
        assertEquals(3, imported.path("rows").get(1).path("row").asInt());
        assertEquals(4, imported.path("rows").get(2).path("row").asInt());
        assertEquals("校验失败：name不能为空且长度不能超过80", imported.path("rows").get(2).path("message").asText());
        for (String path : List.of(
            "/api/system/users/template",
            "/api/exports/scores",
            "/api/exports/statistics",
            "/api/exports/missing"
        )) {
            byte[] excel = mvc
                .perform(get(path).header("Authorization", "Bearer " + token("admin")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
            assertEquals('P', excel[0]);
            assertEquals('K', excel[1]);
        }
    }

    /** Swagger 应发布全部控制器且所有业务操作有中文摘要。 */
    @Test
    void swaggerDocumentsNewAndExistingEndpoints() throws Exception {
        String raw = mvc
            .perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        JsonNode paths = json.readTree(raw).path("paths");
        assertTrue(paths.has("/api/topic-changes"));
        assertTrue(paths.has("/api/system/users/import"));
        paths.fields().forEachRemaining(path ->
            path
                .getValue()
                .elements()
                .forEachRemaining(operation -> {
                    assertTrue(operation.path("summary").asText().matches(".*[\u4e00-\u9fff].*"));
                })
        );
    }

    /** 旧库缺列和新表时可重复升级，既有用户资料不被覆盖。 */
    @Test
    void legacySchemaUpgradeIsIdempotentAndPreservesData() throws Exception {
        DriverManagerDataSource source =
            new DriverManagerDataSource(
                "jdbc:h2:mem:legacy_upgrade;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "sa",
                ""
            );
        String schema = new String(
            new ClassPathResource("db/schema.sql")
                .getInputStream()
                .readAllBytes(),
            StandardCharsets.UTF_8
        )
            .replace("    email VARCHAR(120),\n", "")
            .replace("    phone VARCHAR(30),\n", "")
            .replace("    plagiarism_threshold DECIMAL(5, 2) NOT NULL DEFAULT 30,\n", "");
        try (Connection connection = source.getConnection()) {
            ScriptUtils.executeSqlScript(
                connection,
                new EncodedResource(
                    new ByteArrayResource(
                        schema.getBytes(StandardCharsets.UTF_8)
                    ),
                    StandardCharsets.UTF_8
                )
            );
            connection
                .createStatement()
                .execute(
                    "INSERT INTO sys_user (username,password,name) VALUES ('preserved','hash','保留用户')"
                );
        }
        UpgradeMigration migration = new UpgradeMigration(
            source
        );
        migration.run(null);
        migration.run(null);
        try (
            Connection connection = source.getConnection();
            ResultSet row = connection
                .createStatement()
                .executeQuery("SELECT name,email,phone FROM sys_user WHERE username='preserved'")
        ) {
            assertTrue(row.next());
            assertEquals("保留用户", row.getString("name"));
            assertNull(row.getString("email"));
        }
    }
}
