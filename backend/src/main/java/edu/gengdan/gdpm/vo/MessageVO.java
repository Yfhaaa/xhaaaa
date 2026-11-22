package edu.gengdan.gdpm.vo;



/** 操作成功的兼容中文提示。 */
public class MessageVO {

    public final String message;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public MessageVO(String message) {
        this.message = message;
    }
}
