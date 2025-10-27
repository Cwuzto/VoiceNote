package com.example.voicenote.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * EN: Utility class for formatting and parsing currency values.
 * VI: Lớp tiện ích để định dạng và phân tích giá trị tiền tệ.
 */
public class FormatUtils {

    /**
     * EN: Format a number into currency format (e.g., 12,500 -> "12,500").
     * VI: Định dạng số thành kiểu tiền tệ (ví dụ: 12,500 -> "12,500").
     */
    public static String money(long value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value);
    }

    /**
     * EN: Parse a formatted money string back to long (e.g., "12,500" -> 12500).
     * VI: Chuyển chuỗi tiền tệ về kiểu long (ví dụ: "12,500" -> 12500).
     */
    public static long parseMoney(String text) {
        try {
            return NumberFormat.getNumberInstance(Locale.US).parse(text.replaceAll("[^0-9,]", "")).longValue();
        } catch (ParseException e) {
            return 0;
        }
    }
}