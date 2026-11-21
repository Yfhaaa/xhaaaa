package edu.gengdan.gdpm.vo;

import edu.gengdan.gdpm.dto.PageQuery;
import java.util.List;

/** 统一分页视图，服务层完成数据范围过滤之后再分页。 */
public class PageVO<T> {

    public final List<T> records;
    public final long total;
    public final int page;
    public final int size;

    public PageVO(List<T> rows, PageQuery query) {
        this.page = query.page();
        this.size = query.size();
        this.total = rows.size();
        int start = (int) Math.min(total, (long) (page - 1) * size);
        this.records = rows.subList(start, (int) Math.min(total, (long) start + size));
    }

    /** 在传入分页参数时返回统一分页结构。 */
    public static Object optional(Object value, PageQuery query) {
        if (value instanceof List && query.isPaged()) {
            return new PageVO<>((List<?>) value, query);
        }
        return value;
    }
}
