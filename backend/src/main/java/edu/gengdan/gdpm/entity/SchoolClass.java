package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** school_class 表实体。 */
@TableName("school_class")
public class SchoolClass {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 名称。 */
    private String name;
    /** 所属专业编号。 */
    private Long majorId;

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

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }
}
