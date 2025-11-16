// File: com/example/voicenote/ui/dialog/TimeFilterSheet.java
package com.example.voicenote.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private boolean showAllTime = true; // Mặc định là true

    /**
     * Factory method để truyền tham số
     * @param showAllTime true nếu muốn hiển thị "Toàn thời gian"
     */
    public static TimeFilterSheet newInstance(boolean showAllTime) {
        TimeFilterSheet fragment = new TimeFilterSheet();
        Bundle args = new Bundle();
        args.putBoolean("SHOW_ALL_TIME", showAllTime);
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(OnTimeSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Lấy giá trị
            showAllTime = getArguments().getBoolean("SHOW_ALL_TIME", true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet_filter_time, container, false);

        TextView tvFilterTimeAll = v.findViewById(R.id.tvFilterTimeAll);

        // Ẩn/hiện "Toàn thời gian" dựa trên tham số
        tvFilterTimeAll.setVisibility(showAllTime ? View.VISIBLE : View.GONE);

        tvFilterTimeAll.setOnClickListener(view -> {
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
        v.findViewById(R.id.tvFilterTimeThisYear).setOnClickListener(view -> {
            listener.onTimeSelected("THIS_YEAR", "Năm nay");
            dismiss();
        });
        v.findViewById(R.id.tvFilterTimeCustom).setOnClickListener(view -> {
            listener.onCustomRangeClicked();
            dismiss();
        });

        return v;
    }
}