package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voicenote.data.local.entity.InvoiceEntity;
import com.example.voicenote.data.local.rel.InvoiceWithLines;
import com.example.voicenote.data.repo.InvoiceRepository;

import java.util.List;

/**
 * EN: ViewModel for invoice list, delegates to repository.
 * VI: ViewModel danh sách hoá đơn, gọi xuống repository.
 */
public class InvoiceListViewModel extends AndroidViewModel {
    private final InvoiceRepository repo;

    public InvoiceListViewModel(@NonNull Application app) {
        super(app);
        repo = new InvoiceRepository(app);
    }

    public LiveData<List<InvoiceWithLines>> getAllInvoices() {
        return repo.getInvoicesWithLines();
    }

    public void setPaid(InvoiceEntity invoice, boolean paid) {
        repo.setPaid(invoice, paid);
    }
}