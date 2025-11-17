// File: com/example/voicenote/ui/overview/BestSellerActivity.java
package com.example.voicenote.ui.overview;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voicenote.R;
import com.example.voicenote.ui.dialog.SortFilterSheet;
import com.example.voicenote.ui.dialog.TimeFilterSheet;
import com.example.voicenote.ui.overview.adapter.BestSellerAdapter;
import com.example.voicenote.vm.BestSellerViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class BestSellerActivity extends AppCompatActivity {

    private BestSellerViewModel viewModel;
    private BestSellerAdapter adapter;
    private TextView chipTime, chipSort, tvEmpty;
    private View headerLayout, searchBar;
    private EditText edtSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_best_seller);

        viewModel = new ViewModelProvider(this).get(BestSellerViewModel.class);

        // --- Ánh xạ ---
        chipTime = findViewById(R.id.chipTime);
        chipSort = findViewById(R.id.chipSort);
        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rvBestSellers = findViewById(R.id.rvBestSellers);

        // Ánh xạ Search Bar
        headerLayout = findViewById(R.id.headerLayout);
        searchBar = findViewById(R.id.searchBar);
        edtSearch = findViewById(R.id.edtSearch);

        // --- Setup RV ---
        adapter = new BestSellerAdapter();
        rvBestSellers.setLayoutManager(new LinearLayoutManager(this));
        rvBestSellers.setAdapter(adapter);

        // --- Listeners ---
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        chipTime.setOnClickListener(v -> showTimeFilter());
        chipSort.setOnClickListener(v -> showSortFilter());
        findViewById(R.id.btnSearch).setOnClickListener(v -> toggleSearch(true));
        findViewById(R.id.btnCancelSearch).setOnClickListener(v -> toggleSearch(false));

        // --- Observe VM ---
        viewModel.getBestSellersList().observe(this, bestSellers -> {
            if (bestSellers == null || bestSellers.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvBestSellers.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvBestSellers.setVisibility(View.VISIBLE);
                adapter.submitList(bestSellers);
            }
        });
    }

    /**
     * Ẩn/Hiện Search Bar
     */
    private void toggleSearch(boolean show) {
        headerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        searchBar.setVisibility(show ? View.VISIBLE : View.GONE);

        if (show) {
            edtSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
        } else {
            edtSearch.setText(""); // Xóa text khi Hủy
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
        }
    }

    private void showTimeFilter() {
        TimeFilterSheet sheet = TimeFilterSheet.newInstance(false); // Ẩn "Toàn thời gian"
        sheet.setListener(new TimeFilterSheet.OnTimeSelectedListener() {
            @Override
            public void onTimeSelected(String rangeKey, String rangeText) {
                viewModel.setTimeRange(rangeKey);
                chipTime.setText(rangeText);
            }
            @Override
            public void onCustomRangeClicked() {
                showDateRangePicker();
            }
        });
        sheet.show(getSupportFragmentManager(), "TimeFilterSheet");
    }

    private void showSortFilter() {
        SortFilterSheet sheet = new SortFilterSheet();
        sheet.setListener((sortKey, sortText) -> {
            viewModel.setSortCriteria(sortKey);
            chipSort.setText(sortText);
            adapter.setSortCriteria(sortKey); // [QUAN TRỌNG] Báo cho Adapter
        });
        sheet.show(getSupportFragmentManager(), "SortFilterSheet");
    }

    private void showDateRangePicker() {
        // (Code copy từ OverviewFragment)
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker().build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            long startTime = selection.first;
            long endTime = selection.second;
            // (Code xử lý múi giờ)
            TimeZone timeZone = TimeZone.getDefault();
            long offset = timeZone.getOffset(startTime);
            long adjustedStartTime = startTime + offset;
            long adjustedEndTime = endTime + offset;
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(adjustedEndTime);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            adjustedEndTime = cal.getTimeInMillis();

            viewModel.setTimeRangeCustom(adjustedStartTime, adjustedEndTime);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String rangeText = sdf.format(adjustedStartTime) + " - " + sdf.format(adjustedEndTime);
            chipTime.setText(rangeText);
        });
        dateRangePicker.show(getSupportFragmentManager(), "DateRangePicker");
    }
}