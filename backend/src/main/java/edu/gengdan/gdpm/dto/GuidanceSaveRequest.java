package edu.gengdan.gdpm.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 指导教师为本人指导学生记录指导内容和发生时间。 */
public class GuidanceSaveRequest {

    @NotNull(message = "studentId必须为正整数")
    @Min(value = 1, message = "studentId必须为正整数")
    private Long studentId;

    @NotBlank(message = "content不能为空且长度不能超过10000")
    @Size(max = 10000, message = "content不能为空且长度不能超过10000")
    private String content = "";

    @NotBlank(message = "guidanceAt不能为空且长度不能超过30")
    @Size(max = 30, message = "guidanceAt不能为空且长度不能超过30")
    private String guidanceAt = "";

    /** 返回studentId，供服务端执行权限与业务校验。 */
    public Long getStudentId() {
        return studentId;
    }

    /** 绑定studentId。 */
    public void setStudentId(Long value) {
        this.studentId = value;
    }

    /** 返回content，供服务端执行权限与业务校验。 */
    public String getContent() {
        return content;
    }

    /** 绑定content并去除首尾空白。 */
    public void setContent(String value) {
        this.content = value == null ? "" : value.trim();
    }

    /** 返回guidanceAt，供服务端执行权限与业务校验。 */
    public String getGuidanceAt() {
        return guidanceAt;
    }

    /** 绑定guidanceAt并去除首尾空白。 */
    public void setGuidanceAt(String value) {
        this.guidanceAt = value == null ? "" : value.trim();
    }
}
