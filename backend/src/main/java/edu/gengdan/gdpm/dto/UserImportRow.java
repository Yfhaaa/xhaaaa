package edu.gengdan.gdpm.dto;

import com.alibaba.excel.annotation.ExcelProperty;

/** 用户导入模板；密码列只读取不回传。 */
public class UserImportRow {

    @ExcelProperty("账号")
    private String username;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("初始密码")
    private String password;

    @ExcelProperty("角色编号")
    private String roleId;

    @ExcelProperty("专业编号")
    private String majorId;

    @ExcelProperty("班级编号")
    private String classId;

    @ExcelProperty("届次")
    private String cohort;

    /** 读取登录账号。 */
    public String getUsername() {
        return username;
    }

    /** 设置登录账号。 */
    public void setUsername(String value) {
        this.username = value;
    }

    /** 读取名称。 */
    public String getName() {
        return name;
    }

    /** 设置名称。 */
    public void setName(String value) {
        this.name = value;
    }

    /** 读取密码哈希。密码相关值不得进入响应或操作日志。 */
    public String getPassword() {
        return password;
    }

    /** 设置密码哈希。密码相关值不得进入响应或操作日志。 */
    public void setPassword(String value) {
        this.password = value;
    }

    /** 读取角色编号。 */
    public String getRoleId() {
        return roleId;
    }

    /** 设置角色编号。 */
    public void setRoleId(String value) {
        this.roleId = value;
    }

    /** 读取专业编号。 */
    public String getMajorId() {
        return majorId;
    }

    /** 设置专业编号。 */
    public void setMajorId(String value) {
        this.majorId = value;
    }

    /** 读取班级编号。 */
    public String getClassId() {
        return classId;
    }

    /** 设置班级编号。 */
    public void setClassId(String value) {
        this.classId = value;
    }

    /** 读取毕业届次。 */
    public String getCohort() {
        return cohort;
    }

    /** 设置毕业届次。 */
    public void setCohort(String value) {
        this.cohort = value;
    }
}
