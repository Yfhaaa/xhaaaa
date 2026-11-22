package edu.gengdan.gdpm.vo;

import java.util.List;
import java.util.Map;

/** 统计筛选后的分组及范围内学生，不改变报表行下划线字段。 */
public class StatisticsVO {

    public final List<Map<String, Object>> groups;

    public final List<Map<String, Object>> students;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public StatisticsVO(
        List<Map<String, Object>> groups,
        List<Map<String, Object>> students
    ) {
        this.groups = groups;
        this.students = students;
    }
}
