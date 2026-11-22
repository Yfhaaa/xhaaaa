package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/** sys_log 表实体。 */
@TableName("sys_log")
public class SysLog {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户编号。 */
    private Long userId;

    /** 操作名称。 */
    private String action;

    /** 操作详情。 */
    private String detail;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 读取编号。 */
    public Long getId() {
        return id;
    }

    /** 设置编号。 */
    public void setId(Long id) {
        this.id = id;
    }

    /** 读取用户编号。 */
    public Long getUserId() {
        return userId;
    }

    /** 设置用户编号。 */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /** 读取Action字段。 */
    public String getAction() {
        return action;
    }

    /** 设置Action字段。 */
    public void setAction(String action) {
        this.action = action;
    }

    /** 读取Detail字段。 */
    public String getDetail() {
        return detail;
    }

    /** 设置Detail字段。 */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /** 读取创建时间。 */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** 设置创建时间。 */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
