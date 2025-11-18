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
    // Nếu là tài khoản đầu tiên, set mặc định
    public void insertAccount(BankAccountEntity account) {
        executor.execute(() -> {
            account.createdAt = System.currentTimeMillis();
            account.updatedAt = System.currentTimeMillis();
            dao.insertAccount(account); // Gọi hàm transaction trong DAO
        });
    }

    public void updateAccount(BankAccountEntity account) {
        executor.execute(() -> {
            account.updatedAt = System.currentTimeMillis();
            dao.updateAccount(account); // Gọi hàm transaction trong DAO
        });
    }

    // Thêm hàm xoá
    public void deleteAccount(BankAccountEntity account) {
        executor.execute(() -> dao.deleteAccount(account));
    }
}