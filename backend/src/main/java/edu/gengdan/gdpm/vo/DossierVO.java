package edu.gengdan.gdpm.vo;

import java.util.List;
import java.util.Map;

/** 授权学生的全过程时间线、材料版本、指导和成绩。 */
public class DossierVO {

    public final Map<String, Object> student;

    public final List<Map<String, Object>> timeline;

    public final List<Map<String, Object>> guidance;

    public final List<Map<String, Object>> scores;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public DossierVO(
        Map<String, Object> student,
        List<Map<String, Object>> timeline,
        List<Map<String, Object>> guidance,
        List<Map<String, Object>> scores
    ) {
        this.student = student;
        this.timeline = timeline;
        this.guidance = guidance;
        this.scores = scores;
    }
}
