package edu.gengdan.gdpm.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 指定评阅教师一次提交意见、百分制分数及是否同意答辩。 */
public class ThesisReviewRequest {

    @NotNull(message = "approved必须为布尔值")
    @JsonDeserialize(using = StrictBooleanDeserializer.class)
    private Boolean approved;

    @NotBlank(message = "comment不能为空且长度不能超过2000")
    @Size(max = 2000, message = "comment不能为空且长度不能超过2000")
    private String comment = "";

    @NotNull(message = "请求参数格式不正确")
    @Digits(integer = 100, fraction = 2, message = "score最多保留两位小数")
    @DecimalMin(value = "0", message = "score超出范围")
    @DecimalMax(value = "100", message = "score超出范围")
    private BigDecimal score;

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

    /** 返回score，供服务端执行权限与业务校验。 */
    public BigDecimal getScore() {
        return score;
    }

    /** 绑定score。 */
    public void setScore(BigDecimal value) {
        this.score = value;
    }
}
