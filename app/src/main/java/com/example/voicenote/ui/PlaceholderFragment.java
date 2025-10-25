package com.example.voicenote.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PlaceholderFragment extends Fragment {
    private static final String ARG_TITLE = "t";

    public static PlaceholderFragment newInstance(String title){
        PlaceholderFragment f = new PlaceholderFragment();
        Bundle b = new Bundle(); b.putString(ARG_TITLE, title); f.setArguments(b);
        return f;
    }

    @Nullable @Override public View onCreateView(LayoutInflater inf, ViewGroup c, Bundle b) {
        TextView tv = new TextView(getContext());
        tv.setText(getArguments()!=null? getArguments().getString(ARG_TITLE):"");
        tv.setPadding(32, 64, 32, 64);
        tv.setTextSize(18f);
        return tv;
    }
}
