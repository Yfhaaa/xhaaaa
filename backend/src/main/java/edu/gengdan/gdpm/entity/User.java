package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** sys_user 表实体。 */
@TableName("sys_user")
public class User {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 登录账号。 */
    private String username;

    /** 密码哈希。 */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    /** 名称。 */
    private String name;
    /** 所属专业编号。 */
    private Long majorId;
    /** 所属班级编号。 */
    private Long classId;
    /** 毕业届次。 */
    private String cohort;
    /** 启用标记。 */
    private Integer enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getCohort() {
        return cohort;
    }

    public void setCohort(String cohort) {
        this.cohort = cohort;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
