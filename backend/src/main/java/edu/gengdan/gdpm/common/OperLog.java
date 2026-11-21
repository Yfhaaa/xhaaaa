package edu.gengdan.gdpm.common;

import java.lang.annotation.*;

/** 标记成功后需记录操作审计的业务方法。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {
    String value();
}
