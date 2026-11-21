package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** defense_group 表实体。 */
@TableName("defense_group")
public class DefenseGroup {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 名称。 */
    private String name;
    /** 答辩教师编号列表。 */
    private String teacherIds;
    /** 答辩地点。 */
    private String location;
    /** 答辩时间。 */
    private java.time.LocalDateTime defenseAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacherIds() {
        return teacherIds;
    }

    public void setTeacherIds(String teacherIds) {
        this.teacherIds = teacherIds;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public java.time.LocalDateTime getDefenseAt() {
        return defenseAt;
    }

    public void setDefenseAt(java.time.LocalDateTime defenseAt) {
        this.defenseAt = defenseAt;
    }
}
