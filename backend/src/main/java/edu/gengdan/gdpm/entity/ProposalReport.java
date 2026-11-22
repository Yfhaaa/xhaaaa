package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/** proposal_report 表实体。 */
@TableName("proposal_report")
public class ProposalReport implements ProcessMaterial {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学生编号。 */
    private Long studentId;

    /** 指导教师编号。 */
    private Long teacherId;

    /** 材料阶段。 */
    private String stage;

    /** 标题。 */
    private String title;

    /** 正文内容。 */
    private String content;

    /** 文件编号。 */
    private Long fileId;

    /** 材料版本。 */
    private Integer version;

    /** 业务状态。 */
    private String status;

    /** 提交时间。 */
    private LocalDateTime submittedAt;

    /** 审核人编号。 */
    private Long reviewerId;

    /** 审核意见。 */
    private String reviewComment;

    /** 审核时间。 */
    private LocalDateTime reviewedAt;

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

    /** 读取学生编号。 */
    public Long getStudentId() {
        return studentId;
    }

    /** 设置学生编号。 */
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    /** 读取指导教师编号。 */
    public Long getTeacherId() {
        return teacherId;
    }

    /** 设置指导教师编号。 */
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    /** 读取材料阶段。 */
    public String getStage() {
        return stage;
    }

    /** 设置材料阶段。 */
    public void setStage(String stage) {
        this.stage = stage;
    }

    /** 读取标题。 */
    public String getTitle() {
        return title;
    }

    /** 设置标题。 */
    public void setTitle(String title) {
        this.title = title;
    }

    /** 读取正文。 */
    public String getContent() {
        return content;
    }

    /** 设置正文。 */
    public void setContent(String content) {
        this.content = content;
    }

    /** 读取附件编号。 */
    public Long getFileId() {
        return fileId;
    }

    /** 设置附件编号。 */
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    /** 读取材料版本。 */
    public Integer getVersion() {
        return version;
    }

    /** 设置材料版本。 */
    public void setVersion(Integer version) {
        this.version = version;
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

    /** 读取审核时间。 */
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    /** 设置审核时间。 */
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
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
