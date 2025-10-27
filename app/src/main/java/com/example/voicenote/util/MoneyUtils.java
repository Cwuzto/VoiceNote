package com.example.voicenote.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * EN: Utility class providing live money formatting in EditText.
 * VI: Lớp tiện ích hỗ trợ định dạng tiền tệ trực tiếp trong EditText.
 */
public class MoneyUtils {

    /**
     * EN: Attach a TextWatcher to format input while typing.
     * VI: Gắn TextWatcher để định dạng tiền khi người dùng nhập.
     */
    public static void attachMoneyFormatter(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String last = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                last = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                editText.removeTextChangedListener(this);
                String raw = s.toString().replaceAll("[^0-9]", "");
                if (raw.isEmpty()) raw = "0";
                try {
                    long val = Long.parseLong(raw);
                    String formatted = NumberFormat.getNumberInstance(Locale.US).format(val);
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                } catch (Exception e) {
                    editText.setText(last);
                    editText.setSelection(last.length());
                }
                editText.addTextChangedListener(this);
            }
        });
    }
}