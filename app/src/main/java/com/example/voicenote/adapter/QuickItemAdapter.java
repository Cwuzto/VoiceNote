package com.example.voicenote.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
import com.example.voicenote.model.QuickItem;

import java.util.List;

public class QuickItemAdapter extends RecyclerView.Adapter<QuickItemAdapter.VH> {

    public interface OnPick { void onPick(QuickItem item); }
    public interface OnRemove { void onRemove(QuickItem item, int position); }

    private final List<QuickItem> data;
    private final OnPick onPick;
    private final OnRemove onRemove;

    public QuickItemAdapter(List<QuickItem> data, OnPick onPick, OnRemove onRemove){
        this.data=data; this.onPick=onPick; this.onRemove=onRemove;
    }

    static class VH extends RecyclerView.ViewHolder{
        TextView tvInitial, tvName, tvBadge, ivRemove;
        VH(View v){
            super(v);
            tvInitial=v.findViewById(R.id.tvInitial);
            tvName=v.findViewById(R.id.tvName);
            tvBadge=v.findViewById(R.id.tvBadge);
            ivRemove=v.findViewById(R.id.ivRemove);
        }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_quick_chip, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        QuickItem it = data.get(pos);
        h.tvInitial.setText(it.initial);
        h.tvName.setText(it.name);

        h.tvBadge.setVisibility(it.selected>0? View.VISIBLE: View.GONE);
        if (it.selected>0) h.tvBadge.setText(String.valueOf(it.selected));

        h.ivRemove.setVisibility(it.showRemove? View.VISIBLE: View.GONE);

        // Click: nếu không ở chế độ remove -> +1
        h.itemView.setOnClickListener(v -> {
            if (it.showRemove) return; // đang ở chế độ xoá, bỏ qua click thường
            it.selected++;
            notifyItemChanged(h.getAdapterPosition());
            if (onPick!=null) onPick.onPick(it);
        });

        // Long click: bật/tắt hiển thị dấu trừ
        h.itemView.setOnLongClickListener(v -> {
            it.showRemove = !it.showRemove;
            notifyItemChanged(h.getAdapterPosition());
            return true;
        });

        // Click vào dấu trừ -> xoá item khỏi list
        h.ivRemove.setOnClickListener(v -> {
            int p = h.getAdapterPosition();
            if (p != RecyclerView.NO_POSITION) {
                QuickItem removed = data.get(p);
                if (onRemove!=null) onRemove.onRemove(removed, p);
            }
        });

        int[] pastelColors = {
                R.color.pastel_blue,
                R.color.pastel_purple,
                R.color.pastel_green,
                R.color.pastel_orange,
                R.color.pastel_pink
        };
        int color = pastelColors[pos % pastelColors.length];
        h.itemView.findViewById(R.id.tvInitial)
                .getRootView().findViewById(R.id.tvInitial)
                .setBackgroundResource(color);
    }

    @Override public int getItemCount() { return data.size(); }
}