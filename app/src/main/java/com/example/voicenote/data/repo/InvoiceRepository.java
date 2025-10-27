package com.example.voicenote.data.repo;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.example.voicenote.data.local.AppDatabase;
import com.example.voicenote.data.local.dao.InvoiceDao;
import com.example.voicenote.data.local.dao.LineItemDao;
import com.example.voicenote.data.local.entity.InvoiceEntity;
import com.example.voicenote.data.local.entity.LineItemEntity;
import com.example.voicenote.data.local.rel.InvoiceWithLines;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EN: Repository handles invoice + line item operations.
 * VI: Repository xử lý các thao tác hoá đơn và dòng hàng.
 */
public class InvoiceRepository {
    private final InvoiceDao invoiceDao;
    private final LineItemDao lineItemDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public InvoiceRepository(Application app) {
        AppDatabase db = Room.databaseBuilder(app, AppDatabase.class, "voicenote.db").build();
        this.invoiceDao = db.invoiceDao();
        this.lineItemDao = db.lineItemDao();
    }

    public LiveData<List<InvoiceWithLines>> getInvoicesWithLines() {
        return invoiceDao.getInvoicesWithLines();
    }

    public LiveData<InvoiceWithLines> getInvoiceById(long id) {
        return invoiceDao.getInvoiceById(id);
    }

    // ---- Canonical methods (đang dùng trong code mới) ----
    public void saveInvoice(InvoiceEntity invoice, List<LineItemEntity> lines) {
        executor.execute(() -> {
            long id = invoiceDao.insertInvoice(invoice); // REPLACE acts as update
            for (LineItemEntity li : lines) {
                li.invoiceId = id;
                lineItemDao.insertLineItem(li);
            }
        });
    }

    public void setPaid(InvoiceEntity invoice, boolean paid) {
        executor.execute(() -> {
            invoice.paid = paid;
            invoiceDao.insertInvoice(invoice);
        });
    }

    public void deleteInvoice(InvoiceEntity invoice) {
        executor.execute(() -> invoiceDao.deleteInvoice(invoice));
    }

    public void updatePaymentStatus(InvoiceEntity invoice, boolean paid) {
        setPaid(invoice, paid);
    }

    public void insertInvoiceWithLines(InvoiceEntity invoice, List<LineItemEntity> lines) {
        saveInvoice(invoice, lines);
    }
}