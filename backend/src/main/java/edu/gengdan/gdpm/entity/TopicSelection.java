package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

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
    private LocalDateTime submittedAt;

    /** 审核人编号。 */
    private Long reviewerId;

    /** 审核意见。 */
    private String reviewComment;

    /** 修改次数。 */
    private Integer revisionCount;

    /** 读取编号。 */
    public Long getId() {
        return id;
    }

    /** 设置编号。 */
    public void setId(Long id) {
        this.id = id;
    }

    /** 读取TopicId字段。 */
    public Long getTopicId() {
        return topicId;
    }

    /** 设置TopicId字段。 */
    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    /** 读取学生编号。 */
    public Long getStudentId() {
        return studentId;
    }

    /** 设置学生编号。 */
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    /** 读取业务状态。 */
    public String getStatus() {
        return status;
    }

    /** 设置业务状态。 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 读取提交时间。 */
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    /** 设置提交时间。 */
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    /** 读取审核人编号。 */
    public Long getReviewerId() {
        return reviewerId;
    }

    /** 设置审核人编号。 */
    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    /** 读取审核意见。 */
    public String getReviewComment() {
        return reviewComment;
    }

    /** 设置审核意见。 */
    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    /** 读取退回修改次数。 */
    public Integer getRevisionCount() {
        return revisionCount;
    }

    /** 设置退回修改次数。 */
    public void setRevisionCount(Integer revisionCount) {
        this.revisionCount = revisionCount;
    }
}
