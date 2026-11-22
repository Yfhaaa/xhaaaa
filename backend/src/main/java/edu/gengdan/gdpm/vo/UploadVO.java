package edu.gengdan.gdpm.vo;



/** 上传完成的附件编号、原始文件名与字节数。 */
public class UploadVO {

    public final Long id;

    public final String original_name;

    public final long size_bytes;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public UploadVO(
        Long id,
        String original_name,
        long size_bytes
    ) {
        this.id = id;
        this.original_name = original_name;
        this.size_bytes = size_bytes;
    }
}
