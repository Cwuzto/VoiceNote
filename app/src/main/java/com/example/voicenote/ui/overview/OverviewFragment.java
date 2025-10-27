package com.example.voicenote.ui.overview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.voicenote.R;

/**
 * EN: Overview dashboard fragment.
 * VI: Fragment bảng tổng quan hiển thị doanh thu và thống kê cơ bản.
 */
public class OverviewFragment extends Fragment {
    private boolean hasData = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_overview, container, false);
        LinearLayout groupEmpty = v.findViewById(R.id.groupEmptyRevenue);
        LinearLayout groupData = v.findViewById(R.id.groupDataRevenue);
        TextView tvRevenueValue = v.findViewById(R.id.tvRevenueValue);
        TextView tvOrdersValue = v.findViewById(R.id.tvOrdersValue);

        v.findViewById(R.id.btnCreateDummy).setOnClickListener(x -> {
            hasData = true;
            groupEmpty.setVisibility(View.GONE);
            groupData.setVisibility(View.VISIBLE);
            tvRevenueValue.setText("12.5tr");
            tvOrdersValue.setText("34");
        });
        return v;
    }
}