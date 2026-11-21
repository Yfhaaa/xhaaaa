package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** sys_role 表实体。 */
@TableName("sys_role")
public class SysRole {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 业务编码。 */
    private String code;
    /** 名称。 */
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
