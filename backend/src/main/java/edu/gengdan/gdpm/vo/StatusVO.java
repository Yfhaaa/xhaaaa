package edu.gengdan.gdpm.vo;



/** 审批成功后的当前业务状态。 */
public class StatusVO {

    public final String status;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public StatusVO(String status) {
        this.status = status;
    }
}
