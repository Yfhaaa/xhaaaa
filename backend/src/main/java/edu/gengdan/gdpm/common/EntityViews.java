package edu.gengdan.gdpm.common;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** 将原生实体转换为兼容既有页面的下划线字段视图。 */
public final class EntityViews {

    private EntityViews() {}

    /** 将实体属性转换为兼容的下划线字段视图。 */
    public static Map<String, Object> view(Object entity) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            for (PropertyDescriptor property : Introspector.getBeanInfo(
                entity.getClass(),
                Object.class
            ).getPropertyDescriptors()) {
                String key = property.getName();
                if (key.equals("password")) {
                    continue;
                }
                result.put(
                    key.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT),
                    property.getReadMethod().invoke(entity)
                );
            }
        } catch (ReflectiveOperationException | IntrospectionException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }
}
