package edu.gengdan.gdpm.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 管理员创建答辩组，成员必须为有效教师。 */
public class DefenseGroupRequest {

    @NotBlank(message = "name不能为空且长度不能超过100")
    @Size(max = 100, message = "name不能为空且长度不能超过100")
    private String name = "";

    @NotBlank(message = "location不能为空且长度不能超过100")
    @Size(max = 100, message = "location不能为空且长度不能超过100")
    private String location = "";

    @NotBlank(message = "defenseAt不能为空且长度不能超过30")
    @Size(max = 30, message = "defenseAt不能为空且长度不能超过30")
    private String defenseAt = "";

    @NotNull(message = "请选择答辩组教师")
    @Size(min = 1, message = "请选择答辩组教师")
    private List<@NotNull(message = "请选择答辩组教师") Long> teacherIds;

    /** 返回name，供服务端执行权限与业务校验。 */
    public String getName() {
        return name;
    }

    /** 绑定name并去除首尾空白。 */
    public void setName(String value) {
        this.name = value == null ? "" : value.trim();
    }

    /** 返回location，供服务端执行权限与业务校验。 */
    public String getLocation() {
        return location;
    }

    /** 绑定location并去除首尾空白。 */
    public void setLocation(String value) {
        this.location = value == null ? "" : value.trim();
    }

    /** 返回defenseAt，供服务端执行权限与业务校验。 */
    public String getDefenseAt() {
        return defenseAt;
    }

    /** 绑定defenseAt并去除首尾空白。 */
    public void setDefenseAt(String value) {
        this.defenseAt = value == null ? "" : value.trim();
    }

    /** 返回teacherIds，供服务端执行权限与业务校验。 */
    public List<@NotNull(message = "请选择答辩组教师") Long> getTeacherIds() {
        return teacherIds;
    }

    /** 绑定teacherIds。 */
    public void setTeacherIds(List<@NotNull(message = "请选择答辩组教师") Long> value) {
        this.teacherIds = value;
    }
}
