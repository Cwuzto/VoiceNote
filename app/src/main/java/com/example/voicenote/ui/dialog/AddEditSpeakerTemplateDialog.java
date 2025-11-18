// File: com/example/voicenote/ui/dialog/AddEditSpeakerTemplateDialog.java
package com.example.voicenote.ui.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.SpeakerTemplateEntity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddEditSpeakerTemplateDialog extends BottomSheetDialogFragment {

    public interface OnSaveListener {
        void onSave(SpeakerTemplateEntity template);
    }

    public interface OnTestListener {
        void onTest(String text);
    }

    private SpeakerTemplateEntity existingItem;
    private OnSaveListener listener;
    private OnTestListener testListener; // Để nghe thử

    public static AddEditSpeakerTemplateDialog newInstance(SpeakerTemplateEntity item, OnSaveListener listener, OnTestListener testListener) {
        AddEditSpeakerTemplateDialog dialog = new AddEditSpeakerTemplateDialog();
        dialog.existingItem = item;
        dialog.listener = listener;
        dialog.testListener = testListener;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_add_speaker_template, container, false);

        TextView tvTitle = v.findViewById(R.id.tvDialogTitle);
        EditText edtContent = v.findViewById(R.id.edtContent);
        TextView tvCounter = v.findViewById(R.id.tvCounter);
        ImageView btnClear = v.findViewById(R.id.btnClear);
        ImageView btnTestSound = v.findViewById(R.id.btnTestSound);
        TextView btnSave = v.findViewById(R.id.btnSave);
        TextView chipInsertMoney = v.findViewById(R.id.chipInsertMoney);

        if (existingItem != null) {
            tvTitle.setText("Sửa nội dung loa");
            edtContent.setText(existingItem.content);
        }

        edtContent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCounter.setText(s.length() + "/50 ký tự");
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnClear.setOnClickListener(view -> edtContent.setText(""));

        // Logic CHÈN TAG {Số tiền} vào vị trí con trỏ
        chipInsertMoney.setOnClickListener(view -> {
            int start = Math.max(edtContent.getSelectionStart(), 0);
            int end = Math.max(edtContent.getSelectionEnd(), 0);
            edtContent.getText().replace(Math.min(start, end), Math.max(start, end), "{Số tiền}", 0, "{Số tiền}".length());
        });

        // Nghe thử ngay trong dialog
        btnTestSound.setOnClickListener(view -> {
            if (testListener != null) testListener.onTest(edtContent.getText().toString());
        });

        btnSave.setOnClickListener(view -> {
            String content = edtContent.getText().toString().trim();
            if (content.isEmpty()) return;

            if (existingItem == null) {
                existingItem = new SpeakerTemplateEntity();
                existingItem.isSelected = false;
            }
            existingItem.content = content;

            if (listener != null) listener.onSave(existingItem);
            dismiss();
        });

        return v;
    }
}