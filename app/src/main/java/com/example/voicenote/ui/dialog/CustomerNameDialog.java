package com.example.voicenote.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable; // [MỚI]
import android.text.TextWatcher; // [MỚI]
import android.view.Gravity;
import android.view.View; // [MỚI]
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton; // [MỚI]

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.voicenote.R;

/**
 * EN: Dialog to edit customer's name.
 * VI: Dialog chỉnh sửa tên khách hàng.
 */
public class CustomerNameDialog extends DialogFragment {

    public interface Callback {
        void onDone(String name);
    }

    private static final String ARG_NAME = "arg_name";
    private Callback callback;
    private final String PLACEHOLDER_TEXT = "Khách hàng, phòng bàn..."; // [MỚI]

    public static CustomerNameDialog newInstance(String currentName) {
        CustomerNameDialog d = new CustomerNameDialog();
        Bundle b = new Bundle();
        b.putString(ARG_NAME, currentName);
        d.setArguments(b);
        return d;
    }

    public CustomerNameDialog setCallback(Callback cb) {
        this.callback = cb;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog d = new Dialog(requireContext(), R.style.Dialog_Rounded_Overlay);
        d.setContentView(R.layout.dialog_customer_name);

        // Khai báo final để dùng trong listener
        final EditText edtCustomer = d.findViewById(R.id.edtCustomer);
        final ImageButton btnClearText = d.findViewById(R.id.btnClearText);
        View btnCancel = d.findViewById(R.id.btnCancel);
        View btnOk = d.findViewById(R.id.btnOk);

        btnCancel.setOnClickListener(v -> dismiss());

        btnOk.setOnClickListener(v -> {
            //  Đã có tham chiếu edtCustomer
            if (callback != null) {
                callback.onDone(edtCustomer.getText().toString().trim());
            }
            dismiss();
        });

        //  Thêm listener cho nút xoá text
        btnClearText.setOnClickListener(v -> edtCustomer.setText(""));

        // --- Xử lý logic ban đầu ---
        String currentName = getArguments() != null ? getArguments().getString(ARG_NAME, "") : "";

        //  Kiểm tra text mặc định
        if (PLACEHOLDER_TEXT.equals(currentName)) {
            edtCustomer.setText("");
        } else {
            edtCustomer.setText(currentName);
        }

        // Thêm TextWatcher để ẩn/hiện nút 'X'
        edtCustomer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nếu có text (s.length() > 0) thì VISIBLE, ngược lại GONE
                btnClearText.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần
            }
        });

        // Kích hoạt 1 lần lúc mở để set trạng thái ban đầu cho nút 'X'
        btnClearText.setVisibility(edtCustomer.getText().length() > 0 ? View.VISIBLE : View.GONE);

        return d;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window w = dialog.getWindow();
            w.setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.92f),
                    WindowManager.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.CENTER);
        }
    }
}