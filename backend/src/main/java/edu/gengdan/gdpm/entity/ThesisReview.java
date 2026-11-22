package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** thesis_review 业务记录，保留各环节身份与时间。 */
@TableName("thesis_review")
public class ThesisReview {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long reviewerId;
    private Long thesisId;
    private String status;
    private String comment;
    private BigDecimal score;
    private Integer agreeDefense;
    private LocalDateTime assignedAt;
    private LocalDateTime reviewedAt;

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

    /** 读取审核人编号。 */
    public Long getReviewerId() {
        return reviewerId;
    }

    /** 设置审核人编号。 */
    public void setReviewerId(Long value) {
        this.reviewerId = value;
    }

    /** 读取ThesisId字段。 */
    public Long getThesisId() {
        return thesisId;
    }

    /** 设置ThesisId字段。 */
    public void setThesisId(Long value) {
        this.thesisId = value;
    }

    /** 读取业务状态。 */
    public String getStatus() {
        return status;
    }

    /** 设置业务状态。 */
    public void setStatus(String value) {
        this.status = value;
    }

    /** 读取意见。 */
    public String getComment() {
        return comment;
    }

    /** 设置意见。 */
    public void setComment(String value) {
        this.comment = value;
    }

    /** 读取评阅分。 */
    public BigDecimal getScore() {
        return score;
    }

    /** 设置评阅分。 */
    public void setScore(BigDecimal value) {
        this.score = value;
    }

    /** 读取AgreeDefense字段。 */
    public Integer getAgreeDefense() {
        return agreeDefense;
    }

    /** 设置AgreeDefense字段。 */
    public void setAgreeDefense(Integer value) {
        this.agreeDefense = value;
    }

    /** 读取AssignedAt字段。 */
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    /** 设置AssignedAt字段。 */
    public void setAssignedAt(LocalDateTime value) {
        this.assignedAt = value;
    }

    /** 读取审核时间。 */
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    /** 设置审核时间。 */
    public void setReviewedAt(LocalDateTime value) {
        this.reviewedAt = value;
    }
}
