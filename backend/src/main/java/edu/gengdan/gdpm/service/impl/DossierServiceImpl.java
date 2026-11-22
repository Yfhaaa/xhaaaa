package edu.gengdan.gdpm.service.impl;

import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.Constants;
import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.common.Values;
import edu.gengdan.gdpm.entity.GuidanceRecord;
import edu.gengdan.gdpm.entity.ReviewRecord;
import edu.gengdan.gdpm.entity.ScoreInfo;
import edu.gengdan.gdpm.entity.StudentAssignment;
import edu.gengdan.gdpm.entity.TopicChange;
import edu.gengdan.gdpm.entity.TopicSelection;
import edu.gengdan.gdpm.entity.User;
import edu.gengdan.gdpm.entity.WorkflowStep;
import edu.gengdan.gdpm.mapper.UserMapper;
import edu.gengdan.gdpm.service.DossierService;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.service.StatisticsService;
import edu.gengdan.gdpm.vo.BoardVO;
import edu.gengdan.gdpm.vo.DossierVO;
import edu.gengdan.gdpm.vo.StatisticsVO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 校验学生范围后聚合本人全过程时间线、全部材料版本、指导记录与可见成绩。 仅管理员获取大屏汇总、负载、三十天趋势和逾期预警，允许统计未发布成绩。 */
@Service
public class DossierServiceImpl extends DomainSupport<UserMapper, User> implements DossierService {

    @Autowired
    private MaterialService materials;

    @Autowired
    private StatisticsService statistics;

    /** 校验学生范围后聚合本人全过程时间线、全部材料版本、指导记录与可见成绩。 */
    public DossierVO dossier(long sid) {
        auth.permission("material:read");
        auth.student(sid);
        Map<String, Object> student = reports
            .students(auth.scope())
            .stream()
            .filter(r -> Values.id(r.get("student_id")) == sid)
            .findFirst()
            .orElseThrow(() -> new ApiException(404, "学生不存在"));
        List<Map<String, Object>> timeline = new ArrayList<>();
        Map<String, String> names = new LinkedHashMap<>();
        names.put("selection", "确定选题");
        names.put("task", "任务书");
        names.put("proposal", "开题报告");
        names.put("midterm", "中期检查");
        names.put("draft", "论文初稿");
        names.put("final", "论文定稿");
        names.put("defense", "答辩评定");
        names.put("score", "成绩发布");
        names.put("archive", "项目归档");
        List<Map<String, Object>> all = materials.listMaterials(null);
        for (Map.Entry<String, String> entry : names.entrySet()) {
            String stage = entry.getKey();
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("code", stage);
            node.put("name", entry.getValue());
            List<Map<String, Object>> versions = new ArrayList<>();
            if (Constants.STAGES.contains(stage)) {
                for (Map<String, Object> material : all) {
                    if (Values.id(material.get("student_id")) == sid && stage.equals(material.get("stage"))) {
                        Map<String, Object> version = new LinkedHashMap<>(material);
                        Object reviewer = material.get("reviewer_id");
                        version.put(
                            "reviewer_name",
                            reviewer == null
                                ? BusinessStatus.PENDING.label()
                                : required(users.selectById(Values.id(reviewer))).getName()
                        );
                        versions.add(version);
                    }
                }
                versions.sort((a, b) ->
                    Integer.compare(
                        ((Number) b.get("version")).intValue(),
                        ((Number) a.get("version")).intValue()
                    )
                );
            } else if (stage.equals("selection")) {
                for (TopicSelection selection : selections.selectList(
                    query(TopicSelection.class)
                        .eq(TopicSelection::getStudentId, sid)
                        .orderByDesc(TopicSelection::getId)
                )) {
                    Map<String, Object> view = EntityViews.view(selection);
                    view.put("title", required(topics.selectById(selection.getTopicId())).getTitle());
                    if (selection.getReviewerId() != null) {
                        view.put(
                            "reviewer_name",
                            required(users.selectById(selection.getReviewerId())).getName()
                        );
                    }
                    versions.add(view);
                }
                versions.sort((a, b) ->
                    Boolean.compare(
                        Objects.equals(b.get("topic_id"), student.get("topic_id")),
                        Objects.equals(a.get("topic_id"), student.get("topic_id"))
                    )
                );
                TopicChange change = changes.selectOne(
                    query(TopicChange.class)
                        .eq(TopicChange::getStudentId, sid)
                        .eq(TopicChange::getStatus, BusinessStatus.APPROVED.label())
                        .orderByDesc(TopicChange::getId)
                        .last("LIMIT 1")
                );
                if (change != null && Objects.equals(change.getNewTopicId(), student.get("topic_id"))) {
                    Map<String, Object> current = new LinkedHashMap<>();
                    current.put("title", student.get("topic_title"));
                    current.put("status", BusinessStatus.APPROVED.label());
                    current.put("submitted_at", change.getCreatedAt());
                    current.put("review_comment", "三级变更审批通过，当前课题已调整");
                    List<ReviewRecord> finalReviews = reviews.selectList(
                        query(ReviewRecord.class)
                            .eq(ReviewRecord::getBusinessType, "topic_change")
                            .eq(ReviewRecord::getBusinessId, change.getId())
                            .orderByDesc(ReviewRecord::getId)
                    );
                    if (!finalReviews.isEmpty()) {
                        current.put(
                            "reviewer_name",
                            required(users.selectById(finalReviews.get(0).getReviewerId())).getName()
                        );
                    }
                    versions.add(0, current);
                }
            } else if (stage.equals("defense")) {
                for (Map<String, Object> defense : reports.defenses(auth.scope())) {
                    if (Values.id(defense.get("student_id")) == sid) {
                        versions.add(defense);
                    }
                }
            } else if (stage.equals("score")) {
                for (Map<String, Object> score : reports.scores(auth.scope())) {
                    if (Values.id(score.get("student_id")) == sid) {
                        score.put(
                            "status",
                            Values.id(score.get("published")) == 1
                                ? BusinessStatus.COMPLETED.label()
                                : BusinessStatus.AWAITING_PUBLICATION.label()
                        );
                        versions.add(score);
                    }
                }
            } else {
                for (Map<String, Object> archive : reports.archives(auth.scope())) {
                    if (Values.id(archive.get("student_id")) == sid) {
                        versions.add(archive);
                    }
                }
            }
            node.put("versions", versions);
            node.put(
                "status",
                versions.isEmpty()
                    ? BusinessStatus.NOT_SUBMITTED.label()
                    : versions.get(0).getOrDefault("status", BusinessStatus.COMPLETED.label())
            );
            timeline.add(node);
        }
        List<Map<String, Object>> guidance = new ArrayList<>(),
            score = new ArrayList<>();
        for (GuidanceRecord record : guidanceRecords.selectList(
            query(GuidanceRecord.class)
                .eq(GuidanceRecord::getStudentId, sid)
                .orderByDesc(GuidanceRecord::getGuidanceAt)
        )) {
            Map<String, Object> view = EntityViews.view(record);
            view.put("teacher_name", required(users.selectById(record.getTeacherId())).getName());
            guidance.add(view);
        }
        for (Map<String, Object> row : reports.scores(auth.scope())) {
            if (Values.id(row.get("student_id")) == sid) {
                score.add(row);
            }
        }
        return new DossierVO(student, timeline, guidance, score);
    }

