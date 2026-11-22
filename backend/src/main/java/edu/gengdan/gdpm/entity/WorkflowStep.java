package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private LocalDateTime deadline;

    /** 显示顺序。 */
    private Integer sortOrder;

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

    /** 读取截止时间。 */
    public LocalDateTime getDeadline() {
        return deadline;
    }

    /** 设置截止时间。 */
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    /** 读取流程顺序。 */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /** 设置流程顺序。 */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /** 可维护的个人资料或流程配置。 */
    private BigDecimal plagiarismThreshold;

    /** 读取PlagiarismThreshold字段。 */
    public BigDecimal getPlagiarismThreshold() {
        return plagiarismThreshold;
    }

    /** 设置PlagiarismThreshold字段。 */
    public void setPlagiarismThreshold(BigDecimal value) {
        this.plagiarismThreshold = value;
    }
}
