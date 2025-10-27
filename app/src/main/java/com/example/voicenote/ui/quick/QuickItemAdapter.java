package com.example.voicenote.ui.quick;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.QuickItemEntity;


import java.util.List;


/**
 * Uses public fields initial/name now available in entity.
 */
public class QuickItemAdapter extends RecyclerView.Adapter<QuickItemAdapter.VH> {
    public interface OnPick {
        void onPick(QuickItemEntity item);
    }

    public interface OnRemove {
        void onRemove(QuickItemEntity item, int pos);
    }

    private final List<QuickItemEntity> data;
    private final OnPick onPick;
    private final OnRemove onRemove;

    public QuickItemAdapter(List<QuickItemEntity> data, OnPick onPick, OnRemove onRemove) {
        this.data = data;
        this.onPick = onPick;
        this.onRemove = onRemove;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvInitial, tvName, ivRemove;

        VH(View v) {
            super(v);
            tvInitial = v.findViewById(R.id.tvInitial);
            tvName = v.findViewById(R.id.tvName);
            ivRemove = v.findViewById(R.id.ivRemove);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_quick_chip, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        QuickItemEntity it = data.get(pos);
        h.tvInitial.setText(it.initial);
        h.tvName.setText(it.name);
        h.itemView.setOnClickListener(v -> {
            if (onPick != null) onPick.onPick(it);
        });
        h.ivRemove.setOnClickListener(v -> {
            if (onRemove != null) onRemove.onRemove(it, h.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}