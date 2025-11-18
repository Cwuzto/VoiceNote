// File: com/example/voicenote/ui/more/adapter/BankAccountAdapter.java
package com.example.voicenote.ui.more.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.BankAccountEntity;
import java.util.ArrayList;
import java.util.List;

public class BankAccountAdapter extends RecyclerView.Adapter<BankAccountAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(BankAccountEntity account);
    }

    private final List<BankAccountEntity> data = new ArrayList<>();
    private final OnItemClickListener listener;

    public BankAccountAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<BankAccountEntity> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bank_account, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position), listener);
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBankName, tvAccountNumber, tvAccountHolder, tvDefaultBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBankName = itemView.findViewById(R.id.tvBankName);
            tvAccountNumber = itemView.findViewById(R.id.tvAccountNumber);
            tvAccountHolder = itemView.findViewById(R.id.tvAccountHolder);
            tvDefaultBadge = itemView.findViewById(R.id.tvDefaultBadge);
        }

        public void bind(BankAccountEntity item, OnItemClickListener listener) {
            tvBankName.setText(item.bankName);
            tvAccountNumber.setText(item.accountNumber);
            tvAccountHolder.setText(item.accountHolderName);
            tvDefaultBadge.setVisibility(item.isDefault ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> listener.onClick(item));
        }
    }
}