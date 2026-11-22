package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/** student_assignment 表实体。 */
@TableName("student_assignment")
public class StudentAssignment {

    /** 学生编号。 */
    @TableId(value = "student_id", type = IdType.INPUT)
    private Long studentId;

    /** 课题编号。 */
    private Long topicId;

    /** 指导教师编号。 */
    private Long teacherId;

    /** 读取学生编号。 */
    public Long getStudentId() {
        return studentId;
    }

    /** 设置学生编号。 */
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    /** 读取TopicId字段。 */
    public Long getTopicId() {
        return topicId;
    }

    /** 设置TopicId字段。 */
    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    /** 读取指导教师编号。 */
    public Long getTeacherId() {
        return teacherId;
    }

    /** 设置指导教师编号。 */
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
}
