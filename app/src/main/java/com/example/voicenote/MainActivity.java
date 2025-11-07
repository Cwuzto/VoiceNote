package com.example.voicenote;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

// [SỬA] Import fragment mới
import com.example.voicenote.ui.order.OrderListFragment;
import com.example.voicenote.ui.more.MoreFragment;
import com.example.voicenote.ui.overview.OverviewFragment;
import com.example.voicenote.ui.sale.SaleActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navView;

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.bottom_nav);
        navView.setOnItemSelectedListener(this::onNavItemSelected);

        if(savedInstanceState==null){
            // Nếu là lần chạy đầu
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OverviewFragment())
                    .commit();

            // [MỚI] Kiểm tra Intent ngay khi tạo
            handleNavigationIntent(getIntent());
        }
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
        else if(id==R.id.nav_invoice) frag = new OrderListFragment();
        else if(id==R.id.nav_more) frag = new MoreFragment();
        // tab qr // else if(id==R.id.nav_qr) ...
        else return false;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag)
                .commit();
        return true;
    }
}