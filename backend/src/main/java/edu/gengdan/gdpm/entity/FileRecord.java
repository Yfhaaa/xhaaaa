package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/** file_record 表实体。 */
@TableName("file_record")
public class FileRecord {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 上传者编号。 */
    private Long ownerId;

    /** 原始文件名。 */
    private String originalName;

    /** 存储文件名。 */
    private String storedName;

    /** 文件字节数。 */
    private Long sizeBytes;

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

    /** 读取OwnerId字段。 */
    public Long getOwnerId() {
        return ownerId;
    }

    /** 设置OwnerId字段。 */
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    /** 读取OriginalName字段。 */
    public String getOriginalName() {
        return originalName;
    }

    /** 设置OriginalName字段。 */
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    /** 读取StoredName字段。 */
    public String getStoredName() {
        return storedName;
    }

    /** 设置StoredName字段。 */
    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    /** 读取SizeBytes字段。 */
    public Long getSizeBytes() {
        return sizeBytes;
    }

    /** 设置SizeBytes字段。 */
    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
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
