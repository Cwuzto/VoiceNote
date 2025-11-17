// File: com/example/voicenote/ui/dialog/SortFilterSheet.java
package com.example.voicenote.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.voicenote.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortFilterSheet extends BottomSheetDialogFragment {

    public interface OnSortSelectedListener {
        void onSortSelected(String sortKey, String sortText);
    }

    private OnSortSelectedListener listener;

    public void setListener(OnSortSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet_filter_sort, container, false);

        v.findViewById(R.id.tvSortByQuantity).setOnClickListener(view -> {
            listener.onSortSelected("QUANTITY", "Theo số lượng");
            dismiss();
        });
        v.findViewById(R.id.tvSortByRevenue).setOnClickListener(view -> {
            listener.onSortSelected("REVENUE", "Theo doanh thu");
            dismiss();
        });
        return v;
    }
}