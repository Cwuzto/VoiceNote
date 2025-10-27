package com.example.voicenote;


import android.os.Bundle;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.example.voicenote.ui.invoice.InvoiceListFragment;
import com.example.voicenote.ui.more.MoreFragment;
import com.example.voicenote.ui.overview.OverviewFragment;
import com.example.voicenote.ui.sale.SaleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


/**
 * Fixed IDs to match your XML: bottom_nav, fragment_container, nav_* menu IDs.
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(this::onNavItemSelected);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OverviewFragment()).commit();
        }
    }


    private boolean onNavItemSelected(@NonNull MenuItem item) {
        Fragment frag;
        int id = item.getItemId();
        if (id == R.id.nav_overview) frag = new OverviewFragment();
        else if (id == R.id.nav_sale) frag = new SaleFragment();
        else if (id == R.id.nav_invoice) frag = new InvoiceListFragment();
        else if (id == R.id.nav_more) frag = new MoreFragment();
        else return false;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frag).commit();
        return true;
    }
}