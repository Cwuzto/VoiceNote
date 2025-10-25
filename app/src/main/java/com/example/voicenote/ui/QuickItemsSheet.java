package com.example.voicenote.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
import com.example.voicenote.adapter.QuickItemAdapter;
import com.example.voicenote.model.QuickItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class QuickItemsSheet extends BottomSheetDialogFragment {

    public interface OnPick { void onPick(QuickItem it); } // +1 vào giỏ

    private final ArrayList<QuickItem> list;
    private final OnPick cb;
    private QuickItemAdapter adapter;

    public QuickItemsSheet(ArrayList<QuickItem> list, OnPick cb){
        this.list = list;
        this.cb = cb;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.bottomsheet_quick_items, c, false);

        RecyclerView rv = v.findViewById(R.id.rvQuick);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 4));
        adapter = new QuickItemAdapter(list,
                item -> { if (cb!=null) cb.onPick(item); },
                (item, position) -> { // xoá item
                    list.remove(position);
                    adapter.notifyItemRemoved(position);
                });
        rv.setAdapter(adapter);

        TextView btnAdd = v.findViewById(R.id.btnAddItem);
        btnAdd.setOnClickListener(view -> openAddSheet());

        return v;
    }

    // Mở bottom sheet "Thêm hàng hoá"
    private void openAddSheet(){
        AddProductSheet sheet = new AddProductSheet((name, price) -> {
            // tạo QuickItem mới và thêm vào danh sách
            String initial = makeInitial(name);
            QuickItem qi = new QuickItem(System.currentTimeMillis(), name, initial, price);
            list.add(0, qi);
            if (adapter!=null) adapter.notifyItemInserted(0);
        });
        sheet.show(getChildFragmentManager(), "addProduct");
    }

    private String makeInitial(String name){
        String[] parts = name.trim().split("\\s+");
        if (parts.length==0) return "?";
        if (parts.length==1) return parts[0].substring(0,1).toUpperCase();
        return (parts[0].substring(0,1) + parts[1].substring(0,1)).toUpperCase();
    }

    @Override public void onStart() {
        super.onStart();
        if (getDialog() == null) return;
        View sheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet != null) {
            sheet.setBackgroundResource(R.drawable.bg_bottomsheet_round); // bo góc
            sheet.setPadding(0, 0, 0, 0);
        }
    }
}