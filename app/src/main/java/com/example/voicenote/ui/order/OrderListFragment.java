// File: com/example/voicenote/ui/invoice/OrderListFragment.java
package com.example.voicenote.ui.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
import com.example.voicenote.ui.dialog.StatusFilterSheet;
import com.example.voicenote.ui.dialog.TimeFilterSheet;
import com.example.voicenote.ui.order.adapter.OrderAdapter;
import com.example.voicenote.vm.OrderListViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Fragment hiển thị danh sách Order (đã refactor từ InvoiceListFragment)
 */
public class OrderListFragment extends Fragment {
    private OrderListViewModel viewModel;
    private OrderAdapter adapter;
    private TextView chipStatus, chipTime;
    private String currentFilterStatus = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_order_list, container, false);

        RecyclerView rv = v.findViewById(R.id.rvInvoice);
        TextView tvEmpty = v.findViewById(R.id.tvEmptyHint);
        View searchBar = v.findViewById(R.id.searchBar);
        EditText edtSearch = v.findViewById(R.id.edtSearch);
        TextView btnCancelSearch = v.findViewById(R.id.btnCancelSearch);
        ImageView btnSearch = v.findViewById(R.id.btnSearch);
        chipStatus = v.findViewById(R.id.chipStatus);
        chipTime = v.findViewById(R.id.chipTime);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khởi tạo adapter với 2 listener
        adapter = new OrderAdapter(
                // 1. Listener cho nút Checkbox "Đã thanh toán"
                (order, checked) -> {
                    viewModel.updatePaymentStatus(order, checked);
                },

                // 2. [MỚI] Listener khi bấm vào thẻ card
                (orderWithItems) -> {
                    // Tạo Intent để mở OrderDetailActivity
                    Intent intent = new Intent(getContext(), OrderDetailActivity.class);

                    // Gửi ID của đơn hàng qua
                    intent.putExtra("order_id", orderWithItems.order.id);

                    startActivity(intent);
                }
        );
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(OrderListViewModel.class);
        viewModel.getAllOrders().observe(getViewLifecycleOwner(), orders -> { // [SỬA]
            adapter.submit(orders); // [SỬA]
            tvEmpty.setVisibility(orders == null || orders.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // --- Xử lý hiển thị search bar ---
        btnSearch.setOnClickListener(v1 -> {
            searchBar.setVisibility(View.VISIBLE);
            btnSearch.setVisibility(View.GONE);
            edtSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
        });

        btnCancelSearch.setOnClickListener(v2 -> {
            searchBar.setVisibility(View.GONE);
            btnSearch.setVisibility(View.VISIBLE);
            edtSearch.setText("");
            edtSearch.clearFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
        });

        // Xử lý click cho Chip Lọc Trạng thái
        chipStatus.setOnClickListener(view -> showStatusFilter());

        // Xử lý click cho Chip Lọc Thời gian
        chipTime.setOnClickListener(view -> showTimeFilter());

        // --- Lọc danh sách khi người dùng nhập ---
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filterOrders(s.toString()); // [SỬA]
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return v;
    }
    /**
     * Mở BottomSheet chọn Trạng thái
     */
    private void showStatusFilter() {
        StatusFilterSheet sheet = new StatusFilterSheet();
        sheet.setListener((status, statusText) -> {
            // Cập nhật UI
            chipStatus.setText(statusText);
            // Gọi ViewModel
            viewModel.setStatusFilter(status);
        });
        sheet.show(getParentFragmentManager(), "StatusFilterSheet");
    }

    /**
     * Mở BottomSheet chọn Thời gian
     */
    private void showTimeFilter() {
        TimeFilterSheet sheet = new TimeFilterSheet();
        sheet.setListener(new TimeFilterSheet.OnTimeSelectedListener() {
            @Override
            public void onTimeSelected(String rangeKey, String rangeText) {
                // Cập nhật UI
                chipTime.setText(rangeText);

                // Tính toán ngày và gọi ViewModel
                Calendar cal = Calendar.getInstance();
                long endTime = cal.getTimeInMillis(); // Mặc định là bây giờ

                // Set về cuối ngày
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                endTime = cal.getTimeInMillis();

                switch(rangeKey) {
                    case "ALL":
                        viewModel.setDateRange(0, 0); // 0 = không lọc
                        break;
                    case "TODAY":
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        long startToday = cal.getTimeInMillis();
                        viewModel.setDateRange(startToday, endTime);
                        break;
                    case "YESTERDAY":
                        cal.add(Calendar.DAY_OF_YEAR, -1);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        long startYes = cal.getTimeInMillis();
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        long endYes = cal.getTimeInMillis();
                        viewModel.setDateRange(startYes, endYes);
                        break;
                    case "7DAYS":
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.add(Calendar.DAY_OF_YEAR, -6); // 6 ngày trước + hôm nay = 7
                        long start7 = cal.getTimeInMillis();
                        viewModel.setDateRange(start7, endTime);
                        break;
                    case "THIS_MONTH":
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        long startMonth = cal.getTimeInMillis();
                        viewModel.setDateRange(startMonth, endTime);
                        break;
                    case "LAST_MONTH":
                        cal.set(Calendar.DAY_OF_MONTH, 1); // Về ngày 1 tháng này
                        cal.add(Calendar.DAY_OF_YEAR, -1); // Về ngày cuối tháng trước
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        long endLastMonth = cal.getTimeInMillis();
                        cal.set(Calendar.DAY_OF_MONTH, 1); // Về ngày 1 tháng trước
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        long startLastMonth = cal.getTimeInMillis();
                        viewModel.setDateRange(startLastMonth, endLastMonth);
                        break;
                }
            }

            @Override
            public void onCustomRangeClicked() {
                // Xử lý "Tuỳ chỉnh" (Ảnh 3)
                showDateRangePicker();
            }
        });
        sheet.show(getParentFragmentManager(), "TimeFilterSheet");
    }

    /**
     * [MỚI] Mở Date Picker chuẩn của Material
     */
    private void showDateRangePicker() {

        // Tạo Date Picker
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Chọn khoảng thời gian")
                        .build();

        // Lắng nghe khi người dùng bấm "Xác nhận"
        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            long startTime = selection.first;
            long endTime = selection.second;

            // Chuyển múi giờ (DatePicker dùng UTC, DB dùng local)
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

            // Cập nhật ViewModel
            viewModel.setDateRange(adjustedStartTime, adjustedEndTime);

            // Cập nhật UI
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String rangeText = sdf.format(adjustedStartTime) + " - " + sdf.format(adjustedEndTime);
            chipTime.setText(rangeText);
        });

        dateRangePicker.show(getParentFragmentManager(), "DateRangePicker");
    }
}