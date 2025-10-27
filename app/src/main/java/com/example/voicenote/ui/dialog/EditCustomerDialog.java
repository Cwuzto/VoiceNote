package com.example.voicenote.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.voicenote.R;

/**
 * EN: Dialog to edit customer's name.
 * VI: Dialog chỉnh sửa tên khách hàng.
 */
public class EditCustomerDialog extends DialogFragment {
    public interface Callback { void onDone(String name); }
    private Callback callback;

    public EditCustomerDialog setCallback(Callback cb) { this.callback = cb; return this; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_edit_customer, container, false);
        EditText edt = v.findViewById(R.id.edtCustomer);
        v.findViewById(R.id.btnOk).setOnClickListener(x -> {
            if (callback != null) callback.onDone(edt.getText().toString().trim());
            dismiss();
        });
        v.findViewById(R.id.btnCancel).setOnClickListener(x -> dismiss());
        return v;
    }
}