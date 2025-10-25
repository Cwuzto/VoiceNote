package com.example.voicenote.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.voicenote.R;

public class MoreFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_more, container, false);

        TextView tvOwnerName = v.findViewById(R.id.tvOwnerName);
        TextView tvOwnerPhone = v.findViewById(R.id.tvOwnerPhone);
        TextView tvHotline = v.findViewById(R.id.tvHotline);
        View rowHotline = v.findViewById(R.id.rowHotline);
        View rowAddQR = v.findViewById(R.id.rowAddQR);
        View rowAddEmployee = v.findViewById(R.id.rowAddEmployee);
        View rowScanOtherShop = v.findViewById(R.id.rowScanOtherShop);

        // TODO: bind tên/điện thoại thực tế từ user profile
        tvOwnerName.setText("Tạ Gia Bảo");
        tvOwnerPhone.setText("0931238461");

        rowHotline.setOnClickListener(v1 -> {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tvHotline.getText().toString().replace(" ","")));
            startActivity(i);
        });

        rowAddQR.setOnClickListener(v12 -> {
            // TODO: mở màn thiết lập QR để bật loa
        });

        rowAddEmployee.setOnClickListener(v13 -> {
            // TODO: mở màn thêm nhân viên (không hiển thị nhãn Pro)
        });

        rowScanOtherShop.setOnClickListener(v14 -> {
            // TODO: mở màn quét QR vào quán khác
        });

        return v;
    }
}
