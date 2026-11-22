package edu.gengdan.gdpm.dto;



/** 服务端生成的数据范围，禁止从 HTTP 请求绑定。 */
public class DataScope {

    public final boolean admin;
    public final boolean student;
    public final long userId;

    /** 装配已配置的依赖或已验证的数据，供所属业务使用。 */
    public DataScope(boolean admin, boolean student, long userId) {
        this.admin = admin;
        this.student = student;
        this.userId = userId;
    }
}
