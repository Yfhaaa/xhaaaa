package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** workflow_step 表实体。 */
@TableName("workflow_step")
public class WorkflowStep {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 业务编码。 */
    private String code;
    /** 名称。 */
    private String name;
    /** 截止时间。 */
    private java.time.LocalDateTime deadline;
    /** 显示顺序。 */
    private Integer sortOrder;

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

    public java.time.LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(java.time.LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
