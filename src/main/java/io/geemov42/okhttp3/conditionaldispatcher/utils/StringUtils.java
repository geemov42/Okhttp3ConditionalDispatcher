package io.geemov42.okhttp3.conditionaldispatcher.utils;

import static java.util.Objects.requireNonNull;

public class StringUtils {

    private StringUtils(){}

    public static String requireNonNullAndNotBlank(String value) {

        requireNonNull(value);

        if (value.isBlank()) {
            throw new IllegalArgumentException("Path should not be empty");
        }

        return value;
    }
}
