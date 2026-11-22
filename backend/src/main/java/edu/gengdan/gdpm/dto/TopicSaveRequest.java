package edu.gengdan.gdpm.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 教师申报课题；必填专业、容量和正文，退回后方可重提。 */
public class TopicSaveRequest {

    @NotBlank(message = "title不能为空且长度不能超过200")
    @Size(max = 200, message = "title不能为空且长度不能超过200")
    private String title = "";

    @NotBlank(message = "description不能为空且长度不能超过10000")
    @Size(max = 10000, message = "description不能为空且长度不能超过10000")
    private String description = "";

    @NotNull(message = "majorId必须为正整数")
    @Min(value = 1, message = "majorId必须为正整数")
    private Long majorId;

    @NotNull(message = "capacity必须为正整数")
    @Min(value = 1, message = "capacity必须为正整数")
    @Max(value = 20, message = "每个课题最多20人")
    private Integer capacity;

    /** 返回title，供服务端执行权限与业务校验。 */
    public String getTitle() {
        return title;
    }

    /** 绑定title并去除首尾空白。 */
    public void setTitle(String value) {
        this.title = value == null ? "" : value.trim();
    }

    /** 返回description，供服务端执行权限与业务校验。 */
    public String getDescription() {
        return description;
    }

    /** 绑定description并去除首尾空白。 */
    public void setDescription(String value) {
        this.description = value == null ? "" : value.trim();
    }

    /** 返回majorId，供服务端执行权限与业务校验。 */
    public Long getMajorId() {
        return majorId;
    }

    /** 绑定majorId。 */
    public void setMajorId(Long value) {
        this.majorId = value;
    }

    /** 返回capacity，供服务端执行权限与业务校验。 */
    public Integer getCapacity() {
        return capacity;
    }

    /** 绑定capacity。 */
    public void setCapacity(Integer value) {
        this.capacity = value;
    }
}
