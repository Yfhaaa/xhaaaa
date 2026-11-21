package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** score_info 表实体。 */
@TableName("score_info")
public class ScoreInfo {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学生编号。 */
    private Long studentId;
    /** 指导成绩。 */
    private java.math.BigDecimal guidanceScore;
    /** 评阅成绩。 */
    private java.math.BigDecimal reviewScore;
    /** 答辩成绩。 */
    private java.math.BigDecimal defenseScore;
    /** 指导成绩权重。 */
    private java.math.BigDecimal guidanceWeight;
    /** 评阅成绩权重。 */
    private java.math.BigDecimal reviewWeight;
    /** 答辩成绩权重。 */
    private java.math.BigDecimal defenseWeight;
    /** 综合成绩。 */
    private java.math.BigDecimal totalScore;
    /** 成绩发布标记。 */
    private Integer published;
    /** 指导教师编号。 */
    private Long teacherId;
    /** 更新时间。 */
    private java.time.LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public java.math.BigDecimal getGuidanceScore() {
        return guidanceScore;
    }

    public void setGuidanceScore(java.math.BigDecimal guidanceScore) {
        this.guidanceScore = guidanceScore;
    }

    public java.math.BigDecimal getReviewScore() {
        return reviewScore;
    }

    public void setReviewScore(java.math.BigDecimal reviewScore) {
        this.reviewScore = reviewScore;
    }

    public java.math.BigDecimal getDefenseScore() {
        return defenseScore;
    }

    public void setDefenseScore(java.math.BigDecimal defenseScore) {
        this.defenseScore = defenseScore;
    }

    public java.math.BigDecimal getGuidanceWeight() {
        return guidanceWeight;
    }

    public void setGuidanceWeight(java.math.BigDecimal guidanceWeight) {
        this.guidanceWeight = guidanceWeight;
    }

    public java.math.BigDecimal getReviewWeight() {
        return reviewWeight;
    }

    public void setReviewWeight(java.math.BigDecimal reviewWeight) {
        this.reviewWeight = reviewWeight;
    }

    public java.math.BigDecimal getDefenseWeight() {
        return defenseWeight;
    }

    public void setDefenseWeight(java.math.BigDecimal defenseWeight) {
        this.defenseWeight = defenseWeight;
    }

    public java.math.BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(java.math.BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getPublished() {
        return published;
    }

    public void setPublished(Integer published) {
        this.published = published;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
