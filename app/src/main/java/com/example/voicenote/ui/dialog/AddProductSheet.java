package com.example.voicenote.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.voicenote.R;

/**
 * EN: Dialog to add a new product quickly.
 * VI: Dialog cho phép thêm hàng hoá mới nhanh chóng.
 */
public class AddProductSheet extends DialogFragment {
    public interface OnAdded { void onAdded(String name, long price); }
    private final OnAdded cb;

    public AddProductSheet(OnAdded cb) { this.cb = cb; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_add_product, container, false);
        EditText edtName = v.findViewById(R.id.edtName);
        EditText edtPrice = v.findViewById(R.id.edtPrice);
        TextView btnAdd = v.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(view -> {
            String name = edtName.getText().toString().trim();
            long price = Long.parseLong(edtPrice.getText().toString().replaceAll("[^0-9]", ""));
            if (!name.isEmpty()) cb.onAdded(name, price);
            dismiss();
        });
        return v;
    }
}