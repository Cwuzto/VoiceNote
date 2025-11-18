// File: com/example/voicenote/data/local/dao/BankAccountDao.java
package com.example.voicenote.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.voicenote.data.local.entity.BankAccountEntity;
import java.util.List;

@Dao
public interface BankAccountDao {

    @Query("SELECT * FROM bank_account ORDER BY is_default DESC, created_at DESC")
    LiveData<List<BankAccountEntity>> getAllBankAccounts();

    // Các hàm cơ bản (Internal use)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInternal(BankAccountEntity account);

    @Update
    void updateInternal(BankAccountEntity account);

    @Delete
    void deleteInternal(BankAccountEntity account);

    @Query("UPDATE bank_account SET is_default = 0")
    void clearAllDefaults();

    @Query("SELECT COUNT(*) FROM bank_account")
    int getCount();

    @Query("SELECT * FROM bank_account LIMIT 1")
    BankAccountEntity getFirstAccount();

    // --- LOGIC THÔNG MINH ---

    @Transaction
    default void insertAccount(BankAccountEntity account) {
        if (getCount() == 0) {
            // Nếu chưa có tài khoản nào, cái này BẮT BUỘC là default
            account.isDefault = true;
        }

        if (account.isDefault) {
            // Nếu cái mới là default, bỏ chọn các cái cũ
            clearAllDefaults();
        }

        insertInternal(account);
    }

    @Transaction
    default void updateAccount(BankAccountEntity account) {
        if (account.isDefault) {
            // Nếu update thành default, bỏ chọn các cái khác
            clearAllDefaults();
        } else {
            // Nếu user cố tình bỏ default nhưng đây là tài khoản duy nhất
            if (getCount() == 1) {
                account.isDefault = true; // Bắt buộc phải true
            }
        }
        updateInternal(account);
    }

    @Transaction
    default void deleteAccount(BankAccountEntity account) {
        boolean wasDefault = account.isDefault;
        deleteInternal(account);

        // Nếu vừa xóa cái mặc định, hãy tìm cái khác lên thay thế
        if (wasDefault) {
            BankAccountEntity nextDefault = getFirstAccount();
            if (nextDefault != null) {
                nextDefault.isDefault = true;
                updateInternal(nextDefault);
            }
        }
    }
}