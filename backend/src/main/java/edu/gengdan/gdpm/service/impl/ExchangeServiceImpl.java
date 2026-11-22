package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import com.alibaba.excel.EasyExcel;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.Constants;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.common.Values;
import edu.gengdan.gdpm.dto.UserImportRow;
import edu.gengdan.gdpm.dto.UserSaveRequest;
import edu.gengdan.gdpm.entity.ArchiveRecord;
import edu.gengdan.gdpm.entity.FileRecord;
import edu.gengdan.gdpm.entity.ScoreInfo;
import edu.gengdan.gdpm.mapper.ArchiveRecordMapper;
import edu.gengdan.gdpm.service.ExchangeService;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.service.StatisticsService;
import edu.gengdan.gdpm.service.UserService;
import edu.gengdan.gdpm.vo.ImportResultVO;
import edu.gengdan.gdpm.vo.StatisticsVO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理员下载含可删除示例行的固定七列表头用户模板。 管理员逐行导入合规用户，失败行隔离，不回传密码，最多两千行二十兆。 持 stats:read 权限导出与查询相同范围的成绩、进度或缺失材料。
 * 范围内用户下载齐全项目的五阶段材料和成绩；管理员可按班级打包，缺文件时失败。
 */
@Service
public class ExchangeServiceImpl
    extends DomainSupport<ArchiveRecordMapper, ArchiveRecord>
    implements ExchangeService
{

    @Autowired
    private UserService userService;

    @Autowired
    private StatisticsService statistics;

    @Autowired
    private MaterialService materials;

    @Value("${app.upload-dir}")
    private String uploadDir;

    /** 管理员下载含可删除示例行的固定七列表头用户模板。 */
    public byte[] template() {
        admin();
        return sheet(
            List.of("账号", "姓名", "初始密码", "角色编号", "专业编号", "班级编号", "届次"),
            List.of(List.of("example_student", "示例学生", "Change1234", "3", "1", "1", "2026"))
        );
    }

    /** 管理员逐行导入合规用户，失败行隔离，不回传密码，最多两千行二十兆。 */
    @OperLog("批量导入用户")
    public ImportResultVO importUsers(MultipartFile file) throws Exception {
        admin();
        require(
            file != null && !file.isEmpty() && file.getSize() <= 20L * 1024 * 1024,
            "请选择20MB以内的Excel"
        );
        require(
            Objects.toString(file.getOriginalFilename(), "").toLowerCase(Locale.ROOT).endsWith(".xlsx"),
            "请使用下载的xlsx模板"
        );
        List<Map<Integer, String>> header = EasyExcel.read(file.getInputStream())
            .headRowNumber(0)
            .sheet()
            .doReadSync();
        require(!header.isEmpty() && header.size() <= 2001, "每批最多导入2000行");
        List<String> expected = List.of(
            "账号",
            "姓名",
            "初始密码",
            "角色编号",
            "专业编号",
            "班级编号",
            "届次"
        );
        for (int i = 0; i < expected.size(); i++) {
            require(expected.get(i).equals(header.get(0).get(i)), "模板列标题不匹配，请重新下载模板");
        }
        List<UserImportRow> rows = EasyExcel.read(file.getInputStream())
            .head(UserImportRow.class)
            .sheet()
            .doReadSync();
        List<Map<String, Object>> results = new ArrayList<>();
        int line = 1,
            success = 0;
        for (UserImportRow row : rows) {
            line++;
            try {
                UserSaveRequest body = new UserSaveRequest();
                body.setUsername(row.getUsername());
                body.setName(row.getName());
                body.setPassword(row.getPassword());
                body.setRoleId(Long.valueOf(row.getRoleId()));
                body.setMajorId(Long.valueOf(row.getMajorId()));
                body.setClassId(Long.valueOf(row.getClassId()));
                body.setCohort(row.getCohort());
                body.setEnabled(1);
                userService.user(null, body);
                results.add(Map.of("row", line, "success", true, "message", "导入成功"));
                success++;
            } catch (Exception error) {
                String message =
                    error instanceof ApiException || error instanceof IllegalArgumentException
                        ? "校验失败：" + error.getMessage()
                        : "账号重复或数据不合法";
                results.add(Map.of("row", line, "success", false, "message", message));
            }
        }
        return new ImportResultVO(success, rows.size() - success, results);
    }

    /** 使用 EasyExcel 输出真正的 xlsx 文件。 */
    private byte[] sheet(List<String> columns, List<List<Object>> rows) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<List<String>> heads = new ArrayList<>();
        for (String name : columns) {
            heads.add(List.of(name));
        }
        EasyExcel.write(output).head(heads).autoCloseStream(false).sheet("数据").doWrite(rows);
        return output.toByteArray();
    }

    /** 持 stats:read 权限导出与查询相同范围的成绩、进度或缺失材料。 */
    public byte[] excel(String type, Map<String, String> filters) {
        auth.permission("stats:read");
        List<List<Object>> rows = new ArrayList<>();
        if (type.equals("scores")) {
            for (Map<String, Object> score : reports.scores(auth.scope())) {
                rows.add(
                    Arrays.asList(
                        score.get("student_name"),
                        score.get("guidance_score"),
                        score.get("review_score"),
                        score.get("defense_score"),
                        score.get("total_score"),
                        score.get("published")
                    )
                );
            }
            return sheet(List.of("学生", "指导分", "评阅分", "答辩分", "总成绩", "已发布"), rows);
        }
        StatisticsVO data = statistics.statistics(filters);
        if (type.equals("statistics")) {
            for (Map<String, Object> group : data.groups) {
                rows.add(
                    Arrays.asList(
                        group.get("name"),
                        group.get("student_count"),
                        group.get("average_progress"),
                        group.get("average_score"),
                        group.get("archive_count")
                    )
                );
            }
            return sheet(List.of("分组", "学生数", "平均进度", "平均成绩", "归档数"), rows);
        }
        require(type.equals("missing"), "导出类型不正确");
        for (Map<String, Object> student : data.students) {
            List<String> missing = (List<String>) student.get("missing_materials");
            if (!missing.isEmpty()) {
                rows.add(
                    Arrays.asList(
                        student.get("username"),
                        student.get("name"),
                        student.get("class_name"),
                        student.get("teacher_name"),
                        String.join("、", missing)
                    )
                );
            }
        }
        return sheet(List.of("学号", "姓名", "班级", "指导教师", "缺失材料"), rows);
    }

    /** 范围内用户下载齐全项目的五阶段材料和成绩；管理员可按班级打包，缺文件时失败。 */
    @OperLog("下载归档包")
    public byte[] archive(Long studentId, Long classId) throws Exception {
        auth.permission("stats:read");
        if (classId != null) {
            admin();
            required(classes.selectById(classId));
        }
        if (studentId != null) {
            auth.student(studentId);
        }
        require(studentId != null || classId != null, "请选择学生或班级");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int count = 0;
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            for (Map<String, Object> student : reports.students(auth.scope())) {
                long sid = Values.id(student.get("student_id"));
                if (
                    (studentId != null && studentId != sid) ||
                    (classId != null && Values.id(student.get("class_id")) != classId)
                ) {
                    continue;
                }
                ScoreInfo score = scores.selectOne(
                    query(ScoreInfo.class).eq(ScoreInfo::getStudentId, sid).eq(ScoreInfo::getPublished, 1)
                );
                boolean complete = score != null;
                for (String stage : Constants.STAGES) {
                    complete &= materials.passed(sid, stage);
                }
                if (!complete && studentId == null) {
                    continue;
                }
                require(complete, "项目材料未齐全或成绩未发布");
                String prefix = "student-" + sid + "/";
                StringBuilder manifest = new StringBuilder(
                    "毕业设计归档清单\n姓名：" +
                        student.get("name") +
                        "\n学号：" +
                        student.get("username") +
                        "\n课题：" +
                        student.get("topic_title") +
                        "\n"
                );
                for (String stage : Constants.STAGES) {
                    Map<String, Object> material = materials.latest(sid, stage);
                    FileRecord file = required(files.selectById(Values.id(material.get("file_id"))));
                    Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
                    Path target = root.resolve(file.getStoredName()).normalize();
                    require(
                        target.startsWith(root) && Files.isRegularFile(target),
                        "归档材料文件缺失：" + stage
                    );
                    String original = file.getOriginalName();
                    String ext =
                        original.lastIndexOf('.') < 0
                            ? ".bin"
                            : original.substring(original.lastIndexOf('.'));
                    String name = stage + "-v" + material.get("version") + ext;
                    add(zip, prefix + name, Files.readAllBytes(target));
                    manifest
                        .append(name)
                        .append(" · ")
                        .append(original)
                        .append(" · ")
                        .append(Files.size(target))
                        .append("字节\n");
                }
                add(
                    zip,
                    prefix + "成绩单.xlsx",
                    sheet(
                        List.of("姓名", "指导分", "评阅分", "答辩分", "总成绩"),
                        List.of(
                            Arrays.asList(
                                student.get("name"),
                                score.getGuidanceScore(),
                                score.getReviewScore(),
                                score.getDefenseScore(),
                                score.getTotalScore()
                            )
                        )
                    )
                );
                manifest.append("成绩单.xlsx\n归档清单.txt\n");
                add(zip, prefix + "归档清单.txt", manifest.toString().getBytes(StandardCharsets.UTF_8));
                count++;
            }
            require(count > 0, "没有符合条件的已完成项目");
            add(zip, "批次清单.txt", ("已归档项目数：" + count).getBytes(StandardCharsets.UTF_8));
        }
        return output.toByteArray();
    }

    /** 统一 ZIP 条目写入。 */
    private void add(ZipOutputStream zip, String name, byte[] bytes) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(bytes);
        zip.closeEntry();
    }
}
