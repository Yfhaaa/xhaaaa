package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** score_info 表实体。 */
@TableName("score_info")
public class ScoreInfo {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学生编号。 */
    private Long studentId;

    /** 指导成绩。 */
    private BigDecimal guidanceScore;

    /** 评阅成绩。 */
    private BigDecimal reviewScore;

    /** 答辩成绩。 */
    private BigDecimal defenseScore;

    /** 指导成绩权重。 */
    private BigDecimal guidanceWeight;

    /** 评阅成绩权重。 */
    private BigDecimal reviewWeight;

    /** 答辩成绩权重。 */
    private BigDecimal defenseWeight;

    /** 综合成绩。 */
    private BigDecimal totalScore;

    /** 成绩发布标记。 */
    private Integer published;

    /** 指导教师编号。 */
    private Long teacherId;

    /** 更新时间。 */
    private LocalDateTime updatedAt;

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

    /** 读取指导分。 */
    public BigDecimal getGuidanceScore() {
        return guidanceScore;
    }

    /** 设置指导分。 */
    public void setGuidanceScore(BigDecimal guidanceScore) {
        this.guidanceScore = guidanceScore;
    }

    /** 读取评阅分。 */
    public BigDecimal getReviewScore() {
        return reviewScore;
    }

    /** 设置评阅分。 */
    public void setReviewScore(BigDecimal reviewScore) {
        this.reviewScore = reviewScore;
    }

    /** 读取答辩分。 */
    public BigDecimal getDefenseScore() {
        return defenseScore;
    }

    /** 设置答辩分。 */
    public void setDefenseScore(BigDecimal defenseScore) {
        this.defenseScore = defenseScore;
    }

    /** 读取指导分权重。 */
    public BigDecimal getGuidanceWeight() {
        return guidanceWeight;
    }

    /** 设置指导分权重。 */
    public void setGuidanceWeight(BigDecimal guidanceWeight) {
        this.guidanceWeight = guidanceWeight;
    }

    /** 读取评阅分权重。 */
    public BigDecimal getReviewWeight() {
        return reviewWeight;
    }

    /** 设置评阅分权重。 */
    public void setReviewWeight(BigDecimal reviewWeight) {
        this.reviewWeight = reviewWeight;
    }

    /** 读取答辩分权重。 */
    public BigDecimal getDefenseWeight() {
        return defenseWeight;
    }

    /** 设置答辩分权重。 */
    public void setDefenseWeight(BigDecimal defenseWeight) {
        this.defenseWeight = defenseWeight;
    }

    /** 读取总成绩。 */
    public BigDecimal getTotalScore() {
        return totalScore;
    }

    /** 设置总成绩。 */
    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    /** 读取成绩发布标记。 */
    public Integer getPublished() {
        return published;
    }

    /** 设置成绩发布标记。 */
    public void setPublished(Integer published) {
        this.published = published;
    }

    /** 读取指导教师编号。 */
    public Long getTeacherId() {
        return teacherId;
    }

    /** 设置指导教师编号。 */
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    /** 读取更新时间。 */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** 设置更新时间。 */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
