package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

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

    /** 读取专业编号。 */
    public Long getMajorId() {
        return majorId;
    }

    /** 设置专业编号。 */
    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }
}
