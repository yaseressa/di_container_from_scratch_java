package com.kq.utils;

import java.util.Objects;

public class StringUtils {
    public static boolean stringPresent(String str) {
        return Objects.nonNull(str) && !str.isBlank();
    }

    public static String camelCaseConvertor(String str) {
        if (stringPresent(str)) {
            String firstLetter = str.substring(0, 1).toLowerCase();
            String rest = str.substring(1);
            return firstLetter + rest;
        }
        return str;
    }
}
