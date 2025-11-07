// File: com/example/voicenote/ui/more/EmployeeListActivity.java
package com.example.voicenote.ui.more;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.UserEntity;
import com.example.voicenote.ui.dialog.AddEmployeeDialog;
import com.example.voicenote.ui.more.adapter.EmployeeAdapter;
import com.example.voicenote.vm.EmployeeViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EmployeeListActivity extends AppCompatActivity {

    private EmployeeViewModel employeeViewModel;
    private EmployeeAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        employeeViewModel = new ViewModelProvider(this).get(EmployeeViewModel.class);

        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rvEmployees = findViewById(R.id.rvEmployees);
        FloatingActionButton fabAddEmployee = findViewById(R.id.fabAddEmployee);
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        // --- Setup Adapter ---
        adapter = new EmployeeAdapter(new EmployeeAdapter.OnEmployeeClickListener() {
            @Override
            public void onEditClick(UserEntity user) {
                openAddEmployeeDialog(user);
            }

            @Override
            public void onDeleteClick(UserEntity user) {
                confirmDelete(user);
            }
        });
        rvEmployees.setLayoutManager(new LinearLayoutManager(this));
        rvEmployees.setAdapter(adapter);

        // --- Lắng nghe dữ liệu ---
        employeeViewModel.getAllEmployees().observe(this, users -> {
            if (users == null || users.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                tvEmpty.setVisibility(View.GONE);
            }
            adapter.submitList(users);
        });

        // Lắng nghe kết quả Thêm/Sửa
        employeeViewModel.getSaveResult().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tên đăng nhập có thể đã tồn tại", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Listeners ---
        fabAddEmployee.setOnClickListener(v -> openAddEmployeeDialog(null));
    }

    private void openAddEmployeeDialog(UserEntity user) {
        AddEmployeeDialog dialog = AddEmployeeDialog.newInstance(user);
        dialog.setOnSaveListener((userToSave, password) -> {
            if (userToSave.id == 0) {
                // Thêm mới
                employeeViewModel.addEmployee(userToSave.fullName, userToSave.username, password);
            } else {
                // Cập nhật
                employeeViewModel.updateEmployee(userToSave, password);
            }
        });
        dialog.show(getSupportFragmentManager(), "AddEmployeeDialog");
    }

    private void confirmDelete(UserEntity user) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá nhân viên")
                .setMessage("Bạn có chắc muốn xoá " + user.fullName + "?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    employeeViewModel.deleteEmployee(user);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}