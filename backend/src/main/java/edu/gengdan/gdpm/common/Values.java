package edu.gengdan.gdpm.common;

import java.util.*;

/** 既有下划线字段视图的值转换工具，不承担数据库访问。 */
public final class Values {

    private Values() {}

    /** 将编号转换为长整数。 */
    public static long id(Object value) {
        return Long.parseLong(String.valueOf(value));
    }

    /** 读取可选文本并去除首尾空白。 */
    public static String str(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString().trim();
    }

    /** 按传入顺序构造视图字段。 */
    public static Map<String, Object> fields(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put((String) pairs[i], pairs[i + 1]);
        }
        return result;
    }
}
