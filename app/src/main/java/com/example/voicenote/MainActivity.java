package com.example.voicenote;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.ui.product.ProductListFragment;
import com.example.voicenote.ui.order.OrderListFragment;
import com.example.voicenote.ui.more.MoreFragment;
import com.example.voicenote.ui.overview.OverviewFragment;
import com.example.voicenote.ui.sale.SaleActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.voicenote.util.SessionManager;
import com.example.voicenote.vm.ProfileViewModel;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navView;
    private SessionManager sessionManager;
    private ProfileViewModel profileViewModel;

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.bottom_nav);
        navView.setOnItemSelectedListener(this::onNavItemSelected);

        // Thêm SessionManager và ViewModel
        sessionManager = new SessionManager(this);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        if(savedInstanceState==null){
            // Nếu là lần chạy đầu
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OverviewFragment())
                    .commit();

            //  Kiểm tra Intent ngay khi tạo
            handleNavigationIntent(getIntent());
        }
        // Tải dữ liệu User để phân quyền
        loadUserAndSetupPermissions();
    }

    /**
     * [MỚI] Tải User và ẩn Tab nếu là EMPLOYEE
     */
    private void loadUserAndSetupPermissions() {
        long userId = sessionManager.getUserId();
        if (userId == -1) return; // Lỗi (nên quay về Login)

        profileViewModel.getUser(userId).observe(this, user -> {
            if (user == null) return;

            if ("EMPLOYEE".equals(user.role)) {
                // LÀ NHÂN VIÊN -> ẨN TAB
                navView.getMenu().findItem(R.id.nav_overview).setVisible(false);
                navView.getMenu().findItem(R.id.nav_products).setVisible(false);

                // (Optional) Nếu tab Overview đang được chọn, chuyển họ sang tab Hoá đơn
                if (navView.getSelectedItemId() == R.id.nav_overview) {
                    navView.setSelectedItemId(R.id.nav_invoice);
                }
            } else {
                // LÀ OWNER -> HIỂN THỊ
                navView.getMenu().findItem(R.id.nav_overview).setVisible(true);
                navView.getMenu().findItem(R.id.nav_products).setVisible(true);
            }
        });
    }

    /**
     * Xử lý Intent khi Activity đã chạy (từ SaleActivity quay về)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNavigationIntent(intent);
    }

    /**
     * [MỚI] Hàm trung tâm xử lý "tin nhắn" điều hướng
     */
    private void handleNavigationIntent(Intent intent) {
        if (intent == null || intent.getStringExtra("NAVIGATE_TO") == null) {
            return;
        }

        String navigateTo = intent.getStringExtra("NAVIGATE_TO");

        if ("ORDERS_TAB".equals(navigateTo)) {
            // Đây là "tin nhắn" từ SaleActivity
            // Tự động chọn tab Hoá đơn
            navView.setSelectedItemId(R.id.nav_invoice);

            // Xoá "tin nhắn" đi để không bị gọi lại khi xoay màn hình
            intent.removeExtra("NAVIGATE_TO");
        }
    }

    private boolean onNavItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();

        // Bấm tab Bán hàng
        if (id == R.id.nav_sale) {
            startActivity(new Intent(this, SaleActivity.class));
            return false;
        }
        Fragment frag;
        if(id==R.id.nav_overview) frag = new OverviewFragment();
        else if(id==R.id.nav_products) frag = new ProductListFragment();
        else if(id==R.id.nav_invoice) frag = new OrderListFragment();
        else if(id==R.id.nav_more) frag = new MoreFragment();
        else return false;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag)
                .commit();
        return true;
    }
}