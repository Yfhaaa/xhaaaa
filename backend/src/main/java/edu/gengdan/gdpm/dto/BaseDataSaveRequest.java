package edu.gengdan.gdpm.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 管理员维护专业、班级或届次；班级要求专业，届次限制二十字。 */
public class BaseDataSaveRequest {

    @NotBlank(message = "name不能为空且长度不能超过100")
    @Size(max = 100, message = "name不能为空且长度不能超过100")
    @Size(max = 20, message = "name不能为空且长度不能超过20", groups = Cohort.class)
    private String name = "";

    @NotNull(message = "majorId必须为正整数", groups = SchoolClass.class)
    @Min(value = 1, message = "majorId必须为正整数", groups = SchoolClass.class)
    private Long majorId;

    /** 返回name，供服务端执行权限与业务校验。 */
    public String getName() {
        return name;
    }

    /** 绑定name并去除首尾空白。 */
    public void setName(String value) {
        this.name = value == null ? "" : value.trim();
    }

    /** 返回majorId，供服务端执行权限与业务校验。 */
    public Long getMajorId() {
        return majorId;
    }

    /** 绑定majorId。 */
    public void setMajorId(Long value) {
        this.majorId = value;
    }

    /** 仅班级维护要求专业编号。 */
    public interface SchoolClass {}

    /** 仅届次名称限制二十字。 */
    public interface Cohort {}
}
