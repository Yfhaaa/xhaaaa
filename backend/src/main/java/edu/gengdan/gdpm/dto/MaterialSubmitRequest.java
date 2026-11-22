package edu.gengdan.gdpm.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 按流程提交新版本；任务书额外要求学生编号，其余阶段使用当前学生。 */
public class MaterialSubmitRequest {

    @NotNull(message = "studentId必须为正整数", groups = Task.class)
    @Min(value = 1, message = "studentId必须为正整数", groups = Task.class)
    private Long studentId;

    @NotNull(message = "fileId必须为正整数")
    @Min(value = 1, message = "fileId必须为正整数")
    private Long fileId;

    @NotBlank(message = "title不能为空且长度不能超过200")
    @Size(max = 200, message = "title不能为空且长度不能超过200")
    private String title = "";

    private String content = "";

    /** 返回studentId，供服务端执行权限与业务校验。 */
    public Long getStudentId() {
        return studentId;
    }

    /** 绑定studentId。 */
    public void setStudentId(Long value) {
        this.studentId = value;
    }

    /** 返回fileId，供服务端执行权限与业务校验。 */
    public Long getFileId() {
        return fileId;
    }

    /** 绑定fileId。 */
    public void setFileId(Long value) {
        this.fileId = value;
    }

    /** 返回title，供服务端执行权限与业务校验。 */
    public String getTitle() {
        return title;
    }

    /** 绑定title并去除首尾空白。 */
    public void setTitle(String value) {
        this.title = value == null ? "" : value.trim();
    }

    /** 返回content，供服务端执行权限与业务校验。 */
    public String getContent() {
        return content;
    }

    /** 绑定content并去除首尾空白。 */
    public void setContent(String value) {
        this.content = value == null ? "" : value.trim();
    }

    /** 任务书下达时才要求学生编号。 */
    public interface Task {}
}
