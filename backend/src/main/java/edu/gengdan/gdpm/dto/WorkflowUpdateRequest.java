package edu.gengdan.gdpm.dto;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 管理员更新截止时间，可选更新定稿重复率阈值。 */
public class WorkflowUpdateRequest {

    @NotBlank(message = "deadline不能为空且长度不能超过30")
    @Size(max = 30, message = "deadline不能为空且长度不能超过30")
    private String deadline = "";

    @Digits(integer = 100, fraction = 2, message = "plagiarismThreshold最多保留两位小数")
    @DecimalMin(value = "0", message = "plagiarismThreshold超出范围")
    @DecimalMax(value = "100", message = "plagiarismThreshold超出范围")
    private BigDecimal plagiarismThreshold;

    /** 返回deadline，供服务端执行权限与业务校验。 */
    public String getDeadline() {
        return deadline;
    }

    /** 绑定deadline并去除首尾空白。 */
    public void setDeadline(String value) {
        this.deadline = value == null ? "" : value.trim();
    }

    /** 返回plagiarismThreshold，供服务端执行权限与业务校验。 */
    public BigDecimal getPlagiarismThreshold() {
        return plagiarismThreshold;
    }

    /** 绑定plagiarismThreshold。 */
    public void setPlagiarismThreshold(BigDecimal value) {
        this.plagiarismThreshold = value;
    }
}
