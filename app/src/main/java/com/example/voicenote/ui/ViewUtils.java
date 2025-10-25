package com.example.voicenote.ui;

import java.text.NumberFormat;
import java.util.Locale;

public class ViewUtils {
    public static String formatCurrency(long vnd) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(vnd);
    }
}
