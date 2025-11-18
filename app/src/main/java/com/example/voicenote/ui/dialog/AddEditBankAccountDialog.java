// File: com/example/voicenote/ui/dialog/AddEditBankAccountDialog.java
package com.example.voicenote.ui.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.BankAccountEntity;

public class AddEditBankAccountDialog extends DialogFragment {

    public interface OnSaveListener {
        void onSave(BankAccountEntity account);
    }

    private BankAccountEntity existingAccount;
    private OnSaveListener onSaveListener;

    private EditText edtBankName, edtAccountNumber, edtAccountHolder, edtQrTemplate;
    private CheckBox cbDefault;
    private TextView tvDialogTitle;

    public static AddEditBankAccountDialog newInstance(BankAccountEntity account) {
        AddEditBankAccountDialog dialog = new AddEditBankAccountDialog();
        Bundle args = new Bundle();
        if (account != null) {
            args.putLong("id", account.id);
            args.putString("bankName", account.bankName);
            args.putString("accountNumber", account.accountNumber);
            args.putString("accountHolder", account.accountHolderName);
            args.putString("qrTemplate", account.qrTemplate);
            args.putBoolean("isDefault", account.isDefault);
        }
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnSaveListener(OnSaveListener listener) {
        this.onSaveListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("id")) {
            existingAccount = new BankAccountEntity();
            existingAccount.id = getArguments().getLong("id");
            existingAccount.bankName = getArguments().getString("bankName");
            existingAccount.accountNumber = getArguments().getString("accountNumber");
            existingAccount.accountHolderName = getArguments().getString("accountHolder");
            existingAccount.qrTemplate = getArguments().getString("qrTemplate");
            existingAccount.isDefault = getArguments().getBoolean("isDefault");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window w = getDialog().getWindow();
            w.setBackgroundDrawableResource(R.drawable.bg_dialog_rounded);
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.92);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            w.setAttributes(lp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_add_bank_account, container, false);

        tvDialogTitle = v.findViewById(R.id.tvDialogTitle);
        edtBankName = v.findViewById(R.id.edtBankName);
        edtAccountNumber = v.findViewById(R.id.edtAccountNumber);
        edtAccountHolder = v.findViewById(R.id.edtAccountHolder);
        edtQrTemplate = v.findViewById(R.id.edtQrTemplate);
        cbDefault = v.findViewById(R.id.cbDefault);

        if (existingAccount != null) {
            tvDialogTitle.setText("Sửa tài khoản");
            edtBankName.setText(existingAccount.bankName);
            edtAccountNumber.setText(existingAccount.accountNumber);
            edtAccountHolder.setText(existingAccount.accountHolderName);
            edtQrTemplate.setText(existingAccount.qrTemplate);
            cbDefault.setChecked(existingAccount.isDefault);
        }

        v.findViewById(R.id.btnCancel).setOnClickListener(view -> dismiss());
        v.findViewById(R.id.btnSave).setOnClickListener(view -> save());

        return v;
    }

    private void save() {
        String bankName = edtBankName.getText().toString().trim();
        String accNum = edtAccountNumber.getText().toString().trim();
        String accHolder = edtAccountHolder.getText().toString().trim();
        String qrTemp = edtQrTemplate.getText().toString().trim();

        if (bankName.isEmpty()) { edtBankName.setError("Nhập tên ngân hàng"); return; }
        if (accNum.isEmpty()) { edtAccountNumber.setError("Nhập số tài khoản"); return; }
        if (accHolder.isEmpty()) { edtAccountHolder.setError("Nhập tên chủ TK"); return; }

        if (existingAccount == null) {
            existingAccount = new BankAccountEntity();
        }

        existingAccount.bankName = bankName;
        existingAccount.accountNumber = accNum;
        existingAccount.accountHolderName = accHolder;
        existingAccount.qrTemplate = qrTemp.isEmpty() ? "compact" : qrTemp;
        existingAccount.isDefault = cbDefault.isChecked();

        if (onSaveListener != null) {
            onSaveListener.onSave(existingAccount);
        }
        dismiss();
    }
}