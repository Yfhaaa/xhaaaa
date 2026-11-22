package edu.gengdan.gdpm.dto;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 指导教师或管理员为当前定稿登记本人上传的查重报告。 */
public class PlagiarismRegisterRequest {

    @NotNull(message = "studentId必须为正整数")
    @Min(value = 1, message = "studentId必须为正整数")
    private Long studentId;

    @NotNull(message = "fileId必须为正整数")
    @Min(value = 1, message = "fileId必须为正整数")
    private Long fileId;

    @NotBlank(message = "platform不能为空且长度不能超过100")
    @Size(max = 100, message = "platform不能为空且长度不能超过100")
    private String platform = "";

    @NotNull(message = "请求参数格式不正确")
    @Digits(integer = 100, fraction = 2, message = "rate最多保留两位小数")
    @DecimalMin(value = "0", message = "rate超出范围")
    @DecimalMax(value = "100", message = "rate超出范围")
    private BigDecimal rate;

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

    /** 返回platform，供服务端执行权限与业务校验。 */
    public String getPlatform() {
        return platform;
    }

    /** 绑定platform并去除首尾空白。 */
    public void setPlatform(String value) {
        this.platform = value == null ? "" : value.trim();
    }

    /** 返回rate，供服务端执行权限与业务校验。 */
    public BigDecimal getRate() {
        return rate;
    }

    /** 绑定rate。 */
    public void setRate(BigDecimal value) {
        this.rate = value;
    }
}
