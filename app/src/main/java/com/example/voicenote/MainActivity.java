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
    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(this::onNavItemSelected);
        if(savedInstanceState==null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OverviewFragment())
                    .commit();
        }
    }

    private boolean onNavItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        if (id == R.id.nav_sale) {
            startActivity(new Intent(this, SaleActivity.class));
            return false;
        }
        Fragment frag;
        if(id==R.id.nav_overview) frag = new OverviewFragment();
            // [SỬA] Thay thế InvoiceListFragment
        else if(id==R.id.nav_invoice) frag = new OrderListFragment();
        else if(id==R.id.nav_more) frag = new MoreFragment();
        else return false;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag)
                .commit();
        return true;
    }
}