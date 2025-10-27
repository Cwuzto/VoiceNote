package com.example.voicenote.ui.quick;


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
import com.example.voicenote.data.local.entity.QuickItemEntity;
import com.example.voicenote.ui.dialog.AddProductSheet;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


import java.util.List;


public class QuickItemsSheet extends BottomSheetDialogFragment {
    public interface OnPick {
        void onPick(QuickItemEntity item);
    }

    private final List<QuickItemEntity> data;
    private final OnPick cb;
    private QuickItemAdapter adapter;

    public QuickItemsSheet(List<QuickItemEntity> data, OnPick cb) {
        this.data = data;
        this.cb = cb;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.bottomsheet_quick_items, c, false);
        RecyclerView rv = v.findViewById(R.id.rvQuick);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 4));
        adapter = new QuickItemAdapter(data, item -> {
            if (cb != null) cb.onPick(item);
        }, (item, position) -> {
            data.remove(position);
            adapter.notifyItemRemoved(position);
        });
        rv.setAdapter(adapter);
        TextView btnAdd = v.findViewById(R.id.btnAddItem);
        btnAdd.setOnClickListener(view -> openAddSheet());
        return v;
    }

    private void openAddSheet() {
        AddProductSheet sheet = new AddProductSheet((name, price) -> {
            QuickItemEntity qi = new QuickItemEntity(name, name.substring(0, 1).toUpperCase(), price);
            data.add(0, qi);
            if (adapter != null) adapter.notifyItemInserted(0);
        });
        sheet.show(getChildFragmentManager(), "addProduct");
    }
}