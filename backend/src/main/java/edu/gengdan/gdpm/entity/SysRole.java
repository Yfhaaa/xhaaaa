package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

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

    /** 读取编号。 */
    public Long getId() {
        return id;
    }

    /** 设置编号。 */
    public void setId(Long id) {
        this.id = id;
    }

    /** 读取业务编码。 */
    public String getCode() {
        return code;
    }

    /** 设置业务编码。 */
    public void setCode(String code) {
        this.code = code;
    }

    /** 读取名称。 */
    public String getName() {
        return name;
    }

    /** 设置名称。 */
    public void setName(String name) {
        this.name = name;
    }
}
