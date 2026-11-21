package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** sys_session 表实体。 */
@TableName("sys_session")
public class SysSession {

    /** 令牌摘要。 */
    @TableId(value = "token_hash", type = IdType.INPUT)
    private String tokenHash;

    /** 用户编号。 */
    private Long userId;
    /** 过期时间。 */
    private java.time.LocalDateTime expiresAt;

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public java.time.LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(java.time.LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
