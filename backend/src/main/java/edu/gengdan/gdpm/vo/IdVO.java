package edu.gengdan.gdpm.vo;



/** 成功写入的业务记录编号。 */
public class IdVO {

    public final Long id;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public IdVO(Long id) {
        this.id = id;
    }
}
