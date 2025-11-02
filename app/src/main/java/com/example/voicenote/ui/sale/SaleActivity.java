// File: com/example/voicenote/ui/sale/SaleActivity.java
package com.example.voicenote.ui.sale;

import android.content.Context;
import android.graphics.Canvas; // [MỚI]
import android.graphics.Color; // [MỚI]
import android.graphics.drawable.ColorDrawable; // [MỚI]
import android.graphics.drawable.Drawable; // [MỚI]
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull; // [MỚI]
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // [MỚI]
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper; // [MỚI]
import androidx.recyclerview.widget.LinearLayoutManager; // [MỚI]
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
// [SỬA] Import các entity và adapter mới
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.entity.ProductEntity;
import com.example.voicenote.ui.dialog.AddProductSheet;
import com.example.voicenote.ui.dialog.CustomerNameDialog;
import com.example.voicenote.ui.dialog.EditOrderItemDialog; // [MỚI]
import com.example.voicenote.ui.quick.GridSpacingItemDecoration;
import com.example.voicenote.ui.quick.QuickGridAdapter;
// [SỬA] Import ViewModel mới
import com.example.voicenote.vm.OrderEditViewModel;
import com.example.voicenote.vm.ProductViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleActivity extends AppCompatActivity {

    // --- Views ---
    private RecyclerView rvQuickGrid, rvOrderLines; // [MỚI] rvOrderLines
    private LinearLayout quickBar, rowCustomer;
    private LinearLayout contentGuide; // [SỬA]
    private NestedScrollView cartScrollView; // [MỚI]
    private EditText edtLine;
    private TextView tvCustomer, btnDone, tvTotal; // [MỚI] tvTotal

    // --- Adapters & ViewModels ---
    private QuickGridAdapter quickGridAdapter;
    private OrderLineAdapter orderLineAdapter; // [MỚI]
    private ProductViewModel productViewModel;
    private OrderEditViewModel orderEditViewModel; // [MỚI]

    // --- Data ---
    private final List<ProductEntity> quickProducts = new ArrayList<>();
    private final List<OrderItemEntity> currentOrderItems = new ArrayList<>(); // [MỚI] Giỏ hàng

    private boolean gridVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        // --- Khởi tạo ViewModels ---
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        orderEditViewModel = new ViewModelProvider(this).get(OrderEditViewModel.class); // [MỚI]

        // --- Ánh xạ Views ---
        findViews();

        // --- Thiết lập Listeners ---
        setupListeners();

        // --- Thiết lập RecyclerViews ---
        setupQuickGrid(); // Lưới chọn nhanh
        setupOrderLines(); // Giỏ hàng [MỚI]

        // --- Lắng nghe dữ liệu DB ---
        observeQuickProducts();

        // --- Xử lý nút Back ---
        setupBackButton();

        // --- Cập nhật UI lần đầu ---
        updateCartUI();
    }

    private void findViews() {
        quickBar = findViewById(R.id.quickBar);
        edtLine = findViewById(R.id.edtLine);
        rvQuickGrid = findViewById(R.id.rvQuickGrid);
        rvOrderLines = findViewById(R.id.rvOrderLines); // [MỚI]
        rowCustomer = findViewById(R.id.rowCustomer);
        tvCustomer = findViewById(R.id.tvCustomer);
        cartScrollView = findViewById(R.id.cartScrollView); // [MỚI]
        btnDone = findViewById(R.id.btnDone);
        tvTotal = findViewById(R.id.tvTotal); // [MỚI]
        contentGuide = findViewById(R.id.contentGuide); // [SỬA]
    }

    private void setupListeners() {
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        btnDone.setEnabled(false); // Mặc định tắt
        btnDone.setOnClickListener(v -> saveOrderAndFinish()); // [SỬA] Gọi hàm mới

        rowCustomer.setOnClickListener(v -> {
            String current = tvCustomer.getText() != null ? tvCustomer.getText().toString() : "";
            CustomerNameDialog.newInstance(current)
                    .setCallback(name -> tvCustomer.setText(name))
                    .show(getSupportFragmentManager(), "customer_name");
        });

        findViewById(R.id.btnGrid).setOnClickListener(v -> {
            gridVisible = !gridVisible;
            updateLayout(gridVisible);
            if (gridVisible) hideKeyboard();
        });

        edtLine.setOnClickListener(v -> {
            if (gridVisible) {
                gridVisible = false;
                updateLayout(false);
            }
        });
    }

    private void setupQuickGrid() {
        rvQuickGrid.setLayoutManager(new GridLayoutManager(this, 4));
        rvQuickGrid.addItemDecoration(new GridSpacingItemDecoration(4, dp(10), true));
        rvQuickGrid.setNestedScrollingEnabled(true);

        quickGridAdapter = new QuickGridAdapter(
                quickProducts,
                this::openAddDialog,
                this::onProductPicked, // [MỚI] Logic thêm vào giỏ hàng
                (item, position) -> productViewModel.deleteProduct(item)
        );
        rvQuickGrid.setAdapter(quickGridAdapter);
    }

    // [MỚI] Thiết lập RecyclerView cho giỏ hàng
    private void setupOrderLines() {
        rvOrderLines.setLayoutManager(new LinearLayoutManager(this));
        rvOrderLines.setNestedScrollingEnabled(false);

        orderLineAdapter = new OrderLineAdapter(currentOrderItems, new OrderLineAdapter.OnItemInteractionListener() {
            @Override
            public void onQuantityChanged(int position, int newQuantity) {
                if (newQuantity <= 0) {
                    // Nếu số lượng về 0, xoá
                    currentOrderItems.remove(position);
                    orderLineAdapter.notifyItemRemoved(position);
                } else {
                    currentOrderItems.get(position).quantity = newQuantity;
                    orderLineAdapter.notifyItemChanged(position);
                }
                updateCartUI(); // Cập nhật tổng tiền và UI
            }

            @Override
            public void onNoteChanged(int position, String newNote) {
                if (position < currentOrderItems.size()) {
                    currentOrderItems.get(position).note = newNote;
                }
            }

            @Override
            public void onItemClicked(int position, OrderItemEntity item) {
                openEditDialog(item, position); // Mở dialog chỉnh sửa
            }

            @Override
            public void onDeleteClicked(int position, OrderItemEntity item) {
                currentOrderItems.remove(position);
                orderLineAdapter.notifyItemRemoved(position);
                updateCartUI();
            }
        });
        rvOrderLines.setAdapter(orderLineAdapter);

        // [MỚI] Gắn Swipe Helper
        attachSwipeHelper();
    }

    // [MỚI] Lắng nghe danh sách sản phẩm từ DB
    private void observeQuickProducts() {
        productViewModel.getAllProducts().observe(this, productEntities -> {
            quickProducts.clear();
            if (productEntities != null) {
                quickProducts.addAll(productEntities);
            }
            quickGridAdapter.notifyDataSetChanged();
        });
    }

    private void setupBackButton() {
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

    // --- Logic nghiệp vụ chính ---

    // [MỚI] Khi chọn 1 món từ lưới (QuickGrid)
    private void onProductPicked(ProductEntity product, int position) {
        // 1. Kiểm tra xem món đã có trong giỏ hàng chưa
        for (int i = 0; i < currentOrderItems.size(); i++) {
            OrderItemEntity item = currentOrderItems.get(i);
            // Giả sử tên sản phẩm là duy nhất
            if (item.productName.equals(product.name)) {
                // Đã có -> Tăng số lượng
                item.quantity++;
                orderLineAdapter.notifyItemChanged(i);
                updateCartUI();
                return;
            }
        }

        // 2. Nếu chưa có -> Tạo OrderItemEntity mới
        OrderItemEntity newItem = new OrderItemEntity();
        newItem.productName = product.name;
        newItem.unitPrice = product.price;
        newItem.quantity = 1;
        newItem.note = "";

        currentOrderItems.add(newItem);
        orderLineAdapter.notifyItemInserted(currentOrderItems.size() - 1);
        updateCartUI();
    }

    // [MỚI] Khi mở dialog thêm sản phẩm (nút +)
    private void openAddDialog() {
        AddProductSheet sheet = new AddProductSheet((name, price) -> {
            ProductEntity product = new ProductEntity(name, price);
            productViewModel.insertProduct(product);
            // LiveData sẽ tự động cập nhật lưới quickProducts
        });
        sheet.show(getSupportFragmentManager(), "add_product");
    }

    // [MỚI] Mở dialog chỉnh sửa
    private void openEditDialog(OrderItemEntity item, int position) {
        EditOrderItemDialog dialog = EditOrderItemDialog.newInstance(item);
        dialog.setOnSaveListener(updatedItem -> {
            // Cập nhật item trong list
            currentOrderItems.set(position, updatedItem);
            orderLineAdapter.notifyItemChanged(position);
            updateCartUI();
        });
        dialog.setOnDeleteListener(itemToDelete -> {
            // Xoá item
            currentOrderItems.remove(position);
            orderLineAdapter.notifyItemRemoved(position);
            updateCartUI();
        });
        dialog.show(getSupportFragmentManager(), "edit_order_item");
    }

    // [MỚI] Cập nhật UI giỏ hàng (tổng tiền, ẩn/hiện guide)
    private void updateCartUI() {
        if (currentOrderItems.isEmpty()) {
            // Giỏ hàng rỗng
            contentGuide.setVisibility(View.VISIBLE);
            cartScrollView.setVisibility(View.GONE); // [SỬA]
            btnDone.setEnabled(false);
        } else {
            // Có hàng
            contentGuide.setVisibility(View.GONE);
            cartScrollView.setVisibility(View.VISIBLE); // [SỬA]
            btnDone.setEnabled(true);
        }

        // Tính tổng tiền
        long total = 0;
        for (OrderItemEntity item : currentOrderItems) {
            total += (item.unitPrice * item.quantity);
        }
        tvTotal.setText(String.format(Locale.US, "%,d", total));
    }

    // [MỚI] Lưu đơn hàng và thoát
    private void saveOrderAndFinish() {
        // 1. Kiểm tra (dù nút đã bị disable)
        if (currentOrderItems.isEmpty()) {
            return;
        }

        // 2. Tạo OrderEntity mới
        OrderEntity order = new OrderEntity();

        // 3. Lấy thông tin khách hàng
        String customerName = tvCustomer.getText().toString();
        if (customerName.equals("Khách hàng, phòng bàn...")) {
            order.customerName = "Khách lẻ"; // Tên mặc định
        } else {
            order.customerName = customerName;
        }

        // 4. Tính tổng tiền (an toàn hơn là tính lại)
        long total = 0;
        for (OrderItemEntity item : currentOrderItems) {
            total += (item.unitPrice * item.quantity);
        }
        order.totalAmount = total;

        // 5. Set các trường mặc định cho đơn hàng mới
        order.status = "UNPAID"; // Mặc định là chưa thanh toán
        order.paymentMethod = "CASH"; // Mặc định là tiền mặt
        order.sellerId = null; // Sẽ cập nhật sau khi có logic login
        // (createdAt và updatedAt sẽ được OrderRepository tự động thêm)

        // 6. Gọi ViewModel để lưu
        // (currentOrderItems chính là List<OrderItemEntity> mà ViewModel cần)
        orderEditViewModel.saveOrder(order, currentOrderItems);

        // 7. Lưu thành công, đóng Activity
        finish();
    }

    // [MỚI] Gắn ItemTouchHelper để xử lý vuốt
    private void attachSwipeHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private Drawable deleteIcon = ContextCompat.getDrawable(SaleActivity.this, R.drawable.ic_delete_24);
            private Drawable editIcon = ContextCompat.getDrawable(SaleActivity.this, R.drawable.ic_edit_24);
            private ColorDrawable backgroundDelete = new ColorDrawable(Color.parseColor("#D91C1C")); // Màu đỏ
            private ColorDrawable backgroundEdit = new ColorDrawable(Color.parseColor("#1565FF")); // Màu xanh

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Không hỗ trợ kéo thả
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Không làm gì ở onSwiped, vì chúng ta vẽ nút
            }

            // [QUAN TRỌNG] Vẽ các nút Sửa/Xoá
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                float buttonWidth = itemView.getHeight() * 0.8f; // Chiều rộng mỗi nút

                if (dX < 0) { // Chỉ xử lý vuốt trái
                    // Nền Xoá (màu đỏ)
                    backgroundDelete.setBounds(
                            (int) (itemView.getRight() + dX),
                            itemView.getTop(),
                            itemView.getRight(),
                            itemView.getBottom()
                    );
                    backgroundDelete.draw(c);

                    // Nền Sửa (màu xanh)
                    backgroundEdit.setBounds(
                            (int) (itemView.getRight() + dX - buttonWidth),
                            itemView.getTop(),
                            (int) (itemView.getRight() + dX),
                            itemView.getBottom()
                    );
                    backgroundEdit.draw(c);

                    // Icon Xoá
                    int iconMargin = (int) (buttonWidth * 0.25);
                    int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                    int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconLeft = iconRight - deleteIcon.getIntrinsicWidth();
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);

                    // Icon Sửa
                    int iconLeftEdit = (int) (iconLeft - buttonWidth);
                    int iconRightEdit = (int) (iconRight - buttonWidth);
                    editIcon.setBounds(iconLeftEdit, iconTop, iconRightEdit, iconBottom);
                    editIcon.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        }).attachToRecyclerView(rvOrderLines);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) view = new View(this);
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

    private int dp(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }
}