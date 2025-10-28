package com.example.voicenote.ui.sale;

// [MỚI] Thêm các import cần thiết
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.QuickItemEntity;
import com.example.voicenote.ui.dialog.AddProductSheet;
import com.example.voicenote.ui.dialog.CustomerNameDialog;
import com.example.voicenote.ui.quick.GridSpacingItemDecoration;
import com.example.voicenote.ui.quick.QuickGridAdapter;

import java.util.ArrayList;
import java.util.List;

public class SaleActivity extends AppCompatActivity {
    private RecyclerView rvQuickGrid;
    private QuickGridAdapter adapter;
    private final List<QuickItemEntity> items = new ArrayList<>();
    private TextView tvCustomer;
    private LinearLayout rowCustomer;
    private LinearLayout quickBar;
    private EditText edtLine; // [MỚI] Thêm tham chiếu đến edtLine

    private boolean gridVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        // Ánh xạ các view
        quickBar = findViewById(R.id.quickBar);
        edtLine = findViewById(R.id.edtLine);
        rvQuickGrid = findViewById(R.id.rvQuickGrid);
        rowCustomer = findViewById(R.id.rowCustomer);
        tvCustomer = findViewById(R.id.tvCustomer);
        ImageButton btnGrid = findViewById(R.id.btnGrid);
        ImageButton btnClose = findViewById(R.id.btnClose);
        TextView btnDone = findViewById(R.id.btnDone);

        // --- Thiết lập các listener ---
        // [NÚT CLOSE ĐỂ ĐÓNG]
        btnClose.setOnClickListener(v -> finish());

        // [NÚT DONE (XONG) ĐỂ HOÀN THÀNH TẠO ĐƠN]
        btnDone.setOnClickListener(v -> finish());
        btnDone.setEnabled(false); // <--- DISABLE NÚT XONG (tạm thời)
        btnDone.setOnClickListener(v -> {
            //TODO
            // Sau này logic tạo hoá đơn sẽ nằm ở đây
            // ...
            finish();
        });

        rowCustomer.setOnClickListener(v -> {
            String current = tvCustomer.getText() != null ? tvCustomer.getText().toString() : "";
            CustomerNameDialog.newInstance(current)
                    .setCallback(name -> tvCustomer.setText(name))
                    .show(getSupportFragmentManager(), "customer_name");
        });

        // Logic khi click btnGrid (Case 1)
        btnGrid.setOnClickListener(v -> {
            gridVisible = !gridVisible; // Đảo trạng thái
            updateLayout(gridVisible);   // Cập nhật layout

            if (gridVisible) {
                // [CASE 1] Nếu grid VỪA BẬT, tắt bàn phím
                hideKeyboard();
            }
        });

        // [MỚI] Logic khi click edtLine (Case 2)
        edtLine.setOnClickListener(v -> {
            if (gridVisible) {
                // [CASE 2] Nếu grid đang BẬT, tắt grid đi
                gridVisible = false;
                updateLayout(false);
            }
            // Bàn phím sẽ tự động hiện lên do đây là hành vi mặc định của EditText
        });

        // --- Thiết lập RecyclerView ---
        rvQuickGrid.setLayoutManager(new GridLayoutManager(this, 4));
        rvQuickGrid.addItemDecoration(new GridSpacingItemDecoration(4, dp(10), true));
        rvQuickGrid.setNestedScrollingEnabled(true);

        seedDemoData();

        adapter = new QuickGridAdapter(
                items,
                this::openAddDialog,
                (item, position) -> { item.selected++; adapter.notifyItemChanged(position + 1); },
                (item, position) -> { items.remove(position); adapter.notifyItemRemoved(position + 1); }
        );
        rvQuickGrid.setAdapter(adapter);

        // --- Xử lý nút Back ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (gridVisible) {
                    gridVisible = false;
                    updateLayout(false);
                } else {
                    setEnabled(false);
                    SaleActivity.super.getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * [MỚI] Hàm helper để ẩn bàn phím
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // Tìm view đang focus để lấy window token
        View view = getCurrentFocus();
        if (view == null) {
            // Nếu không có view nào focus, tạo 1 view tạm
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Cập nhật vị trí của quickBar và visibility của rvQuickGrid
     */
    private void updateLayout(boolean showGrid) {
        rvQuickGrid.setVisibility(showGrid ? RecyclerView.VISIBLE : RecyclerView.GONE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) quickBar.getLayoutParams();

        if (showGrid) {
            params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ABOVE, R.id.rvQuickGrid);
        } else {
            params.removeRule(RelativeLayout.ABOVE);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        quickBar.setLayoutParams(params);
    }

    private void openAddDialog() {
        AddProductSheet sheet = new AddProductSheet((name, price) -> {
            String initial = makeInitial(name);
            QuickItemEntity qi = new QuickItemEntity(name, initial, price);
            items.add(qi);
            adapter.notifyItemInserted(items.size());
            rvQuickGrid.scrollToPosition(items.size());
        });
        sheet.show(getSupportFragmentManager(), "add_product");
    }

    private String makeInitial(String name){
        String[] parts = name.trim().split("\\s+");
        if (parts.length==0) return "?";
        if (parts.length==1) return parts[0].substring(0,1).toUpperCase();
        return (parts[0].substring(0,1) + parts[1].substring(0,1)).toUpperCase();
    }

    private void seedDemoData() {
        items.clear();
        items.add(new QuickItemEntity("Phở bò", "PB", 35000));
        items.add(new QuickItemEntity("Bún bò", "BB", 30000));
        items.add(new QuickItemEntity("Cà phê sữa", "CS", 25000));
        items.add(new QuickItemEntity("Trà đào", "TD", 28000));
        items.add(new QuickItemEntity("3 rọi", "3R", 40000));
        items.add(new QuickItemEntity("Thịt xông khói", "TX", 45000));
        items.add(new QuickItemEntity("Kim chi", "KC", 15000));
    }

    private int dp(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }
}