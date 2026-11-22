package edu.gengdan.gdpm.dto;

import edu.gengdan.gdpm.common.ApiException;

/** 所有列表的可选分页请求，未传分页时保留原有完整列表。 */
public class PageQuery {

    private Integer page;
    private Integer size;

    /** 读取页码。 */
    public Integer getPage() {
        return page;
    }

    /** 设置页码。 */
    public void setPage(Integer page) {
        this.page = page;
    }

    /** 读取每页条数。 */
    public Integer getSize() {
        return size;
    }

    /** 设置每页条数。 */
    public void setSize(Integer size) {
        this.size = size;
    }

    /** 判断是否显式请求分页。 */
    public boolean isPaged() {
        return page != null || size != null;
    }

    /** 校验并返回当前页码。 */
    public int page() {
        int value = page == null ? 1 : page;
        ApiException.require(value > 0, "page必须大于0");
        return value;
    }

    /** 校验并返回受限的每页记录数。 */
    public int size() {
        int value = size == null ? 20 : size;
        ApiException.require(value > 0 && value <= 200, "size必须在1至200之间");
        return value;
    }
}
