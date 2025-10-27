package com.example.voicenote.ui.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.voicenote.R;

/**
 * EN: Fragment displaying user info, hotline, and extra options.
 * VI: Fragment hiển thị thông tin người dùng, hotline và các tuỳ chọn thêm.
 */
public class MoreFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_more, container, false);
        TextView tvHotline = v.findViewById(R.id.tvHotline);
        View rowHotline = v.findViewById(R.id.rowHotline);
        rowHotline.setOnClickListener(x -> {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tvHotline.getText().toString().replace(" ", "")));
            startActivity(i);
        });
        return v;
    }
}