package com.example.voicenote;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.example.voicenote.ui.invoice.InvoiceListFragment;
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
            // EN: open fullscreen sale and DO NOT change current tab content underneath
            // VI: mở màn bán hàng full screen, không thay Fragment hiện tại
            startActivity(new Intent(this, SaleActivity.class));
            return false; // không giữ chọn tab sale (để khi quay lại vẫn ở tab cũ)
        }
        Fragment frag;
        if(id==R.id.nav_overview) frag = new OverviewFragment();
        else if(id==R.id.nav_invoice) frag = new InvoiceListFragment();
        else if(id==R.id.nav_more) frag = new MoreFragment();
        else return false;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag)
                .commit();
        return true;
    }
}