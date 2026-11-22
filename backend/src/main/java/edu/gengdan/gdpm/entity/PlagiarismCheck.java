package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** plagiarism_check 业务记录，保留各环节身份与时间。 */
@TableName("plagiarism_check")
public class PlagiarismCheck {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long thesisId;
    private String platform;
    private BigDecimal rate;
    private Long fileId;
    private Long registrarId;
    private LocalDateTime createdAt;

    /** 读取编号。 */
    public Long getId() {
        return id;
    }

    /** 设置编号。 */
    public void setId(Long value) {
        this.id = value;
    }

    /** 读取学生编号。 */
    public Long getStudentId() {
        return studentId;
    }

    /** 设置学生编号。 */
    public void setStudentId(Long value) {
        this.studentId = value;
    }

    /** 读取ThesisId字段。 */
    public Long getThesisId() {
        return thesisId;
    }

    /** 设置ThesisId字段。 */
    public void setThesisId(Long value) {
        this.thesisId = value;
    }

    /** 读取查重平台。 */
    public String getPlatform() {
        return platform;
    }

    /** 设置查重平台。 */
    public void setPlatform(String value) {
        this.platform = value;
    }

    /** 读取重复率。 */
    public BigDecimal getRate() {
        return rate;
    }

    /** 设置重复率。 */
    public void setRate(BigDecimal value) {
        this.rate = value;
    }

    /** 读取附件编号。 */
    public Long getFileId() {
        return fileId;
    }

    /** 设置附件编号。 */
    public void setFileId(Long value) {
        this.fileId = value;
    }

    /** 读取RegistrarId字段。 */
    public Long getRegistrarId() {
        return registrarId;
    }

    /** 设置RegistrarId字段。 */
    public void setRegistrarId(Long value) {
        this.registrarId = value;
    }

    /** 读取创建时间。 */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** 设置创建时间。 */
    public void setCreatedAt(LocalDateTime value) {
        this.createdAt = value;
    }
}
