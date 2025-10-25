package com.example.voicenote.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.voicenote.R;

public class EditCustomerDialog extends DialogFragment {

    public interface Callback { void onDone(String name); }

    private static final String ARG_NAME = "arg_name";
    private Callback callback;

    public static EditCustomerDialog newInstance(String currentName) {
        EditCustomerDialog d = new EditCustomerDialog();
        Bundle b = new Bundle();
        b.putString(ARG_NAME, currentName);
        d.setArguments(b);
        return d;
    }

    public EditCustomerDialog setCallback(Callback cb) {
        this.callback = cb;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.dialog_edit_customer, parent, false);
        EditText edt = v.findViewById(R.id.edtCustomer);
        edt.setText(getArguments() != null ? getArguments().getString(ARG_NAME, "") : "");

        v.findViewById(R.id.btnCancel).setOnClickListener(x -> dismiss());
        v.findViewById(R.id.btnOk).setOnClickListener(x -> {
            if (callback != null) callback.onDone(edt.getText().toString().trim());
            dismiss();
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {
            Window w = d.getWindow();
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
