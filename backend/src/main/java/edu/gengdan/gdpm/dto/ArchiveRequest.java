package edu.gengdan.gdpm.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/** 管理员提交归档对象，完整材料与已发布成绩由服务端核验。 */
public class ArchiveRequest {

    @NotNull(message = "studentId必须为正整数")
    @Min(value = 1, message = "studentId必须为正整数")
    private Long studentId;

    /** 返回studentId，供服务端执行权限与业务校验。 */
    public Long getStudentId() {
        return studentId;
    }

    /** 绑定studentId。 */
    public void setStudentId(Long value) {
        this.studentId = value;
    }
}
