package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/** notice 表实体。 */
@TableName("notice")
public class Notice {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 标题。 */
    private String title;

    /** 正文内容。 */
    private String content;

    /** 发布人编号。 */
    private Long publisherId;

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

    /** 读取标题。 */
    public String getTitle() {
        return title;
    }

    /** 设置标题。 */
    public void setTitle(String title) {
        this.title = title;
    }

    /** 读取正文。 */
    public String getContent() {
        return content;
    }

    /** 设置正文。 */
    public void setContent(String content) {
        this.content = content;
    }

    /** 读取PublisherId字段。 */
    public Long getPublisherId() {
        return publisherId;
    }

    /** 设置PublisherId字段。 */
    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
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
