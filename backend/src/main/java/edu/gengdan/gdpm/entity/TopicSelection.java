package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** topic_selection 表实体。 */
@TableName("topic_selection")
public class TopicSelection {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 课题编号。 */
    private Long topicId;
    /** 学生编号。 */
    private Long studentId;
    /** 业务状态。 */
    private String status;
    /** 提交时间。 */
    private java.time.LocalDateTime submittedAt;
    /** 审核人编号。 */
    private Long reviewerId;
    /** 审核意见。 */
    private String reviewComment;
    /** 修改次数。 */
    private Integer revisionCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.time.LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(java.time.LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public Integer getRevisionCount() {
        return revisionCount;
    }

    public void setRevisionCount(Integer revisionCount) {
        this.revisionCount = revisionCount;
    }
}
