package com.example.voicenote.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voicenote.R;

public class InvoiceDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        findViewById(R.id.tvClose).setOnClickListener(v -> finish());

        LinearLayout btnPaid = findViewById(R.id.btnPaid);
        CheckBox cbPaid = findViewById(R.id.cbPaid);

        btnPaid.setOnClickListener(v -> {
            cbPaid.toggle(); // đổi trạng thái tick
        });

        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            startActivity(new Intent(this, InvoiceEditActivity.class)
                    .putExtra("invoice_id", getIntent().getLongExtra("invoice_id", -1)));
        });


    }
}
