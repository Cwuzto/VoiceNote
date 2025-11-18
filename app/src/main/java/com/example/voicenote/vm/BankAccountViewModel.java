// File: com/example/voicenote/vm/BankAccountViewModel.java
package com.example.voicenote.vm;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.voicenote.data.local.AppDatabase;
import com.example.voicenote.data.local.dao.BankAccountDao;
import com.example.voicenote.data.local.entity.BankAccountEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankAccountViewModel extends AndroidViewModel {

    private final BankAccountDao dao;
    private final LiveData<List<BankAccountEntity>> allAccounts;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BankAccountViewModel(@NonNull Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).bankAccountDao();
        allAccounts = dao.getAllBankAccounts();
    }

    public LiveData<List<BankAccountEntity>> getAllAccounts() {
        return allAccounts;
    }

    public void insertAccount(BankAccountEntity account) {
        executor.execute(() -> {
            account.createdAt = System.currentTimeMillis();
            account.updatedAt = System.currentTimeMillis();
            // Nếu là tài khoản đầu tiên, set mặc định
            // (Logic này có thể cần query count trước, nhưng tạm thời bỏ qua)
            dao.insertAccount(account);
        });
    }

    public void updateAccount(BankAccountEntity account) {
        executor.execute(() -> {
            account.updatedAt = System.currentTimeMillis();
            dao.insertAccount(account); // Insert with REPLACE acts as Update
        });
    }

    // (Bạn có thể thêm deleteAccount nếu cần, thêm hàm delete vào DAO trước)
}