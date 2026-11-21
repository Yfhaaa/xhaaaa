package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

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

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
}
