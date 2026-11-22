package edu.gengdan.gdpm.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 课题、双选、材料和变更审批的严格布尔结果及必填意见。 */
public class ReviewRequest {

    @NotNull(message = "approved必须为布尔值")
    @JsonDeserialize(using = StrictBooleanDeserializer.class)
    private Boolean approved;

    @NotBlank(message = "comment不能为空且长度不能超过2000")
    @Size(max = 2000, message = "comment不能为空且长度不能超过2000")
    private String comment = "";

    /** 返回approved，供服务端执行权限与业务校验。 */
    public Boolean getApproved() {
        return approved;
    }

    /** 绑定approved。 */
    public void setApproved(Boolean value) {
        this.approved = value;
    }

    /** 返回comment，供服务端执行权限与业务校验。 */
    public String getComment() {
        return comment;
    }

    /** 绑定comment并去除首尾空白。 */
    public void setComment(String value) {
        this.comment = value == null ? "" : value.trim();
    }
}
