package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** topic_info 表实体。 */
@TableName("topic_info")
public class TopicInfo {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 标题。 */
    private String title;
    /** 课题说明。 */
    private String description;
    /** 指导教师编号。 */
    private Long teacherId;
    /** 所属专业编号。 */
    private Long majorId;
    /** 可选人数。 */
    private Integer capacity;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
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
