package edu.gengdan.gdpm.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 标记成功后需记录操作审计的业务方法。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {
    /** 返回成功操作的中文动作名称，不允许包含请求体或敏感凭据。 */
    String value();
}
