// File: com/example/voicenote/vm/InvoiceListViewModel.java
package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.voicenote.data.local.entity.InvoiceEntity;
import com.example.voicenote.data.local.rel.InvoiceWithLines;
import com.example.voicenote.data.repo.InvoiceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * EN: ViewModel for Invoice list screen with built-in text filtering.
 *     - allInvoices: source from Room (LiveData<List<InvoiceWithLines>>)
 *     - filteredInvoices: what UI observes (after applying keyword)
 *     - filterInvoices(keyword): recompute filtered list
 *
 * VI: ViewModel cho màn danh sách hoá đơn, có lọc theo từ khoá.
 *     - allInvoices: nguồn dữ liệu từ Room (LiveData<List<InvoiceWithLines>>)
 *     - filteredInvoices: danh sách sau khi áp bộ lọc (UI observe biến này)
 *     - filterInvoices(keyword): tính lại danh sách đã lọc
 */
public class InvoiceListViewModel extends AndroidViewModel {

    private final InvoiceRepository repo;

    // Nguồn dữ liệu gốc từ Room
    private final LiveData<List<InvoiceWithLines>> allInvoices;

    // Dữ liệu đã lọc mà UI lắng nghe
    private final MediatorLiveData<List<InvoiceWithLines>> filteredInvoices = new MediatorLiveData<>();

    // Từ khoá hiện tại (để re-apply khi nguồn thay đổi)
    private String currentKeyword = "";

    public InvoiceListViewModel(@NonNull Application app) {
        super(app);
        repo = new InvoiceRepository(app);

        // Lấy dữ liệu gốc từ Repository (Room)
        allInvoices = repo.getInvoicesWithLines();

        // Gắn nguồn vào Mediator: mỗi khi Room bắn dữ liệu mới -> áp lại filter hiện tại.
        filteredInvoices.addSource(allInvoices, list -> {
            filteredInvoices.setValue(applyFilter(list, currentKeyword));
        });
    }

    /**
     * EN: UI observes this (already filtered). We keep the method name
     *     getAllInvoices() so existing Fragment code doesn't need changes.
     * VI: UI lắng nghe danh sách đã lọc. Giữ nguyên tên getAllInvoices()
     *     để Fragment hiện tại không cần sửa.
     */
    public LiveData<List<InvoiceWithLines>> getAllInvoices() {
        return filteredInvoices;
    }

    /**
     * EN: Filter by keyword across customer name and line item names.
     * VI: Lọc theo từ khoá, kiểm tra tên khách và tên từng món trong hoá đơn.
     */
    public void filterInvoices(String keyword) {
        currentKeyword = keyword != null ? keyword.trim() : "";
        List<InvoiceWithLines> source = allInvoices.getValue();
        filteredInvoices.setValue(applyFilter(source, currentKeyword));
    }

    /**
     * EN: Update "paid" flag of an invoice (delegate to Repo).
     * VI: Cập nhật trạng thái "đã thanh toán" (gọi xuống Repo).
     */
    public void setPaid(InvoiceEntity invoice, boolean paid) {
        repo.setPaid(invoice, paid);
    }

    // ----------------- Helpers -----------------

    private List<InvoiceWithLines> applyFilter(List<InvoiceWithLines> src, String keyword) {
        if (src == null) return new ArrayList<>();
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.getDefault());
        if (q.isEmpty()) {
            // Không lọc -> trả về nguyên danh sách (có thể clone nếu muốn an toàn)
            return new ArrayList<>(src);
        }
        List<InvoiceWithLines> out = new ArrayList<>();
        for (InvoiceWithLines ivw : src) {
            boolean match = false;

            // 1) So khớp theo tên khách
            if (ivw.invoice != null && ivw.invoice.customer != null) {
                if (ivw.invoice.customer.toLowerCase(Locale.getDefault()).contains(q)) {
                    match = true;
                }
            }

            // 2) Nếu chưa khớp, so tiếp theo tên từng dòng hàng
            if (!match && ivw.lineItems != null) {
                for (int i = 0; i < ivw.lineItems.size(); i++) {
                    if (ivw.lineItems.get(i).name != null &&
                            ivw.lineItems.get(i).name.toLowerCase(Locale.getDefault()).contains(q)) {
                        match = true;
                        break;
                    }
                }
            }

            if (match) out.add(ivw);
        }
        return out;
    }
}
