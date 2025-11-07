// File: com/example/voicenote/ui/auth/CreateStoreActivity.java
package com.example.voicenote.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.MainActivity;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.StoreEntity;
import com.example.voicenote.vm.StoreViewModel;

public class CreateStoreActivity extends AppCompatActivity {

    public static final String EXTRA_OWNER_ID = "owner_id";

    private StoreViewModel storeViewModel;
    private EditText edtStoreName;
    private EditText edtStoreAddress;
    private long ownerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_store);

        ownerId = getIntent().getLongExtra(EXTRA_OWNER_ID, -1);
        if (ownerId == -1) {
            // Lỗi, không có OwnerId, không thể tạo cửa hàng
            Toast.makeText(this, "Lỗi: Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show();
            finish(); // Đóng lại
            return;
        }

        storeViewModel = new ViewModelProvider(this).get(StoreViewModel.class);
        edtStoreName = findViewById(R.id.edtStoreName);
        edtStoreAddress = findViewById(R.id.edtStoreAddress);
        TextView btnCreateStore = findViewById(R.id.btnCreateStore);

        btnCreateStore.setOnClickListener(v -> createStore());

        // Lắng nghe kết quả tạo
        storeViewModel.getCreateStoreResult().observe(this, success -> {
            if (success) {
                // Tạo thành công, chuyển đến MainActivity
                startActivity(new Intent(CreateStoreActivity.this, MainActivity.class));
                finishAffinity(); // Đóng LoginActivity và CreateStoreActivity
            } else {
                Toast.makeText(this, "Tạo cửa hàng thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createStore() {
        String storeName = edtStoreName.getText().toString().trim();
        String storeAddress = edtStoreAddress.getText().toString().trim();

        if (storeName.isEmpty()) {
            edtStoreName.setError("Tên cửa hàng không được trống");
            edtStoreName.requestFocus();
            return;
        }

        StoreEntity store = new StoreEntity();
        store.name = storeName;
        store.address = storeAddress;
        store.ownerId = ownerId;

        // Gọi ViewModel
        storeViewModel.createStore(store);
    }
}