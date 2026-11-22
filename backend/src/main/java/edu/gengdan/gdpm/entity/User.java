package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** sys_user 表实体。 */
@TableName("sys_user")
public class User {

    /** 主键编号。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 登录账号。 */
    private String username;

    /** 密码哈希。 */
    @JsonIgnore
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

    /** 读取编号。 */
    public Long getId() {
        return id;
    }

    /** 设置编号。 */
    public void setId(Long id) {
        this.id = id;
    }

    /** 读取登录账号。 */
    public String getUsername() {
        return username;
    }

    /** 设置登录账号。 */
    public void setUsername(String username) {
        this.username = username;
    }

    /** 读取密码哈希。密码相关值不得进入响应或操作日志。 */
    public String getPassword() {
        return password;
    }

    /** 设置密码哈希。密码相关值不得进入响应或操作日志。 */
    public void setPassword(String password) {
        this.password = password;
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

    /** 读取班级编号。 */
    public Long getClassId() {
        return classId;
    }

    /** 设置班级编号。 */
    public void setClassId(Long classId) {
        this.classId = classId;
    }

    /** 读取毕业届次。 */
    public String getCohort() {
        return cohort;
    }

    /** 设置毕业届次。 */
    public void setCohort(String cohort) {
        this.cohort = cohort;
    }

    /** 读取账号启用标记。 */
    public Integer getEnabled() {
        return enabled;
    }

    /** 设置账号启用标记。 */
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    /** 可维护的个人资料或流程配置。 */
    private String email;

    /** 读取邮箱。 */
    public String getEmail() {
        return email;
    }

    /** 设置邮箱。 */
    public void setEmail(String value) {
        this.email = value;
    }

    /** 可维护的个人资料或流程配置。 */
    private String phone;

    /** 读取联系电话。 */
    public String getPhone() {
        return phone;
    }

    /** 设置联系电话。 */
    public void setPhone(String value) {
        this.phone = value;
    }
}
