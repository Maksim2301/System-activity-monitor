package com.example.util;

import java.math.BigDecimal;


public final class SafeCaster {

    private SafeCaster() {}

    public static BigDecimal toBigDecimal(Object value, BigDecimal defaultVal) {
        try {
            if (value == null) return defaultVal;

            if (value instanceof BigDecimal bd) return bd;

            if (value instanceof Number n)
                return BigDecimal.valueOf(n.doubleValue());

            if (value instanceof String s)
                return new BigDecimal(s.trim());

            return defaultVal;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static Integer toInt(Object value, Integer defaultVal) {
        try {
            if (value == null) return defaultVal;

            if (value instanceof Number n)
                return n.intValue();

            if (value instanceof String s)
                return Integer.parseInt(s.trim());

            return defaultVal;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static Long toLong(Object value, Long defaultVal) {
        try {
            if (value == null) return defaultVal;

            if (value instanceof Number n)
                return n.longValue();

            if (value instanceof String s)
                return Long.parseLong(s.trim());

            return defaultVal;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static String toString(Object value, String defaultVal) {
        try {
            return (value == null) ? defaultVal : value.toString();
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
