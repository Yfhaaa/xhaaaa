package edu.gengdan.gdpm.vo;

import java.util.List;
import java.util.Map;

/** 当前用户可见的待办、九节点进度、通知与截止时间。 */
public class DashboardVO {

    public final int studentCount;

    public final int todoCount;

    public final long averageProgress;

    public final long archiveCount;

    public final List<Map<String, Object>> todos;

    public final List<Map<String, Object>> progress;

    public final List<Map<String, Object>> deadlines;

    public final List<Map<String, Object>> notices;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public DashboardVO(
        int studentCount,
        int todoCount,
        long averageProgress,
        long archiveCount,
        List<Map<String, Object>> todos,
        List<Map<String, Object>> progress,
        List<Map<String, Object>> deadlines,
        List<Map<String, Object>> notices
    ) {
        this.studentCount = studentCount;
        this.todoCount = todoCount;
        this.averageProgress = averageProgress;
        this.archiveCount = archiveCount;
        this.todos = todos;
        this.progress = progress;
        this.deadlines = deadlines;
        this.notices = notices;
    }
}
