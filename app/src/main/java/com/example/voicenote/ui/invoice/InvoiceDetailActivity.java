package com.example.voicenote.ui.invoice;


import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.InvoiceEntity;
import com.example.voicenote.vm.InvoiceDetailViewModel;


/** Uses entity public fields (customer, total, paid). */
public class InvoiceDetailActivity extends AppCompatActivity {
    private InvoiceDetailViewModel viewModel;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);
        long invoiceId = getIntent().getLongExtra("invoice_id", -1);
        TextView tvCustomer = findViewById(R.id.tvCustomer);
        TextView tvTotal = findViewById(R.id.tvTotal);
        CheckBox cbPaid = findViewById(R.id.cbPaid);
        viewModel = new ViewModelProvider(this).get(InvoiceDetailViewModel.class);
        viewModel.getInvoiceById(invoiceId).observe(this, ivw -> {
            if (ivw != null && ivw.invoice != null) {
                InvoiceEntity iv = ivw.invoice;
                tvCustomer.setText(iv.customer);
                tvTotal.setText(String.valueOf(iv.total));
                cbPaid.setChecked(iv.paid);
            }
        });
        cbPaid.setOnCheckedChangeListener((b, c) -> {
            InvoiceEntity current = viewModel.getInvoiceById(invoiceId).getValue() != null ? viewModel.getInvoiceById(invoiceId).getValue().invoice : null;
            if (current != null) viewModel.setPaid(current, c);
        });
    }
}