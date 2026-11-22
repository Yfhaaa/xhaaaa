package edu.gengdan.gdpm.vo;



/** 批量分配成功的学生数量。 */
public class CountVO {

    public final int count;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public CountVO(int count) {
        this.count = count;
    }
}
