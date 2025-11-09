// File: com/example/voicenote/ui/dialog/TimeFilterSheet.java
package com.example.voicenote.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.voicenote.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TimeFilterSheet extends BottomSheetDialogFragment {

    public interface OnTimeSelectedListener {
        void onTimeSelected(String rangeKey, String rangeText);
        void onCustomRangeClicked();
    }

    private OnTimeSelectedListener listener;

    public void setListener(OnTimeSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet_filter_time, container, false);

        v.findViewById(R.id.tvFilterTimeAll).setOnClickListener(view -> {
            listener.onTimeSelected("ALL", "Toàn thời gian");
            dismiss();
        });
        v.findViewById(R.id.tvFilterTimeToday).setOnClickListener(view -> {
            listener.onTimeSelected("TODAY", "Hôm nay");
            dismiss();
        });
        v.findViewById(R.id.tvFilterTimeYesterday).setOnClickListener(view -> {
            listener.onTimeSelected("YESTERDAY", "Hôm qua");
            dismiss();
        });
        v.findViewById(R.id.tvFilterTime7Days).setOnClickListener(view -> {
            listener.onTimeSelected("7DAYS", "7 ngày qua");
            dismiss();
        });
        v.findViewById(R.id.tvFilterTimeThisMonth).setOnClickListener(view -> {
            listener.onTimeSelected("THIS_MONTH", "Tháng này");
            dismiss();
        });
        v.findViewById(R.id.tvFilterTimeLastMonth).setOnClickListener(view -> {
            listener.onTimeSelected("LAST_MONTH", "Tháng trước");
            dismiss();
        });
        v.findViewById(R.id.tvFilterTimeCustom).setOnClickListener(view -> {
            listener.onCustomRangeClicked();
            dismiss();
        });

        return v;
    }
}