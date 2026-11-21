package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** guidance_record 表实体。 */
@TableName("guidance_record")
public class GuidanceRecord {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学生编号。 */
    private Long studentId;
    /** 指导教师编号。 */
    private Long teacherId;
    /** 正文内容。 */
    private String content;
    /** 指导时间。 */
    private java.time.LocalDateTime guidanceAt;
    /** 创建时间。 */
    private java.time.LocalDateTime createdAt;

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

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public java.time.LocalDateTime getGuidanceAt() {
        return guidanceAt;
    }

    public void setGuidanceAt(java.time.LocalDateTime guidanceAt) {
        this.guidanceAt = guidanceAt;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
