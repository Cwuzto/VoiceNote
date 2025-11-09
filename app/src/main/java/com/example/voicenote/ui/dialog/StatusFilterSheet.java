// File: com/example/voicenote/ui/dialog/StatusFilterSheet.java
package com.example.voicenote.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.voicenote.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class StatusFilterSheet extends BottomSheetDialogFragment {

    public interface OnStatusSelectedListener {
        void onStatusSelected(String status, String statusText);
    }

    private OnStatusSelectedListener listener;

    public void setListener(OnStatusSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet_filter_status, container, false);

        v.findViewById(R.id.tvFilterAll).setOnClickListener(view -> {
            listener.onStatusSelected("ALL", "Tất cả đơn");
            dismiss();
        });
        v.findViewById(R.id.tvFilterPaid).setOnClickListener(view -> {
            listener.onStatusSelected("PAID", "Đã nhận tiền");
            dismiss();
        });
        v.findViewById(R.id.tvFilterUnpaid).setOnClickListener(view -> {
            listener.onStatusSelected("UNPAID", "Chưa nhận tiền");
            dismiss();
        });

        return v;
    }
}