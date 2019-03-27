package br.com.poc.ingestion.util;

public abstract class StringUtil {

    public static boolean isEmpty(String str) {
        return (str == null) || "".equals(str.trim());
    }

    public static boolean isNotEmpty(String str) {
        return (str != null) && !"".equals(str.trim());
    }

}
