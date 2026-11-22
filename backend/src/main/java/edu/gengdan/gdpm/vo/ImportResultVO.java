package edu.gengdan.gdpm.vo;

import java.util.List;
import java.util.Map;

/** 逐行导入结果；不含密码和用户令牌。 */
public class ImportResultVO {

    public final int success;

    public final int failed;

    public final List<Map<String, Object>> rows;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public ImportResultVO(
        int success,
        int failed,
        List<Map<String, Object>> rows
    ) {
        this.success = success;
        this.failed = failed;
        this.rows = rows;
    }
}
