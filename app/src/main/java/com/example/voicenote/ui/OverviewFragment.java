package com.example.voicenote.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.voicenote.R;
import com.google.android.material.button.MaterialButton;

public class OverviewFragment extends Fragment {

    private boolean hasData = false; // giả lập: false = ảnh 1, true = ảnh 2

    @Nullable
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_overview, container, false);

        LinearLayout groupEmptyRevenue = v.findViewById(R.id.groupEmptyRevenue);
        LinearLayout groupDataRevenue  = v.findViewById(R.id.groupDataRevenue);
        LinearLayout groupEmptyBest    = v.findViewById(R.id.groupEmptyBest);
        LinearLayout groupDataBest     = v.findViewById(R.id.groupDataBest);

        TextView tvRevenueValue = v.findViewById(R.id.tvRevenueValue);
        TextView tvOrdersValue  = v.findViewById(R.id.tvOrdersValue);
        TextView tvRevenueSummary = v.findViewById(R.id.tvRevenueSummary);

        MaterialButton btnCreate = v.findViewById(R.id.btnCreateDummy);
        btnCreate.setOnClickListener(view -> {
            // khi bấm "Tạo thử đơn" -> giả lập có số liệu
            hasData = true;
            applyState(groupEmptyRevenue, groupDataRevenue, groupEmptyBest, groupDataBest,
                    tvRevenueValue, tvOrdersValue, tvRevenueSummary);
        });

        applyState(groupEmptyRevenue, groupDataRevenue, groupEmptyBest, groupDataBest,
                tvRevenueValue, tvOrdersValue, tvRevenueSummary);

        return v;
    }

    private void applyState(View emptyRev, View dataRev, View emptyBest, View dataBest,
                            TextView tvRev, TextView tvOrders, TextView tvSummary){
        if (!hasData){
            emptyRev.setVisibility(View.VISIBLE);
            dataRev.setVisibility(View.GONE);
            emptyBest.setVisibility(View.VISIBLE);
            dataBest.setVisibility(View.GONE);

            tvRev.setText("0");
            tvOrders.setText("0");
        } else {
            emptyRev.setVisibility(View.GONE);
            dataRev.setVisibility(View.VISIBLE);
            emptyBest.setVisibility(View.GONE);
            dataBest.setVisibility(View.VISIBLE);

            // ví dụ giá trị sau khi có số liệu
            tvRev.setText("12.5tr");
            tvOrders.setText("34");
            tvSummary.setText("Tổng doanh thu: 12.500.000đ");
        }
    }
}

