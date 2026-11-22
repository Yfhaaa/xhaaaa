package edu.gengdan.gdpm.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/** 管理员为定稿通过且评阅同意的学生安排答辩。 */
public class DefenseArrangeRequest {

    @NotNull(message = "studentId必须为正整数")
    @Min(value = 1, message = "studentId必须为正整数")
    private Long studentId;

    @NotNull(message = "groupId必须为正整数")
    @Min(value = 1, message = "groupId必须为正整数")
    private Long groupId;

    /** 返回studentId，供服务端执行权限与业务校验。 */
    public Long getStudentId() {
        return studentId;
    }

    /** 绑定studentId。 */
    public void setStudentId(Long value) {
        this.studentId = value;
    }

    /** 返回groupId，供服务端执行权限与业务校验。 */
    public Long getGroupId() {
        return groupId;
    }

    /** 绑定groupId。 */
    public void setGroupId(Long value) {
        this.groupId = value;
    }
}
