package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

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

    /** 读取标题。 */
    public String getTitle() {
        return title;
    }

    /** 设置标题。 */
    public void setTitle(String title) {
        this.title = title;
    }

    /** 读取课题说明。 */
    public String getDescription() {
        return description;
    }

    /** 设置课题说明。 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** 读取指导教师编号。 */
    public Long getTeacherId() {
        return teacherId;
    }

    /** 设置指导教师编号。 */
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    /** 读取专业编号。 */
    public Long getMajorId() {
        return majorId;
    }

    /** 设置专业编号。 */
    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }

    /** 读取课题容量。 */
    public Integer getCapacity() {
        return capacity;
    }

    /** 设置课题容量。 */
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
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
