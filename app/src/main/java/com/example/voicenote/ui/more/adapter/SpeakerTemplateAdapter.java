// File: com/example/voicenote/ui/more/SpeakerTemplateAdapter.java
package com.example.voicenote.ui.more.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.SpeakerTemplateEntity;
import java.util.ArrayList;
import java.util.List;

public class SpeakerTemplateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_ADD_BUTTON = 1;

    public interface Listener {
        void onSelect(SpeakerTemplateEntity item);
        void onEdit(SpeakerTemplateEntity item);
        void onDelete(SpeakerTemplateEntity item);
        void onTest(SpeakerTemplateEntity item);
        void onAddNew();
    }

    private final List<SpeakerTemplateEntity> data = new ArrayList<>();
    private final Listener listener;

    public SpeakerTemplateAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<SpeakerTemplateEntity> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == data.size()) return TYPE_ADD_BUTTON;
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD_BUTTON) {
            TextView btn = new TextView(parent.getContext());
            btn.setText("+  Thêm mới");
            btn.setTextSize(16);
            btn.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.brand_blue));
            btn.setPadding(32, 32, 32, 32);
            return new VHAdd(btn);
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_speaker_template, parent, false);
        return new VHItem(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHItem) {
            ((VHItem) holder).bind(data.get(position));
        } else if (holder instanceof VHAdd) {
            holder.itemView.setOnClickListener(v -> listener.onAddNew());
        }
    }

    static class VHAdd extends RecyclerView.ViewHolder {
        public VHAdd(@NonNull View itemView) { super(itemView); }
    }

    class VHItem extends RecyclerView.ViewHolder {
        RadioButton rbSelect;
        TextView tvContent;
        ImageView btnMore;
        LinearLayout rootItem;

        public VHItem(@NonNull View itemView) {
            super(itemView);
            rbSelect = itemView.findViewById(R.id.rbSelect);
            tvContent = itemView.findViewById(R.id.tvContent);
            btnMore = itemView.findViewById(R.id.btnMore);
            rootItem = itemView.findViewById(R.id.rootItem);
        }

        void bind(SpeakerTemplateEntity item) {
            tvContent.setText(item.content);
            rbSelect.setChecked(item.isSelected);

            if (item.isSelected) {
                // Highlight (bạn có thể tạo drawable riêng cho selected nếu muốn)
                rootItem.setBackgroundResource(R.drawable.bg_card_16);
            } else {
                rootItem.setBackgroundResource(R.drawable.bg_card_16);
            }

            itemView.setOnClickListener(v -> listener.onSelect(item));
            rbSelect.setOnClickListener(v -> listener.onSelect(item));

            btnMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), btnMore);
                popup.inflate(R.menu.menu_speaker_item);
                popup.setOnMenuItemClickListener(menuItem -> {
                    int id = menuItem.getItemId();
                    if (id == R.id.action_edit) listener.onEdit(item);
                    else if (id == R.id.action_delete) listener.onDelete(item);
                    else if (id == R.id.action_test) listener.onTest(item);
                    return true;
                });
                popup.show();
            });
        }
    }
}