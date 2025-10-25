package com.example.voicenote;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.voicenote.ui.InvoiceFragment;
import com.example.voicenote.ui.MoreFragment;
import com.example.voicenote.ui.OverviewFragment;
import com.example.voicenote.ui.PlaceholderFragment;
import com.example.voicenote.ui.SaleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_VoiceNote);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OverviewFragment())
                    .commit();
        }
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(this::onTabSelected);
        nav.setSelectedItemId(R.id.nav_overview);
    }

    private boolean onTabSelected(MenuItem item){
        if (item.getItemId() == R.id.nav_overview) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OverviewFragment()).commit();
            return true;
        }
        else if (item.getItemId() == R.id.nav_sale) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SaleFragment())
                    .commit();
            return true;
        }
        else if (item.getItemId() == R.id.nav_invoice) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new InvoiceFragment())
                    .commit();
            return true;
        }
        else if (item.getItemId() == R.id.nav_more) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MoreFragment())
                    .commit();
            return true;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, PlaceholderFragment.newInstance(
                        item.getTitle().toString()))
                .commit();
        return true;
    }
}
