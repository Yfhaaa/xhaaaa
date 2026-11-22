package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/** sys_session 表实体。 */
@TableName("sys_session")
public class SysSession {

    /** 令牌摘要。 */
    @TableId(value = "token_hash", type = IdType.INPUT)
    private String tokenHash;

    /** 用户编号。 */
    private Long userId;

    /** 过期时间。 */
    private LocalDateTime expiresAt;

    /** 读取令牌摘要。 */
    public String getTokenHash() {
        return tokenHash;
    }

    /** 设置令牌摘要。 */
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    /** 读取用户编号。 */
    public Long getUserId() {
        return userId;
    }

    /** 设置用户编号。 */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /** 读取会话过期时间。 */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /** 设置会话过期时间。 */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
