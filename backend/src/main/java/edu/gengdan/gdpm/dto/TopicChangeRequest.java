package edu.gengdan.gdpm.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 学生申请变更到同专业已通过且有名额的课题。 */
public class TopicChangeRequest {

    @NotNull(message = "newTopicId必须为正整数")
    @Min(value = 1, message = "newTopicId必须为正整数")
    private Long newTopicId;

    @NotBlank(message = "reason不能为空且长度不能超过2000")
    @Size(max = 2000, message = "reason不能为空且长度不能超过2000")
    private String reason = "";

    /** 返回newTopicId，供服务端执行权限与业务校验。 */
    public Long getNewTopicId() {
        return newTopicId;
    }

    /** 绑定newTopicId。 */
    public void setNewTopicId(Long value) {
        this.newTopicId = value;
    }

    /** 返回reason，供服务端执行权限与业务校验。 */
    public String getReason() {
        return reason;
    }

    /** 绑定reason并去除首尾空白。 */
    public void setReason(String value) {
        this.reason = value == null ? "" : value.trim();
    }
}
