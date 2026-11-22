package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/** review_record 表实体。 */
@TableName("review_record")
public class ReviewRecord {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联业务类型。 */
    private String businessType;

    /** 关联业务编号。 */
    private Long businessId;

    /** 学生编号。 */
    private Long studentId;

    /** 审核人编号。 */
    private Long reviewerId;

    /** 审核结果。 */
    private String result;

    /** 审核意见。 */
    private String comment;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 读取编号。 */
    public Long getId() {
        return id;
    }

    /** 设置编号。 */
    public void setId(Long id) {
        this.id = id;
    }

    /** 读取BusinessType字段。 */
    public String getBusinessType() {
        return businessType;
    }

    /** 设置BusinessType字段。 */
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    /** 读取BusinessId字段。 */
    public Long getBusinessId() {
        return businessId;
    }

    /** 设置BusinessId字段。 */
    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    /** 读取学生编号。 */
    public Long getStudentId() {
        return studentId;
    }

    /** 设置学生编号。 */
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    /** 读取审核人编号。 */
    public Long getReviewerId() {
        return reviewerId;
    }

    /** 设置审核人编号。 */
    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    /** 读取审核结果。 */
    public String getResult() {
        return result;
    }

    /** 设置审核结果。 */
    public void setResult(String result) {
        this.result = result;
    }

    /** 读取意见。 */
    public String getComment() {
        return comment;
    }

    /** 设置意见。 */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /** 读取创建时间。 */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** 设置创建时间。 */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