    /** 仅管理员获取大屏汇总、负载、三十天趋势和逾期预警，允许统计未发布成绩。 */
    public BoardVO board() {
        admin();
        StatisticsVO data = statistics.statistics(Map.of("groupBy", "major"));
        List<Map<String, Object>> students = data.students;
        List<Map<String, Object>> stages = new ArrayList<>(),
            load = new ArrayList<>(),
            warnings = new ArrayList<>();
        for (String stage : List.of(
            "selection",
            "task",
            "proposal",
            "midterm",
            "draft",
            "final",
            "defense"
        )) {
            long count = 0;
            for (Map<String, Object> student : students) {
                long sid = Values.id(student.get("student_id"));
                Map<String, Object> state = reports.progressState(sid);
                if (((Number) state.getOrDefault(stage, 0)).longValue() > 0) {
                    count++;
                }
            }
            stages.add(Map.of("name", stage, "value", count));
        }
        for (Map<String, Object> teacher : reports.teachers()) {
            long id = Values.id(teacher.get("id"));
            load.add(
                Map.of(
                    "name",
                    teacher.get("name"),
                    "value",
                    assignments.selectCount(
                        query(StudentAssignment.class).eq(StudentAssignment::getTeacherId, id)
                    )
                )
            );
        }
        Map<String, Integer> bins = new LinkedHashMap<>();
        for (String bin : List.of("90–100", "80–89", "70–79", "60–69", "低于60")) {
            bins.put(bin, 0);
        }
        for (ScoreInfo score : scores.selectList(null)) {
            double value = score.getTotalScore().doubleValue();
            String bin =
                value >= 90
                    ? "90–100"
                    : value >= 80
                      ? "80–89"
                      : value >= 70
                        ? "70–79"
                        : value >= 60
                          ? "60–69"
                          : "低于60";
            bins.put(bin, bins.get(bin) + 1);
        }
        Map<String, Integer> trend = new TreeMap<>();
        for (int i = 29; i >= 0; i--) {
            trend.put(LocalDate.now().minusDays(i).toString(), 0);
        }
        for (Map<String, Object> material : materials.listMaterials(null)) {
            String date = String.valueOf(material.get("submitted_at")).substring(0, 10);
            if (trend.containsKey(date)) {
                trend.put(date, trend.get(date) + 1);
            }
        }
        for (WorkflowStep step : workflowSteps.selectList(null)) {
            if (step.getDeadline().isAfter(LocalDateTime.now())) {
                continue;
            }
            for (Map<String, Object> student : students) {
                Map<String, Object> state = reports.progressState(Values.id(student.get("student_id")));
                if (((Number) state.getOrDefault(step.getCode(), 0)).longValue() == 0) {
                    warnings.add(
                        Map.of(
                            "student",
                            student.get("name"),
                            "stage",
                            step.getName(),
                            "deadline",
                            step.getDeadline()
                        )
                    );
                }
            }
        }
        return new BoardVO(
            data.groups, students, stages, load, bins, trend, warnings, LocalDateTime.now()
        );
    }
}
