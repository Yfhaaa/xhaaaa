package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;
import static edu.gengdan.gdpm.common.Values.*;

import edu.gengdan.gdpm.common.*;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.mapper.*;
import edu.gengdan.gdpm.service.*;
import java.math.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** StatisticsService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class StatisticsServiceImpl
    extends DomainSupport<ArchiveRecordMapper, ArchiveRecord>
    implements StatisticsService
{

    @Autowired
    private UserService studentService;

    @Autowired
    private MaterialService materials;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private NotificationService notificationService;

    /** 查询待审核课题以生成管理员待办。 */
    private List<Map<String, Object>> pendingTopics() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TopicInfo topic : topics.selectList(
            query(TopicInfo.class).eq(TopicInfo::getStatus, BusinessStatus.PENDING.label())
        )) {
            result.add(EntityViews.view(topic));
        }
        return result;
    }

    /** 计算九个业务节点的完成进度。 */
    public List<Map<String, Object>> progress() {
        List<Map<String, Object>> rows = studentService.students();
        Map<String, String> labels = Map.of(
            "task",
            "任务书",
            "proposal",
            "开题报告",
            "midterm",
            "中期检查",
            "draft",
            "论文初稿",
            "final",
            "论文定稿"
        );
        for (Map<String, Object> row : rows) {
            long sid = id(row.get("student_id"));
            int done = row.get("topic_id") != null ? 1 : 0;
            Map<String, Object> states = new LinkedHashMap<>();
            List<String> missing = new ArrayList<>();
            for (String stage : Constants.STAGES) {
                Map<String, Object> m = materials.latest(sid, stage);
                states.put(stage, m == null ? Map.of("status", "待提交") : m);
                if (m != null && "已通过".equals(m.get("status"))) {
                    done++;
                } else {
                    missing.add(labels.get(stage));
                }
            }
            long defense = defenses.selectCount(
                    query(DefenseInfo.class).eq(DefenseInfo::getStudentId, sid).eq(DefenseInfo::getStatus, "已完成")
                ),
                published = scores.selectCount(
                    query(ScoreInfo.class).eq(ScoreInfo::getStudentId, sid).eq(ScoreInfo::getPublished, 1)
                ),
                archived = archives.selectCount(query(ArchiveRecord.class).eq(ArchiveRecord::getStudentId, sid));
            done += defense + published + archived;
            row.put("materials", states);
            row.put("missing_materials", missing);
            row.put("progress", Math.round((done * 100f) / 9));
            row.put("completed_steps", done);
            row.put("archived", archived > 0);
            row.put("defense_completed", defense > 0);
            row.put("score_published", published > 0);
            List<Map<String, Object>> score = new ArrayList<>();
            for (ScoreInfo item : scores.selectList(
                query(ScoreInfo.class)
                    .eq(ScoreInfo::getStudentId, sid)
                    .eq(auth.role("STUDENT"), ScoreInfo::getPublished, 1)
            )) {
                score.add(EntityViews.view(item));
            }
            row.put("total_score", score.isEmpty() ? null : score.get(0).get("total_score"));
        }
        return rows;
    }

    /** 生成当前角色的待办、进度和提醒。 */
    public Object dashboard() {
        auth.permission("dashboard:read");
        List<Map<String, Object>> rows = progress();
        List<Map<String, Object>> todos = new ArrayList<>();
        if (auth.role("ADMIN")) {
            for (Map<String, Object> t : pendingTopics()) {
                todos.add(fields("title", "审核课题：" + t.get("title"), "path", "/topics"));
            }
            for (Map<String, Object> r : rows) {
                if (Boolean.TRUE.equals(r.get("score_published")) && !Boolean.TRUE.equals(r.get("archived"))) {
                    todos.add(fields("title", "归档：" + r.get("name"), "path", "/statistics"));
                }
            }
        } else if (auth.role("TEACHER")) {
            for (Map<String, Object> m : materials.listMaterials(null)) {
                if ("待审核".equals(m.get("status"))) {
                    todos.add(
                        fields(
                            "title",
                            "审核材料：" + m.get("student_name") + " · " + m.get("title"),
                            "path",
                            "/materials"
                        )
                    );
                }
            }
            for (Map<String, Object> s : reports.pendingSelections(auth.uid())) {
                todos.add(fields("title", "处理选题申请：" + s.get("name"), "path", "/topics"));
            }
            for (Map<String, Object> r : rows) {
                if (!materials.passed(id(r.get("student_id")), "task")) {
                    todos.add(fields("title", "下达任务书：" + r.get("name"), "path", "/materials"));
                }
            }
        } else {
            for (Map<String, Object> r : rows) {
                if (r.get("topic_id") == null) {
                    todos.add(fields("title", "浏览课题并提交双选申请", "path", "/topics"));
                } else {
                    for (String stage : List.of("proposal", "midterm", "draft", "final")) {
                        Map<String, Object> m = materials.latest(auth.uid(), stage);
                        if (m != null && "已退回".equals(m.get("status"))) {
                            todos.add(fields("title", "修改退回材料：" + m.get("title"), "path", "/materials"));
                        }
                    }
                    List<?> missing = (List<?>) r.get("missing_materials");
                    if (!missing.isEmpty()) {
                        todos.add(
                            fields(
                                "title",
                                "待完成材料：" +
                                    String.join(
                                        "、",
                                        missing
                                            .stream()
                                            .map(Object::toString)
                                            .collect(java.util.stream.Collectors.toList())
                                    ),
                                "path",
                                "/materials"
                            )
                        );
                    }
                }
            }
        }
        double average = rows
            .stream()
            .mapToLong(r -> ((Number) r.get("progress")).longValue())
            .average()
            .orElse(0);
        long archives = rows
            .stream()
            .filter(r -> Boolean.TRUE.equals(r.get("archived")))
            .count();
        return fields(
            "studentCount",
            rows.size(),
            "todoCount",
            todos.size(),
            "averageProgress",
            Math.round(average),
            "archiveCount",
            archives,
            "todos",
            todos,
            "progress",
            rows,
            "deadlines",
            workflowService.workflow(),
            "notices",
            notificationService.notices()
        );
    }

    /** 按允许维度与筛选条件统计学生进度。 */
    public Object statistics(Map<String, String> filters) {
        auth.permission("stats:read");
        String by = filters.getOrDefault("groupBy", "major");
        ApiException.require(List.of("major", "class", "teacher").contains(by), "统计维度不正确");
        for (String key : List.of("majorId", "classId", "teacherId")) {
            String value = filters.get(key);
            if (value != null && !value.isBlank()) {
                ApiException.require(value.matches("[1-9][0-9]{0,17}"), "统计筛选编号必须为正整数");
            }
        }
        List<Map<String, Object>> rows = progress();
        rows.removeIf(r -> {
            for (String key : List.of("major", "class", "teacher")) {
                String value = filters.get(key + "Id");
                if (value != null && !value.isBlank() && !value.equals(String.valueOf(r.get(key + "_id")))) {
                    return true;
                }
            }
            String cohort = filters.get("cohort");
            return cohort != null && !cohort.isBlank() && !cohort.equals(r.get("cohort"));
        });
        List<Map<String, Object>> groups = reports.statistics(auth.scope(), by, filters);
        return fields("groups", groups, "students", rows);
    }
}
