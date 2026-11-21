package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** cohort 表实体。 */
@TableName("cohort")
public class Cohort {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 名称。 */
    private String name;

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
}
