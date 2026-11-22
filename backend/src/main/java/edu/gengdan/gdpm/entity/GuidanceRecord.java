package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

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
    private LocalDateTime guidanceAt;

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

    /** 读取正文。 */
    public String getContent() {
        return content;
    }

    /** 设置正文。 */
    public void setContent(String content) {
        this.content = content;
    }

    /** 读取GuidanceAt字段。 */
    public LocalDateTime getGuidanceAt() {
        return guidanceAt;
    }

    /** 设置GuidanceAt字段。 */
    public void setGuidanceAt(LocalDateTime guidanceAt) {
        this.guidanceAt = guidanceAt;
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
