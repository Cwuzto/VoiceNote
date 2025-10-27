package com.example.voicenote.ui.invoice.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.InvoiceEntity;
import com.example.voicenote.data.local.rel.InvoiceWithLines;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Adapter fixed to pass InvoiceEntity directly (to match ViewModel.setPaid). */
public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.VH> {

    public interface OnPaidChange { void onChange(InvoiceEntity invoice, boolean checked); }
    private final List<InvoiceWithLines> data = new ArrayList<>();
    private final OnPaidChange callback;

    public InvoiceAdapter(OnPaidChange cb) { this.callback = cb; }

    public void submit(List<InvoiceWithLines> invoices) {
        data.clear();
        data.addAll(invoices);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) { holder.bind(data.get(position), callback); }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCustomer, tvTime, tvTotal; CheckBox cbPaid;
        VH(View v) { super(v);
            tvCustomer = v.findViewById(R.id.tvCustomer);
            tvTime = v.findViewById(R.id.tvTime);
            tvTotal = v.findViewById(R.id.tvTotal);
            cbPaid = v.findViewById(R.id.cbPaid);
        }
        void bind(InvoiceWithLines ivw, OnPaidChange cb) {
            InvoiceEntity iv = ivw.invoice;
            tvCustomer.setText(iv.customer);
            tvTotal.setText(String.format(Locale.US, "%,d", iv.total));
            cbPaid.setChecked(iv.paid);
            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTime.setText(df.format(iv.timeMillis));
            cbPaid.setOnCheckedChangeListener((b, checked) -> cb.onChange(iv, checked));
        }
    }
}