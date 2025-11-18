// File: com/example/voicenote/ui/more/BankAccountListActivity.java
package com.example.voicenote.ui.more;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.BankAccountEntity;
import com.example.voicenote.ui.dialog.AddEditBankAccountDialog;
import com.example.voicenote.ui.more.adapter.BankAccountAdapter;
import com.example.voicenote.vm.BankAccountViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BankAccountListActivity extends AppCompatActivity {

    private BankAccountViewModel viewModel;
    private BankAccountAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_account_list);

        viewModel = new ViewModelProvider(this).get(BankAccountViewModel.class);

        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.rvBankAccounts);
        FloatingActionButton fab = findViewById(R.id.fabAdd);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        adapter = new BankAccountAdapter(account -> openDialog(account));
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        viewModel.getAllAccounts().observe(this, list -> {
            if (list == null || list.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                tvEmpty.setVisibility(View.GONE);
            }
            adapter.submitList(list);
        });

        fab.setOnClickListener(v -> openDialog(null));
    }

    private void openDialog(BankAccountEntity account) {
        AddEditBankAccountDialog dialog = AddEditBankAccountDialog.newInstance(account);
        dialog.setOnSaveListener(acc -> {
            if (acc.id == 0) {
                viewModel.insertAccount(acc);
                Toast.makeText(this, "Đã thêm", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.updateAccount(acc);
                Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getSupportFragmentManager(), "AddBankDialog");
    }
}