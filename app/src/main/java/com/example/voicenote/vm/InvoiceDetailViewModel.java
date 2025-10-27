package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voicenote.data.local.entity.InvoiceEntity;
import com.example.voicenote.data.local.rel.InvoiceWithLines;
import com.example.voicenote.data.repo.InvoiceRepository;

/**
 * EN: ViewModel for invoice detail screen.
 * VI: ViewModel cho màn chi tiết hoá đơn.
 */
public class InvoiceDetailViewModel extends AndroidViewModel {
    private final InvoiceRepository repo;

    public InvoiceDetailViewModel(@NonNull Application app) {
        super(app);
        repo = new InvoiceRepository(app);
    }

    public LiveData<InvoiceWithLines> getInvoiceById(long id) {
        return repo.getInvoiceById(id);
    }

    public void updatePaymentStatus(InvoiceEntity invoice, boolean paid) {
        repo.updatePaymentStatus(invoice, paid);
    }

    // Alias để Activity gọi viewModel.setPaid(...)
    public void setPaid(InvoiceEntity invoice, boolean paid) {
        repo.setPaid(invoice, paid);
    }

    public void deleteInvoice(InvoiceEntity invoice) {
        repo.deleteInvoice(invoice);
    }
}