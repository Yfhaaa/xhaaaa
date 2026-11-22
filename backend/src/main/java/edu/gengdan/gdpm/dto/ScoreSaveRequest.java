package edu.gengdan.gdpm.dto;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/** 指导教师录分；评阅分只作兼容输入，实际计算引用服务端评阅记录。 */
public class ScoreSaveRequest {

    @NotNull(message = "studentId必须为正整数")
    @Min(value = 1, message = "studentId必须为正整数")
    private Long studentId;

    @NotNull(message = "请求参数格式不正确")
    @Digits(integer = 100, fraction = 2, message = "guidanceScore最多保留两位小数")
    @DecimalMin(value = "0", message = "guidanceScore超出范围")
    @DecimalMax(value = "100", message = "guidanceScore超出范围")
    private BigDecimal guidanceScore;

    @Digits(integer = 100, fraction = 2, message = "reviewScore最多保留两位小数")
    @DecimalMin(value = "0", message = "reviewScore超出范围")
    @DecimalMax(value = "100", message = "reviewScore超出范围")
    private BigDecimal reviewScore;

    @NotNull(message = "请求参数格式不正确")
    @Digits(integer = 100, fraction = 2, message = "defenseScore最多保留两位小数")
    @DecimalMin(value = "0", message = "defenseScore超出范围")
    @DecimalMax(value = "100", message = "defenseScore超出范围")
    private BigDecimal defenseScore;

    @NotNull(message = "请求参数格式不正确")
    @Digits(integer = 100, fraction = 2, message = "guidanceWeight最多保留两位小数")
    @DecimalMin(value = "0", message = "guidanceWeight超出范围")
    @DecimalMax(value = "1", message = "guidanceWeight超出范围")
    private BigDecimal guidanceWeight;

    @NotNull(message = "请求参数格式不正确")
    @Digits(integer = 100, fraction = 2, message = "reviewWeight最多保留两位小数")
    @DecimalMin(value = "0", message = "reviewWeight超出范围")
    @DecimalMax(value = "1", message = "reviewWeight超出范围")
    private BigDecimal reviewWeight;

    @NotNull(message = "请求参数格式不正确")
    @Digits(integer = 100, fraction = 2, message = "defenseWeight最多保留两位小数")
    @DecimalMin(value = "0", message = "defenseWeight超出范围")
    @DecimalMax(value = "1", message = "defenseWeight超出范围")
    private BigDecimal defenseWeight;

    /** 返回studentId，供服务端执行权限与业务校验。 */
    public Long getStudentId() {
        return studentId;
    }

    /** 绑定studentId。 */
    public void setStudentId(Long value) {
        this.studentId = value;
    }

    /** 返回guidanceScore，供服务端执行权限与业务校验。 */
    public BigDecimal getGuidanceScore() {
        return guidanceScore;
    }

    /** 绑定guidanceScore。 */
    public void setGuidanceScore(BigDecimal value) {
        this.guidanceScore = value;
    }

    /** 返回reviewScore，供服务端执行权限与业务校验。 */
    public BigDecimal getReviewScore() {
        return reviewScore;
    }

    /** 绑定reviewScore。 */
    public void setReviewScore(BigDecimal value) {
        this.reviewScore = value;
    }

    /** 返回defenseScore，供服务端执行权限与业务校验。 */
    public BigDecimal getDefenseScore() {
        return defenseScore;
    }

    /** 绑定defenseScore。 */
    public void setDefenseScore(BigDecimal value) {
        this.defenseScore = value;
    }

    /** 返回guidanceWeight，供服务端执行权限与业务校验。 */
    public BigDecimal getGuidanceWeight() {
        return guidanceWeight;
    }

    /** 绑定guidanceWeight。 */
    public void setGuidanceWeight(BigDecimal value) {
        this.guidanceWeight = value;
    }

    /** 返回reviewWeight，供服务端执行权限与业务校验。 */
    public BigDecimal getReviewWeight() {
        return reviewWeight;
    }

    /** 绑定reviewWeight。 */
    public void setReviewWeight(BigDecimal value) {
        this.reviewWeight = value;
    }

    /** 返回defenseWeight，供服务端执行权限与业务校验。 */
    public BigDecimal getDefenseWeight() {
        return defenseWeight;
    }

    /** 绑定defenseWeight。 */
    public void setDefenseWeight(BigDecimal value) {
        this.defenseWeight = value;
    }
}
