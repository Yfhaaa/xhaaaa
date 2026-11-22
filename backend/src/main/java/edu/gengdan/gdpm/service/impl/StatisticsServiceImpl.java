package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.Values.fields;
import static edu.gengdan.gdpm.common.Values.id;

import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.Constants;
import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.entity.ArchiveRecord;
import edu.gengdan.gdpm.entity.DefenseInfo;
import edu.gengdan.gdpm.entity.ScoreInfo;
import edu.gengdan.gdpm.entity.TopicInfo;
import edu.gengdan.gdpm.mapper.ArchiveRecordMapper;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.service.NotificationService;
import edu.gengdan.gdpm.service.StatisticsService;
import edu.gengdan.gdpm.service.UserService;
import edu.gengdan.gdpm.service.WorkflowService;
import edu.gengdan.gdpm.vo.DashboardVO;
import edu.gengdan.gdpm.vo.StatisticsVO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 按当前身份的数据范围汇总九节点进度，学生未发布成绩保持隐藏。 持 dashboard:read 权限查看当前身份的待办、进度、通知与截止提醒。 持 stats:read
 * 权限按专业、班级或教师统计，先做身份范围过滤再应用业务筛选。
 */
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

    /** 按当前身份的数据范围汇总九节点进度，学生未发布成绩保持隐藏。 */
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
                states.put(stage, m == null ? Map.of("status", BusinessStatus.NOT_SUBMITTED.label()) : m);
                if (m != null && BusinessStatus.APPROVED.label().equals(m.get("status"))) {
                    done++;
                } else {
                    missing.add(labels.get(stage));
                }
            }
            long defense = defenses.selectCount(
                    query(DefenseInfo.class)
                        .eq(DefenseInfo::getStudentId, sid)
                        .eq(DefenseInfo::getStatus, BusinessStatus.COMPLETED.label())
                ),
                published = scores.selectCount(
                    query(ScoreInfo.class).eq(ScoreInfo::getStudentId, sid).eq(ScoreInfo::getPublished, 1)
                ),
                archived = archives.selectCount(
                    query(ArchiveRecord.class).eq(ArchiveRecord::getStudentId, sid)
                );
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

    /** 持 dashboard:read 权限查看当前身份的待办、进度、通知与截止提醒。 */
    public DashboardVO dashboard() {
        auth.permission("dashboard:read");
        List<Map<String, Object>> rows = progress();
        List<Map<String, Object>> todos = new ArrayList<>();
        if (auth.role("ADMIN")) {
            for (Map<String, Object> t : pendingTopics()) {
                todos.add(fields("title", "审核课题：" + t.get("title"), "path", "/topics/audit"));
            }
            for (Map<String, Object> r : rows) {
                if (
                    Boolean.TRUE.equals(r.get("score_published")) && !Boolean.TRUE.equals(r.get("archived"))
                ) {
                    todos.add(fields("title", "归档：" + r.get("name"), "path", "/archives"));
                }
            }
        } else if (auth.role("TEACHER")) {
            for (Map<String, Object> m : materials.listMaterials(null)) {
                if (BusinessStatus.PENDING.label().equals(m.get("status"))) {
                    todos.add(
                        fields(
                            "title",
                            "审核材料：" + m.get("student_name") + " · " + m.get("title"),
                            "path",
                            "/pending-reviews"
                        )
                    );
                }
            }
            for (Map<String, Object> s : reports.pendingSelections(auth.uid())) {
                todos.add(fields("title", "处理选题申请：" + s.get("name"), "path", "/selections"));
            }
            for (Map<String, Object> r : rows) {
                if (!materials.passed(id(r.get("student_id")), "task")) {
                    todos.add(fields("title", "下达任务书：" + r.get("name"), "path", "/materials/task"));
                }
            }
        } else {
            for (Map<String, Object> r : rows) {
                if (r.get("topic_id") == null) {
                    todos.add(fields("title", "浏览课题并提交双选申请", "path", "/selections"));
                } else {
                    for (String stage : List.of("proposal", "midterm", "draft", "final")) {
                        Map<String, Object> m = materials.latest(auth.uid(), stage);
                        if (m != null && BusinessStatus.RETURNED.label().equals(m.get("status"))) {
                            todos.add(
                                fields("title", "修改退回材料：" + m.get("title"), "path", "/my-project")
                            );
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
                                            .collect(Collectors.toList())
                                    ),
                                "path",
                                "/my-project"
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
        return new DashboardVO(
            rows.size(),
            todos.size(),
            Math.round(average),
            archives,
            todos,
            rows,
            workflowService.workflow(),
            notificationService.notices()
        );
    }

    /** 持 stats:read 权限按专业、班级或教师统计，先做身份范围过滤再应用业务筛选。 */
    public StatisticsVO statistics(Map<String, String> filters) {
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
        return new StatisticsVO(groups, rows);
    }
}
