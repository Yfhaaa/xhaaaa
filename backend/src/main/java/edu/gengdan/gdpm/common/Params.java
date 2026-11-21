package edu.gengdan.gdpm.common;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/** 请求参数业务校验。 */
public final class Params {

    /** 校验必填文本和最大长度。 */
    public static String text(Map<String, Object> m, String key, int max) {
        String s = Values.str(m, key);
        ApiException.require(!s.isEmpty() && s.length() <= max, key + "不能为空且长度不能超过" + max);
        return s;
    }

    /** 读取并校验正整数参数。 */
    public static long number(Map<String, Object> m, String key) {
        long n = Values.id(m.get(key));
        ApiException.require(n > 0, key + "必须为正整数");
        return n;
    }

    /** 读取严格布尔值审核结果。 */
    public static boolean approved(Map<String, Object> m) {
        ApiException.require(m.get("approved") instanceof Boolean, "approved必须为布尔值");
        return Boolean.TRUE.equals(m.get("approved"));
    }

    /** 将请求时间转换为数据库时间。 */
    public static Timestamp time(Map<String, Object> m, String key) {
        return Timestamp.valueOf(LocalDateTime.parse(text(m, key, 30).replace(" ", "T")));
    }

    /** 校验十进制分数或权重的范围与精度。 */
    public static BigDecimal decimal(Map<String, Object> m, String key, BigDecimal max) {
        BigDecimal n = new BigDecimal(String.valueOf(m.get(key)));
        ApiException.require(n.scale() <= 2, key + "最多保留两位小数");
        ApiException.require(n.signum() >= 0 && n.compareTo(max) <= 0, key + "超出范围");
        return n;
    }
}
