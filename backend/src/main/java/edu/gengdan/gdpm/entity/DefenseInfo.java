package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/** defense_info 表实体。 */
@TableName("defense_info")
public class DefenseInfo {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学生编号。 */
    private Long studentId;

    /** 答辩组编号。 */
    private Long groupId;

    /** 业务状态。 */
    private String status;

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

    /** 读取GroupId字段。 */
    public Long getGroupId() {
        return groupId;
    }

    /** 设置GroupId字段。 */
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /** 读取业务状态。 */
    public String getStatus() {
        return status;
    }

    /** 设置业务状态。 */
    public void setStatus(String status) {
        this.status = status;
    }
}
