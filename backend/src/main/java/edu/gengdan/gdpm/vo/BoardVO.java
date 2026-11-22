package edu.gengdan.gdpm.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 仅管理员可见的统计大屏，包含未发布成绩分布。 */
public class BoardVO {

    public final List<Map<String, Object>> groups;

    public final List<Map<String, Object>> students;

    public final List<Map<String, Object>> stages;

    public final List<Map<String, Object>> teacherLoad;

    public final Map<String, Integer> scoreDistribution;

    public final Map<String, Integer> trend;

    public final List<Map<String, Object>> warnings;

    public final LocalDateTime updatedAt;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public BoardVO(
        List<Map<String, Object>> groups,
        List<Map<String, Object>> students,
        List<Map<String, Object>> stages,
        List<Map<String, Object>> teacherLoad,
        Map<String, Integer> scoreDistribution,
        Map<String, Integer> trend,
        List<Map<String, Object>> warnings,
        LocalDateTime updatedAt
    ) {
        this.groups = groups;
        this.students = students;
        this.stages = stages;
        this.teacherLoad = teacherLoad;
        this.scoreDistribution = scoreDistribution;
        this.trend = trend;
        this.warnings = warnings;
        this.updatedAt = updatedAt;
    }
}
