package com.kq.utils;

import java.util.Objects;

public class StringUtils {
    public static boolean stringPresent(String str) {
        return Objects.nonNull(str) && !str.isBlank();
    }
}
