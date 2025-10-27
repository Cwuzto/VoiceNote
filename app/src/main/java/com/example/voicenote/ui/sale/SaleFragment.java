package com.example.voicenote.ui.sale;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.QuickItemEntity;
import com.example.voicenote.ui.quick.QuickItemsSheet;


import java.util.ArrayList;


public class SaleFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){ View v=inflater.inflate(R.layout.fragment_sale, container, false); ImageButton btnGrid=v.findViewById(R.id.btnGrid); btnGrid.setOnClickListener(view-> openQuickSheet()); return v; }
    private void openQuickSheet(){ ArrayList<QuickItemEntity> data = new ArrayList<>(); data.add(new QuickItemEntity("Phở bò","PB",35000)); data.add(new QuickItemEntity("Bún bò","BB",30000)); QuickItemsSheet sheet = new QuickItemsSheet(data, it -> {}); sheet.show(getChildFragmentManager(), "quick"); }
}