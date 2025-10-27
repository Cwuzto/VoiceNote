package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.voicenote.data.local.entity.InvoiceEntity;
import com.example.voicenote.data.local.entity.LineItemEntity;
import com.example.voicenote.data.repo.InvoiceRepository;

import java.util.List;

/**
 * EN: ViewModel for invoice editing / creation screen.
 * VI: ViewModel cho màn hình chỉnh sửa / tạo hoá đơn.
 */
public class InvoiceEditViewModel extends AndroidViewModel {
    private final InvoiceRepository repo;

    public InvoiceEditViewModel(@NonNull Application app) {
        super(app);
        repo = new InvoiceRepository(app);
    }

    /**
     * EN: Save a new invoice with lines.
     * VI: Lưu một hoá đơn mới cùng danh sách dòng hàng.
     */
    public void insertInvoiceWithLines(InvoiceEntity invoice, List<LineItemEntity> lines) {
        repo.insertInvoiceWithLines(invoice, lines);
    }

    public void saveInvoice(InvoiceEntity invoice, List<LineItemEntity> lines) {
        repo.saveInvoice(invoice, lines);
    }
}