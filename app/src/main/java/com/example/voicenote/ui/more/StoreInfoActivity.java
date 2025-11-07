// File: com/example/voicenote/ui/more/StoreInfoActivity.java
package com.example.voicenote.ui.more;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.StoreEntity;
import com.example.voicenote.util.SessionManager;
import com.example.voicenote.vm.StoreViewModel;
// import com.example.voicenote.vm.StoreViewModel;

public class StoreInfoActivity extends AppCompatActivity {

    private StoreViewModel viewModel;
    private SessionManager sessionManager;
    private StoreEntity currentStore;
    private long ownerId;
    private EditText edtStoreName, edtStoreAddress;
    private TextView tvOwnerName, btnSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_info);

        viewModel = new ViewModelProvider(this).get(StoreViewModel.class);
        sessionManager = new SessionManager(this);
        ownerId = sessionManager.getUserId();

        findViews();

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveStoreInfo());

        observeViewModel(); // [MỚI]
    }

    private void findViews() {
        edtStoreName = findViewById(R.id.edtStoreName);
        edtStoreAddress = findViewById(R.id.edtStoreAddress);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        btnSave = findViewById(R.id.btnSave);
    }

    // [SỬA] Đổi tên từ loadStoreData
    private void observeViewModel() {
        if (ownerId == -1) {
            Toast.makeText(this, "Lỗi phiên đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 1. Tải thông tin cửa hàng
        viewModel.getStore(ownerId).observe(this, store -> {
            if (store != null) {
                currentStore = store;
                edtStoreName.setText(store.name);
                edtStoreAddress.setText(store.address);
            }
        });

        // 2. Tải thông tin chủ quán
        viewModel.getOwner(ownerId).observe(this, owner -> {
            if (owner != null) {
                tvOwnerName.setText(owner.fullName + " (" + owner.username + ")");
            }
        });

        // 3. Lắng nghe kết quả Update
        viewModel.getUpdateStoreResult().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveStoreInfo() {
        if (currentStore == null) return;

        String storeName = edtStoreName.getText().toString().trim();
        String storeAddress = edtStoreAddress.getText().toString().trim();

        if (storeName.isEmpty()) {
            edtStoreName.setError("Tên cửa hàng không được trống");
            return;
        }

        currentStore.name = storeName;
        currentStore.address = storeAddress;

        viewModel.updateStore(currentStore);
    }
}