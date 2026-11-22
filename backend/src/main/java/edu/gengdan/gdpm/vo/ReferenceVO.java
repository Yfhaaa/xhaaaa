package edu.gengdan.gdpm.vo;

import edu.gengdan.gdpm.entity.Cohort;
import edu.gengdan.gdpm.entity.Major;
import java.util.List;
import java.util.Map;

/** 登录用户可见的下拉数据，教师列表仅管理员获取。 */
public class ReferenceVO {

    public final List<Major> majors;

    public final List<Map<String, Object>> classes;

    public final List<Cohort> cohorts;

    public final List<Map<String, Object>> teachers;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public ReferenceVO(
        List<Major> majors,
        List<Map<String, Object>> classes,
        List<Cohort> cohorts,
        List<Map<String, Object>> teachers
    ) {
        this.majors = majors;
        this.classes = classes;
        this.cohorts = cohorts;
        this.teachers = teachers;
    }
}
