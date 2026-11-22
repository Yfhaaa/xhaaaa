package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/** topic_change 业务记录，保留各环节身份与时间。 */
@TableName("topic_change")
public class TopicChange {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long oldTopicId;
    private Long newTopicId;
    private Long oldTeacherId;
    private Long newTeacherId;
    private String reason;
    private String status;
    private Integer approvalStep;
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

    /** 读取OldTopicId字段。 */
    public Long getOldTopicId() {
        return oldTopicId;
    }

    /** 设置OldTopicId字段。 */
    public void setOldTopicId(Long value) {
        this.oldTopicId = value;
    }

    /** 读取NewTopicId字段。 */
    public Long getNewTopicId() {
        return newTopicId;
    }

    /** 设置NewTopicId字段。 */
    public void setNewTopicId(Long value) {
        this.newTopicId = value;
    }

    /** 读取OldTeacherId字段。 */
    public Long getOldTeacherId() {
        return oldTeacherId;
    }

    /** 设置OldTeacherId字段。 */
    public void setOldTeacherId(Long value) {
        this.oldTeacherId = value;
    }

    /** 读取NewTeacherId字段。 */
    public Long getNewTeacherId() {
        return newTeacherId;
    }

    /** 设置NewTeacherId字段。 */
    public void setNewTeacherId(Long value) {
        this.newTeacherId = value;
    }

    /** 读取变更原因。 */
    public String getReason() {
        return reason;
    }

    /** 设置变更原因。 */
    public void setReason(String value) {
        this.reason = value;
    }

    /** 读取业务状态。 */
    public String getStatus() {
        return status;
    }

    /** 设置业务状态。 */
    public void setStatus(String value) {
        this.status = value;
    }

    /** 读取ApprovalStep字段。 */
    public Integer getApprovalStep() {
        return approvalStep;
    }

    /** 设置ApprovalStep字段。 */
    public void setApprovalStep(Integer value) {
        this.approvalStep = value;
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
