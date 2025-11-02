// File: com/example/voicenote/ui/sale/OrderLineAdapter.java
package com.example.voicenote.ui.sale;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.OrderItemEntity;

import java.util.List;
import java.util.Locale;

public class OrderLineAdapter extends RecyclerView.Adapter<OrderLineAdapter.ViewHolder> {

    // Interfaces để Activity xử lý logic
    public interface OnItemInteractionListener {
        void onQuantityChanged(int position, int newQuantity);
        void onNoteChanged(int position, String newNote);
        void onItemClicked(int position, OrderItemEntity item);
        void onDeleteClicked(int position, OrderItemEntity item);
    }

    private final List<OrderItemEntity> orderItems;
    private final OnItemInteractionListener listener;

    public OrderLineAdapter(List<OrderItemEntity> orderItems, OnItemInteractionListener listener) {
        this.orderItems = orderItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_line, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItemEntity item = orderItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    // Lấy item tại vị trí
    public OrderItemEntity getItem(int position) {
        return orderItems.get(position);
    }

    // Xoá item
    public void removeItem(int position) {
        orderItems.remove(position);
        notifyItemRemoved(position);
    }

    // ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTotalPriceLine, tvQty;
        EditText edtNote;
        ImageButton btnMinus, btnPlus;
        LinearLayout clickableArea;
        NoteTextWatcher noteWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTotalPriceLine = itemView.findViewById(R.id.tvTotalPriceLine);
            tvQty = itemView.findViewById(R.id.tvQty);
            edtNote = itemView.findViewById(R.id.edtNote);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            clickableArea = itemView.findViewById(R.id.clickableArea);

            // TextWatcher cho Ghi chú
            noteWatcher = new NoteTextWatcher();
            edtNote.addTextChangedListener(noteWatcher);
        }

        public void bind(OrderItemEntity item, OnItemInteractionListener listener) {
            int position = getAdapterPosition();

            tvName.setText(item.productName);
            tvQty.setText(String.valueOf(item.quantity));
            edtNote.setText(item.note);

            // Tính tổng giá của dòng
            long lineTotal = item.unitPrice * item.quantity;
            tvTotalPriceLine.setText(String.format(Locale.US, "%,d", lineTotal));

            // Cập nhật listener cho TextWatcher
            noteWatcher.updatePosition(position, listener);

            // --- Listeners ---
            btnPlus.setOnClickListener(v -> {
                int newQty = item.quantity + 1;
                listener.onQuantityChanged(position, newQty);
            });

            btnMinus.setOnClickListener(v -> {
                int newQty = Math.max(0, item.quantity - 1); // Không cho âm, = 0 thì sẽ bị xoá
                listener.onQuantityChanged(position, newQty);
            });

            // Click vào vùng tên/giá để mở dialog
            clickableArea.setOnClickListener(v -> {
                listener.onItemClicked(position, item);
            });
        }
    }

    // TextWatcher tùy chỉnh để cập nhật ghi chú
    private static class NoteTextWatcher implements TextWatcher {
        private int position;
        private OnItemInteractionListener listener;
        private boolean isEditing = false; // Tránh vòng lặp vô hạn

        public void updatePosition(int position, OnItemInteractionListener listener) {
            this.position = position;
            this.listener = listener;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (listener != null && !isEditing) {
                isEditing = true;
                listener.onNoteChanged(position, s.toString());
                isEditing = false;
            }
        }
    }
}