package com.example.voicenote.ui.sale;

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
import androidx.lifecycle.ViewModelProvider; // [MỚI]
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
// [SỬA] Import entity mới
import com.example.voicenote.data.local.entity.ProductEntity;
import com.example.voicenote.ui.dialog.AddProductSheet;
import com.example.voicenote.ui.dialog.CustomerNameDialog;
import com.example.voicenote.ui.quick.GridSpacingItemDecoration;
import com.example.voicenote.ui.quick.QuickGridAdapter;
import com.example.voicenote.vm.ProductViewModel; // [MỚI]

import java.util.ArrayList;
import java.util.List;

public class SaleActivity extends AppCompatActivity {
    private RecyclerView rvQuickGrid;
    private QuickGridAdapter adapter;
    // [SỬA] Dùng List<ProductEntity>
    private final List<ProductEntity> items = new ArrayList<>();
    private TextView tvCustomer;
    private LinearLayout rowCustomer;
    private LinearLayout quickBar;
    private EditText edtLine;
    private TextView btnDone;

    private boolean gridVisible = false;

    private ProductViewModel productViewModel; // [MỚI]

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        // [MỚI] Khởi tạo ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Ánh xạ các view
        quickBar = findViewById(R.id.quickBar);
        edtLine = findViewById(R.id.edtLine);
        rvQuickGrid = findViewById(R.id.rvQuickGrid);
        rowCustomer = findViewById(R.id.rowCustomer);
        tvCustomer = findViewById(R.id.tvCustomer);
        ImageButton btnGrid = findViewById(R.id.btnGrid);
        ImageButton btnClose = findViewById(R.id.btnClose);
        btnDone = findViewById(R.id.btnDone);

        // --- Thiết lập các listener ---
        btnClose.setOnClickListener(v -> finish());

        btnDone.setEnabled(false);
        btnDone.setOnClickListener(v -> {
            finish();
        });

        rowCustomer.setOnClickListener(v -> {
            String current = tvCustomer.getText() != null ? tvCustomer.getText().toString() : "";
            CustomerNameDialog.newInstance(current)
                    .setCallback(name -> tvCustomer.setText(name))
                    .show(getSupportFragmentManager(), "customer_name");
        });

        btnGrid.setOnClickListener(v -> {
            gridVisible = !gridVisible;
            updateLayout(gridVisible);
            if (gridVisible) {
                hideKeyboard();
            }
        });

        edtLine.setOnClickListener(v -> {
            if (gridVisible) {
                gridVisible = false;
                updateLayout(false);
            }
        });

        // --- Thiết lập RecyclerView ---
        rvQuickGrid.setLayoutManager(new GridLayoutManager(this, 4));
        rvQuickGrid.addItemDecoration(new GridSpacingItemDecoration(4, dp(10), true));
        rvQuickGrid.setNestedScrollingEnabled(true);

        // [XOÁ] seedDemoData();

        // [SỬA] Cập nhật Adapter
        adapter = new QuickGridAdapter(
                items,
                this::openAddDialog,
                // onPick: Tăng 'selected' (trường @Ignore) và cập nhật
                (item, position) -> {
                    item.selected++;
                    adapter.notifyItemChanged(position + 1); // +1 vì có ô "Add"
                },
                // onRemove: Gọi ViewModel để xoá khỏi DB
                (item, position) -> {
                    productViewModel.deleteProduct(item);
                    // LiveData sẽ tự động cập nhật list
                }
        );
        rvQuickGrid.setAdapter(adapter);

        // [MỚI] Lắng nghe dữ liệu sản phẩm từ Database
        productViewModel.getAllProducts().observe(this, productEntities -> {
            // Cập nhật trạng thái UI cũ (selected, showRemove) vào list mới
            // (Phần này có thể bỏ qua nếu bạn muốn reset trạng thái mỗi khi DB thay đổi)
            // ... (bỏ qua để đơn giản hoá)

            // Cập nhật adapter
            items.clear();
            if (productEntities != null) {
                items.addAll(productEntities);
            }
            // Thông báo toàn bộ adapter (vì list đã được build lại)
            adapter.notifyDataSetChanged();
        });

        // --- Xử lý nút Back ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (gridVisible) {
                    gridVisible = false;
                    updateLayout(false);
                } else {
                    setEnabled(false);
                    // [SỬA] Gọi đúng hàm onBackPressed()
                    SaleActivity.super.getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

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

    // [SỬA] Cập nhật logic thêm sản phẩm
    private void openAddDialog() {
        AddProductSheet sheet = new AddProductSheet((name, price) -> {
            // Tạo entity mới
            ProductEntity product = new ProductEntity(name, price);
            // Gọi ViewModel để lưu vào DB
            productViewModel.insertProduct(product);
            // LiveData sẽ tự động cập nhật UI
            // rvQuickGrid.scrollToPosition(items.size()); // (LiveData sẽ xử lý)
        });
        sheet.show(getSupportFragmentManager(), "add_product");
    }

    // [XOÁ] Hàm makeInitial (đã chuyển vào adapter)
    // private String makeInitial(String name){ ... }

    // [XOÁ] Hàm seedDemoData
    // private void seedDemoData() { ... }

    private int dp(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }
}