package com.example.voicenote.utils;

import java.text.DecimalFormat;

public class FormatUtils {
    private static final DecimalFormat DF = new DecimalFormat("#,###");

    public static String money(long value) {
        return DF.format(value);
    }

    public static long parseMoney(String s) {
        try {
            return Long.parseLong(s.replace(",", "").trim());
        } catch (Exception e) {
            return 0L;
        }
    }
}
