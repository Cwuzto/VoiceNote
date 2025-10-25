package com.example.voicenote.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voicenote.R;
import com.example.voicenote.model.Invoice;
import com.example.voicenote.utils.FormatUtils;

public class InvoiceEditActivity extends AppCompatActivity {
    private LinearLayout containerLines;
    private TextView tvTotal, tvCustomer;
    private Invoice invoice;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_invoice_edit);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        tvCustomer = findViewById(R.id.tvCustomer);
        containerLines = findViewById(R.id.containerLines);
        tvTotal = findViewById(R.id.tvTotal);

        // demo 2 dòng
        addLine("Phở bò", 50000, 1, "");
        addLine("Bún bò", 20000, 1, "");

        findViewById(R.id.rowCustomer).setOnClickListener(v -> {
            String current = tvCustomer.getText().toString();
            EditCustomerDialog.newInstance(current)
                    .setCallback(name -> tvCustomer.setText(name))
                    .show(getSupportFragmentManager(), "edit_customer");
        });

        updateTotal();
    }

    private void addLine(String name, long price, int qty, String note){
        View item = getLayoutInflater().inflate(R.layout.item_edit_line, containerLines, false);
        TextView tvName = item.findViewById(R.id.tvName);
        TextView tvPrice = item.findViewById(R.id.tvPrice);
        TextView tvUnitPrice = item.findViewById(R.id.tvUnitPrice);
        TextView tvQty = item.findViewById(R.id.tvQty);
        ImageButton btnMinus = item.findViewById(R.id.btnMinus);
        ImageButton btnPlus  = item.findViewById(R.id.btnPlus);
        EditText edtNote = item.findViewById(R.id.edtNote);

        tvName.setText(name);
        tvUnitPrice.setText(FormatUtils.money(price));
        tvQty.setText(String.valueOf(qty));
        tvPrice.setText(FormatUtils.money(price * qty));
        edtNote.setText(note);

        Runnable refresh = () -> {
            int q = Integer.parseInt(tvQty.getText().toString());
            tvPrice.setText(FormatUtils.money(price * q));
            updateTotal();
        };
        btnMinus.setOnClickListener(v -> {
            int q = Integer.parseInt(tvQty.getText().toString());
            if (q>0) { tvQty.setText(String.valueOf(q-1)); refresh.run(); }
        });
        btnPlus.setOnClickListener(v -> {
            int q = Integer.parseInt(tvQty.getText().toString());
            tvQty.setText(String.valueOf(q+1)); refresh.run();
        });

        containerLines.addView(item);
    }

    private void updateTotal(){
        long sum = 0;
        for (int i=0;i<containerLines.getChildCount();i++){
            View item = containerLines.getChildAt(i);
            TextView tvPrice = item.findViewById(R.id.tvPrice);
            sum += FormatUtils.parseMoney(tvPrice.getText().toString());
        }
        tvTotal.setText(FormatUtils.money(sum));
    }

}
