package com.example.voicenote.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.voicenote.R;
import com.example.voicenote.model.QuickItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaleFragment extends Fragment {

    // ví dụ giỏ tạm (bạn thay bằng CartViewModel của bạn)
    private final Map<Long, Integer> cartCounter = new HashMap<>();

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sale, container, false);
        ImageButton btnGrid = v.findViewById(R.id.btnGrid);

        btnGrid.setOnClickListener(view -> openQuickSheet());

        return v;
    }

    private void openQuickSheet(){
        // TODO: thay data này bằng query Room Product top bán chạy / menu có sẵn
        ArrayList<QuickItem> data = new ArrayList<>();
        data.add(new QuickItem(1,"Phở bò","PB",35000));
        data.add(new QuickItem(2,"Bún bò","BB",30000));
        // ...

        QuickItemsSheet sheet = new QuickItemsSheet(data, it -> {
            // +1 vào giỏ
        });
        sheet.show(getChildFragmentManager(), "quick");
    }
}

