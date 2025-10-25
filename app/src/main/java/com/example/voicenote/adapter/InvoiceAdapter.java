package com.example.voicenote.adapter;

import android.content.Intent;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
import com.example.voicenote.ui.InvoiceFragment;
import com.example.voicenote.ui.InvoiceDetailActivity;


import java.util.*;

public class InvoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnPaidChange {
        void onChange(InvoiceFragment.Invoice iv, boolean checked);
    }

    private final List<InvoiceFragment.InvoiceRow> data = new ArrayList<>();
    private final OnPaidChange cb;

    public InvoiceAdapter(OnPaidChange cb) {
        this.cb = cb;
    }

    public void submit(List<InvoiceFragment.InvoiceRow> rows) {
        data.clear();
        data.addAll(rows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int type) {
        LayoutInflater inf = LayoutInflater.from(p.getContext());
        if (type == InvoiceFragment.InvoiceRow.TYPE_HEADER) {
            return new VHHeader(inf.inflate(R.layout.item_invoice_header, p, false));
        }
        return new VHItem(inf.inflate(R.layout.item_invoice_card, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
        InvoiceFragment.InvoiceRow row = data.get(pos);
        if (row.type == InvoiceFragment.InvoiceRow.TYPE_HEADER) {
            ((VHHeader) h).bind(row.header);
        } else {
            ((VHItem) h).bind(row.item, cb);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VHHeader extends RecyclerView.ViewHolder {
        TextView tvDay;

        VHHeader(View v) {
            super(v);
            tvDay = v.findViewById(R.id.tvDay);
        }

        void bind(String day) {
            tvDay.setText(day);
        }
    }

    static class VHItem extends RecyclerView.ViewHolder {
        TextView tvCustomer, tvTime, tvLines, tvTotal, btnDelete, tvPaidLabel;
        CheckBox cbPaid;

        VHItem(View v) {
            super(v);
            tvCustomer = v.findViewById(R.id.tvCustomer);
            tvTime = v.findViewById(R.id.tvTime);
            tvLines = v.findViewById(R.id.tvLines);
            tvTotal = v.findViewById(R.id.tvTotal);
            cbPaid = v.findViewById(R.id.cbPaid);
            tvPaidLabel = v.findViewById(R.id.tvPaidLabel);
            btnDelete = v.findViewById(R.id.btnDelete);
        }

        void bind(InvoiceFragment.Invoice iv, OnPaidChange cb) {
            tvCustomer.setText(iv.customer);
            tvTime.setText(android.text.format.DateFormat.format("HH:mm", iv.time));
            tvTotal.setText(InvoiceFragment.formatMoney(iv.total));
            // ghép dòng
            StringBuilder sb = new StringBuilder();
            for (String s : iv.lines) {
                sb.append(s).append("\n");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            tvLines.setText(sb.toString());

            cbPaid.setChecked(iv.paid);
            LinearLayout btnPaidArea = itemView.findViewById(R.id.btnPaidArea);
            CheckBox cbPaid = itemView.findViewById(R.id.cbPaid);

            btnPaidArea.setOnClickListener(v -> {
                boolean newChecked = !cbPaid.isChecked();
                cbPaid.setChecked(newChecked);
                cb.onChange(iv, newChecked); // callback để cập nhật trạng thái
            });


            tvPaidLabel.setText("Đã nhận tiền");
            View pill = (View) cbPaid.getParent();
            pill.setBackgroundResource(R.drawable.bg_outline_pill);

            cbPaid.setOnCheckedChangeListener((buttonView, checked) -> {
                cb.onChange(iv, checked);
            });

            btnDelete.setOnClickListener(v -> {
                // TODO: xoá invoice khỏi DB, notify list
                v.setEnabled(false);
            });

            // mở màn chi tiết khi bấm vào card
            itemView.setOnClickListener(v2 -> {
                Intent it = new Intent(v2.getContext(), InvoiceDetailActivity.class);
                // gửi dữ liệu tối thiểu (tuỳ bạn có id thì gửi id)
                it.putExtra("customer", iv.customer);
                it.putExtra("time", iv.time);
                it.putExtra("total", iv.total);
                it.putExtra("lines", iv.lines); // String[]; Activity nhận qua getStringArrayExtra
                it.putExtra("paid", iv.paid);
                v2.getContext().startActivity(it);
            });
        }
    }
}
