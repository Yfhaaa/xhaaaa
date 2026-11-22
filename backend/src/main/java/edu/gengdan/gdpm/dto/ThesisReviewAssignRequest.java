package edu.gengdan.gdpm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 管理员按单学生或批量学生分配评阅，可选指定非指导教师。 */
public class ThesisReviewAssignRequest {

    @Min(value = 1, message = "studentId必须为正整数")
    private Long studentId;

    @Size(min = 1, max = 200, message = "每批请选择1至200名学生")
    private List<@NotNull(message = "studentId必须为正整数") Long> studentIds;

    @Min(value = 1, message = "reviewerId必须为正整数")
    private Long reviewerId;

    /** 返回studentId，供服务端执行权限与业务校验。 */
    public Long getStudentId() {
        return studentId;
    }

    /** 绑定studentId。 */
    public void setStudentId(Long value) {
        this.studentId = value;
    }

    /** 返回studentIds，供服务端执行权限与业务校验。 */
    public List<@NotNull(message = "studentId必须为正整数") Long> getStudentIds() {
        return studentIds;
    }

    /** 绑定studentIds。 */
    public void setStudentIds(List<@NotNull(message = "studentId必须为正整数") Long> value) {
        this.studentIds = value;
    }

    /** 返回reviewerId，供服务端执行权限与业务校验。 */
    public Long getReviewerId() {
        return reviewerId;
    }

    /** 绑定reviewerId。 */
    public void setReviewerId(Long value) {
        this.reviewerId = value;
    }

    /** 单人分配与批量分配至少提供一种学生编号。 */
    @AssertTrue(message = "studentId必须为正整数")
    @JsonIgnore
    public boolean isStudentSelectionValid() {
        return studentIds != null || studentId != null;
    }
}
