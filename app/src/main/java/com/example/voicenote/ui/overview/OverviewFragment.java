// File: com/example/voicenote/ui/overview/OverviewFragment.java
package com.example.voicenote.ui.overview;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.R;
import com.example.voicenote.data.local.rel.BestSellerItem;
import com.example.voicenote.data.local.rel.ChartDataPoint;
import com.example.voicenote.ui.dialog.TimeFilterSheet;
import com.example.voicenote.ui.sale.SaleActivity;
import com.example.voicenote.vm.OverviewViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OverviewFragment extends Fragment {

    private OverviewViewModel viewModel;
    // Views cho ô trên cùng
    private TextView tvRevenueValue, tvOrdersValue, tvRevenueTimeRange, tvOrderTimeRange;
    // Views cho Card Doanh thu
    private LinearLayout groupEmptyRevenue, groupDataRevenue;
    private LineChart lineChart;
    private TextView chipRevenueChartTimeRange;

    // Views cho Card Bán chạy
    private LinearLayout groupEmptyBest, groupDataBest;
    private LinearLayout containerBestSellers;
    private TextView chipBestSellerTimeRange;
    private View chartEmptyView;

    private String currentChartRangeKey = "THIS_MONTH";

    // Biến lưu trữ an toàn (chống race condition)
    private List<ChartDataPoint> lastChartData = null;
    private Pair<Long, Long> lastChartRange = null;

    // Màu cho rank
    private final int[] rankColors = {0xFFF59E0B, 0xFF6B7280, 0xFF8D6E63};
    private final int[] rankIconColors = {0xFFFDE68A, 0xFFD1D5DB, 0xFFA1887F};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(OverviewViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_overview, container, false);

        // -- ánh xạ view --
        tvRevenueValue = v.findViewById(R.id.tvRevenueValue);
        tvOrdersValue = v.findViewById(R.id.tvOrdersValue);

        groupEmptyRevenue = v.findViewById(R.id.groupEmptyRevenue);
        groupDataRevenue = v.findViewById(R.id.groupDataRevenue);
        lineChart = v.findViewById(R.id.lineChart);
        chartEmptyView = v.findViewById(R.id.chartEmptyView);

        groupEmptyBest = v.findViewById(R.id.groupEmptyBest);
        groupDataBest = v.findViewById(R.id.groupDataBest);
        containerBestSellers = v.findViewById(R.id.containerBestSellers);

        // Ánh xạ 3 nút lọc
        tvRevenueTimeRange = v.findViewById(R.id.tvRevenueTimeRange);
        tvOrderTimeRange = v.findViewById(R.id.tvOrderTimeRange);
        chipRevenueChartTimeRange = v.findViewById(R.id.chipRevenueChartTimeRange);
        chipBestSellerTimeRange = v.findViewById(R.id.chipBestSellerTimeRange);

        // Nút xem tất cả
        v.findViewById(R.id.btnViewAllBestSellers).setOnClickListener(view -> {
            startActivity(new Intent(getContext(), BestSellerActivity.class));
        });

        // Gán 3 listener riêng biệt cho 3 nút lọc
        tvRevenueTimeRange.setOnClickListener(view -> showTimeFilter("REVENUE"));
        chipRevenueChartTimeRange.setOnClickListener(view -> showTimeFilter("CHART"));
        chipBestSellerTimeRange.setOnClickListener(view -> showTimeFilter("BEST_SELLER"));

        // Gán listener cho btnGoToSale
        v.findViewById(R.id.btnGoToSale).setOnClickListener(view -> {
            startActivity(new Intent(getContext(), SaleActivity.class));
        });

        observeViewModel();

        return v;
    }

    private void observeViewModel() {
        // 1. [MỚI] Observe "Trạng thái toàn cục"
        // Quyết định xem nên hiện "Tạo thử đơn" hay "Card Doanh thu"
        viewModel.getPaidOrderCount().observe(getViewLifecycleOwner(), count -> {
            boolean hasAnyData = (count != null && count > 0);

            if (hasAnyData) {
                // Đã có ít nhất 1 đơn -> Ẩn empty state "Tạo thử đơn"
                groupEmptyRevenue.setVisibility(View.GONE);
                groupEmptyBest.setVisibility(View.GONE);
                // Hiện card (bên trong nó sẽ tự xử lý)
                groupDataRevenue.setVisibility(View.VISIBLE);
                groupDataBest.setVisibility(View.VISIBLE);
            } else {
                // Chưa có đơn nào
                groupDataRevenue.setVisibility(View.GONE);
                groupDataBest.setVisibility(View.GONE);
                groupEmptyRevenue.setVisibility(View.VISIBLE);
                groupEmptyBest.setVisibility(View.VISIBLE);
            }
        });

        // 2. Lắng nghe Doanh thu & Số đơn
        viewModel.getRevenueSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null && summary.orderCount > 0) {
                // --- CÓ DỮ LIỆU ---
                tvRevenueValue.setText(String.format(Locale.US, "%,d", summary.totalRevenue));
                tvOrdersValue.setText(String.valueOf(summary.orderCount));

                // Ẩn Empty state
                groupEmptyRevenue.setVisibility(View.GONE);
                groupEmptyBest.setVisibility(View.GONE);

                // Hiện Data state
                groupDataRevenue.setVisibility(View.VISIBLE);
                groupDataBest.setVisibility(View.VISIBLE);
                // (Biểu đồ sẽ được cập nhật bởi observer 3)

            } else {
                // --- KHÔNG CÓ DỮ LIỆU ---
                tvRevenueValue.setText("0");
                tvOrdersValue.setText("0");

                // Ẩn Data state
                groupDataRevenue.setVisibility(View.GONE);
                groupDataBest.setVisibility(View.GONE);

                // Hiện Empty state (như layout cũ)
                groupEmptyRevenue.setVisibility(View.VISIBLE);
                groupEmptyBest.setVisibility(View.VISIBLE);
            }
        });

        // 3. Lắng nghe Top 3 sản phẩm
        viewModel.getBestSellers().observe(getViewLifecycleOwner(), bestSellers -> {
            containerBestSellers.removeAllViews();
            if (bestSellers != null && !bestSellers.isEmpty()) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                int rank = 0;
                for (BestSellerItem item : bestSellers) {
                    if (rank >= 3) break; // Chỉ hiển thị top 3

                    // Inflate layout item_best_seller.xml
                    View itemView = inflater.inflate(R.layout.item_best_seller, containerBestSellers, false);

                    TextView tvRank = itemView.findViewById(R.id.tvRank);
                    ImageView ivRankIcon = itemView.findViewById(R.id.ivRankIcon);
                    TextView tvProductName = itemView.findViewById(R.id.tvProductName);
                    TextView tvQuantity = itemView.findViewById(R.id.tvQuantity);

                    // Bind data
                    tvRank.setText(String.valueOf(rank + 1));
                    tvProductName.setText(item.productName);
                    tvQuantity.setText("x" + item.totalQuantity);

                    if (rank < rankColors.length) {
                        // Gọi .mutate() để tạo 1 bản sao của drawable trước khi tô màu (tint) nó.
                        Drawable rankBg = tvRank.getBackground().mutate();
                        rankBg.setTint(rankColors[rank]);
                        tvRank.setBackground(rankBg);

                        ivRankIcon.setColorFilter(rankIconColors[rank]);
                    }

                    containerBestSellers.addView(itemView);
                    rank++;
                }
            }
        });

        // 4. Observe Dữ liệu Biểu đồ
        viewModel.getChartData().observe(getViewLifecycleOwner(), chartDataPoints -> {
            this.lastChartData = chartDataPoints; // Lưu dữ liệu
            checkAndDrawChart(); // Gọi hàm kiểm tra
        });

        // 5. Observe Khoảng thời gian (Range)
        viewModel.getChartTimeRange().observe(getViewLifecycleOwner(), range -> {
            this.lastChartRange = range; // Lưu khoảng thời gian
            checkAndDrawChart(); // Gọi hàm kiểm tra
        });
    }

    /**
     * [MỚI] Hàm này chỉ chạy khi CẢ HAI LiveData (data và range) đã sẵn sàng
     */
    private void checkAndDrawChart() {
        // Nếu 1 trong 2 chưa sẵn sàng, thoát
        if (lastChartData == null || lastChartRange == null) {
            return;
        }

        // Cả 2 đã sẵn sàng:
        if (lastChartData.isEmpty()) {
            // Không có dữ liệu
            lineChart.setVisibility(View.GONE);
            chartEmptyView.setVisibility(View.VISIBLE);
        } else {
            // Có dữ liệu
            lineChart.setVisibility(View.VISIBLE);
            chartEmptyView.setVisibility(View.GONE);
            updateLineChart(lastChartData, currentChartRangeKey, lastChartRange);
        }
    }

    /**
     * Mở BottomSheet chọn Thời gian (Copy từ OrderListFragment)
     */
    private void showTimeFilter(String filterType) {
        // OverviewFragment không bao giờ hiển thị "Toàn thời gian"
        TimeFilterSheet sheet = TimeFilterSheet.newInstance(false);

        sheet.setListener(new TimeFilterSheet.OnTimeSelectedListener() {
            @Override
            public void onTimeSelected(String rangeKey, String rangeText) {
                // [SỬA] Cập nhật đúng ViewModel
                switch (filterType) {
                    case "REVENUE":
                        viewModel.setRevenueTimeRange(rangeKey);
                        tvRevenueTimeRange.setText("Doanh thu " + rangeText.toLowerCase());
                        tvOrderTimeRange.setText("Đơn " + rangeText.toLowerCase());
                        break;
                    case "CHART":
                        currentChartRangeKey = rangeKey; // [SỬA]
                        viewModel.setChartTimeRange(rangeKey);
                        chipRevenueChartTimeRange.setText(rangeText);
                        break;
                    case "BEST_SELLER":
                        viewModel.setBestSellerTimeRange(rangeKey);
                        chipBestSellerTimeRange.setText(rangeText);
                        break;
                }
            }

            @Override
            public void onCustomRangeClicked() {
                // Gọi hàm DatePicker
                showDateRangePicker(filterType);
            }
        });
        sheet.show(getParentFragmentManager(), "TimeFilterSheet");
    }

    /**
     * Mở Date Picker chuẩn của Material
     */
    private void showDateRangePicker(String filterType) {
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Chọn khoảng thời gian")
                        .build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            long startTime = selection.first;
            long endTime = selection.second;

            // Xử lý múi giờ
            TimeZone timeZone = TimeZone.getDefault();
            long offset = timeZone.getOffset(startTime);

            long adjustedStartTime = startTime + offset;
            long adjustedEndTime = endTime + offset;

            // Set EndTime về cuối ngày
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(adjustedEndTime);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            adjustedEndTime = cal.getTimeInMillis();

            // Cập nhật UI
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String rangeText = sdf.format(adjustedStartTime) + " - " + sdf.format(adjustedEndTime);

            // Cập nhật đúng ViewModel
            switch (filterType) {
                case "REVENUE":
                    viewModel.setRevenueTimeRangeCustom(adjustedStartTime, adjustedEndTime);
                    tvRevenueTimeRange.setText("Doanh thu " + rangeText);
                    tvOrderTimeRange.setText("Đơn " + rangeText);
                    break;
                case "CHART":
                    currentChartRangeKey = "CUSTOM";
                    viewModel.setChartTimeRangeCustom(adjustedStartTime, adjustedEndTime);
                    chipRevenueChartTimeRange.setText(rangeText);
                    break;
                case "BEST_SELLER":
                    viewModel.setBestSellerTimeRangeCustom(adjustedStartTime, adjustedEndTime);
                    chipBestSellerTimeRange.setText(rangeText);
                    break;
            }
        });

        dateRangePicker.show(getParentFragmentManager(), "DateRangePicker");
    }

    /**
     * Hàm helper để cập nhật Text
     */
    private void updateFilterTextViews(String rangeText) {
        String formattedText = rangeText.toLowerCase();
        tvRevenueTimeRange.setText("Doanh thu " + formattedText);
        tvOrderTimeRange.setText("Đơn " + formattedText);
        chipRevenueChartTimeRange.setText(rangeText);
        chipBestSellerTimeRange.setText(rangeText);
    }

    private void updateLineChart(List<ChartDataPoint> dataPoints, String rangeKey, Pair<Long, Long> range) {
        // Xử lý trường hợp chỉ có 1 điểm
        boolean isSingleDay = "TODAY".equals(rangeKey) || "YESTERDAY".equals(rangeKey);
        boolean isSinglePoint = dataPoints.size() == 1;

        // 1. Chuyển đổi ChartDataPoint sang Entry
        ArrayList<Entry> entries = new ArrayList<>();
        final SimpleDateFormat sdf; // Định dạng cho trục X

        // 1. Tìm giá trị Min/Max (để hiển thị text)
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;
        for (ChartDataPoint point : dataPoints) {
            if (point.dayRevenue < minY) minY = point.dayRevenue;
            if (point.dayRevenue > maxY) maxY = point.dayRevenue;
        }

        // 2. Chọn định dạng Trục X (Giờ / Ngày)
        if (isSingleDay) {
            sdf = new SimpleDateFormat("HH'h'", Locale.getDefault());
        } else {
            sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        }

        // 3. Xử lý logic "Hình Vòm" (Arc)
        if (isSinglePoint && range != null) {
            // Lấy điểm dữ liệu thật
            Entry realPoint = new Entry(dataPoints.get(0).dayMillis, dataPoints.get(0).dayRevenue);

            long rangeStart, rangeEnd;

            if (isSingleDay) {
                // Logic Arc cho "Hôm nay" / "Hôm qua" (00:00 -> 23:59)
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis((long) realPoint.getX());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                rangeStart = cal.getTimeInMillis();
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                rangeEnd = cal.getTimeInMillis();
            } else {
                // Logic Arc cho "7 ngày", "Tháng này"...
                rangeStart = range.first;
                rangeEnd = range.second;

                // (Workaround: endTime của range (vd: 7days) là 23:59 HÔM NAY.
                // Chúng ta cần nó là 23:59 của ngày BẮT ĐẦU (vd: 11/11)
                // và 23:59 của ngày KẾT THÚC (vd: 17/11)
                // Tạm thời dùng logic đơn giản:
                if (rangeKey.equals("7DAYS")) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    rangeEnd = cal.getTimeInMillis();
                    cal.add(Calendar.DAY_OF_YEAR, -6);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    rangeStart = cal.getTimeInMillis();
                }
                // (Tương tự cho các range khác... Logic này cần được hoàn thiện sau)
            }

            // Thêm điểm 0đ đầu ngày (Nếu đơn không phải lúc 0h)
            if (realPoint.getX() != rangeStart) {
                entries.add(new Entry(rangeStart, 0));
            }
            entries.add(realPoint); // Thêm điểm thật

            // Thêm điểm 0đ cuối ngày (Nếu đơn không phải lúc 23h)
            if (realPoint.getX() < rangeEnd) { // (Dùng < 23:59)
                entries.add(new Entry(rangeEnd, 0));
            }
            // Vì đã thêm 0đ, minY = 0
            minY = 0f;

        }
        // [SỬA LỖI] 'else' phải nằm ở đây
        else {
            // Trường hợp bình thường (nhiều điểm, hoặc 1 điểm ở range > 1 ngày)
            for (ChartDataPoint point : dataPoints) {
                // X (ngày) là mốc thời gian (long)
                // Y (doanh thu) là tổng tiền (long -> float)
                entries.add(new Entry(point.dayMillis, point.dayRevenue));
            }
        }

        // [SỬA LỖI] Toàn bộ code bên dưới được đưa ra ngoài
        // (để nó chạy cho cả 2 trường hợp IF và ELSE)
        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu");

        // ... (Style cho dataSet: màu, bo tròn, v.v...)
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.brand_blue));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.brand_blue));
        dataSet.setCircleRadius(5f);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setDrawValues(false);

        // Bật Gradient (nếu có > 1 điểm)
        if (entries.size() > 1) {
            dataSet.setDrawFilled(true);
            Drawable fillDrawable = ContextCompat.getDrawable(getContext(), R.drawable.chart_fill_gradient);
            dataSet.setFillDrawable(fillDrawable);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            dataSet.setDrawFilled(false);
            dataSet.setMode(LineDataSet.Mode.LINEAR);
        }

        // Hiển thị giá Min/Max
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.GRAY);
        final float finalMinY = minY;
        final float finalMaxY = maxY;
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                // Chỉ hiện text nếu là Min hoặc Max
                if (entry.getY() == finalMinY || entry.getY() == finalMaxY) {
                    return String.format(Locale.US, "%,dđ", (long) entry.getY());
                }
                return ""; // Ẩn các giá trị khác
            }
        });

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // --- Tùy chỉnh Biểu đồ ---
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        MyMarkerView marker = new MyMarkerView(getContext(), R.layout.chart_marker_view);
        lineChart.setMarker(marker);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);

        // Tắt trục Y bên phải
        lineChart.getAxisRight().setEnabled(false);

        // Tắt trục Y bên trái
        lineChart.getAxisLeft().setEnabled(false);

        // Tùy chỉnh trục X (Ngày/Tháng)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setLabelCount(4, true);

        // Set Granularity (khoảng cách)
        if (isSingleDay) {
            xAxis.setGranularity(3600000f); // 1 giờ
        } else {
            xAxis.setGranularity(86400000f); // 1 ngày
        }

        // Quan trọng: Định dạng mốc thời gian (long) thành "dd/MM"
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                // 'value' ở đây chính là (long) point.dayMillis
                return sdf.format((long) value);
            }
        });

        lineChart.invalidate();
    }
}