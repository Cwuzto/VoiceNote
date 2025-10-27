package com.example.voicenote.ui.invoice;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.InvoiceEntity;
import com.example.voicenote.data.local.entity.LineItemEntity;
import com.example.voicenote.vm.InvoiceEditViewModel;

import java.util.ArrayList;
import java.util.List;

public class InvoiceEditActivity extends AppCompatActivity {
    private InvoiceEditViewModel viewModel;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_invoice_edit);
        viewModel = new ViewModelProvider(this).get(InvoiceEditViewModel.class);
        findViewById(R.id.btnDone).setOnClickListener(v -> saveInvoice());
    }

    private void saveInvoice() {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.customer = "Khách mới";
        invoice.timeMillis = System.currentTimeMillis();
        invoice.total = 0; // sẽ tính từ lines nếu cần
        invoice.paid = false;

        List<LineItemEntity> lines = new ArrayList<>();
        LineItemEntity item = new LineItemEntity();
        item.name = "Phở bò";
        item.qty = 1;
        item.unitPrice = 50000;
        item.note = "";
        lines.add(item);

        viewModel.saveInvoice(invoice, lines);
        finish();
    }
}