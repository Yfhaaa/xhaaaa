package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

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
    private LocalDateTime defenseAt;

    /** 读取编号。 */
    public Long getId() {
        return id;
    }

    /** 设置编号。 */
    public void setId(Long id) {
        this.id = id;
    }

    /** 读取名称。 */
    public String getName() {
        return name;
    }

    /** 设置名称。 */
    public void setName(String name) {
        this.name = name;
    }

    /** 读取TeacherIds字段。 */
    public String getTeacherIds() {
        return teacherIds;
    }

    /** 设置TeacherIds字段。 */
    public void setTeacherIds(String teacherIds) {
        this.teacherIds = teacherIds;
    }

    /** 读取Location字段。 */
    public String getLocation() {
        return location;
    }

    /** 设置Location字段。 */
    public void setLocation(String location) {
        this.location = location;
    }

    /** 读取DefenseAt字段。 */
    public LocalDateTime getDefenseAt() {
        return defenseAt;
    }

    /** 设置DefenseAt字段。 */
    public void setDefenseAt(LocalDateTime defenseAt) {
        this.defenseAt = defenseAt;
    }
}
