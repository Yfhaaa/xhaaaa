package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/** sys_message 业务记录，保留各环节身份与时间。 */
@TableName("sys_message")
public class SysMessage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String content;
    private String path;
    private Integer readFlag;
    private String eventKey;
    private LocalDateTime createdAt;

    /** 读取编号。 */
    public Long getId() {
        return id;
    }

    /** 设置编号。 */
    public void setId(Long value) {
        this.id = value;
    }

    /** 读取用户编号。 */
    public Long getUserId() {
        return userId;
    }

    /** 设置用户编号。 */
    public void setUserId(Long value) {
        this.userId = value;
    }

    /** 读取标题。 */
    public String getTitle() {
        return title;
    }

    /** 设置标题。 */
    public void setTitle(String value) {
        this.title = value;
    }

    /** 读取正文。 */
    public String getContent() {
        return content;
    }

    /** 设置正文。 */
    public void setContent(String value) {
        this.content = value;
    }

    /** 读取消息跳转地址。 */
    public String getPath() {
        return path;
    }

    /** 设置消息跳转地址。 */
    public void setPath(String value) {
        this.path = value;
    }

    /** 读取ReadFlag字段。 */
    public Integer getReadFlag() {
        return readFlag;
    }

    /** 设置ReadFlag字段。 */
    public void setReadFlag(Integer value) {
        this.readFlag = value;
    }

    /** 读取EventKey字段。 */
    public String getEventKey() {
        return eventKey;
    }

    /** 设置EventKey字段。 */
    public void setEventKey(String value) {
        this.eventKey = value;
    }

    /** 读取创建时间。 */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** 设置创建时间。 */
    public void setCreatedAt(LocalDateTime value) {
        this.createdAt = value;
    }
}
