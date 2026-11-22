package edu.gengdan.gdpm.vo;



/** 材料提交后的编号与历史版本号。 */
public class SubmissionVO {

    public final Long id;

    public final int version;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public SubmissionVO(
        Long id,
        int version
    ) {
        this.id = id;
        this.version = version;
    }
}
