package com.example.voicenote.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.voicenote.data.local.entity.BankAccountEntity;

import java.util.List;

@Dao
public interface BankAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAccount(BankAccountEntity account);

    @Query("SELECT * FROM bank_account ORDER BY is_default DESC, bank_name ASC")
    LiveData<List<BankAccountEntity>> getAllBankAccounts();

    @Query("SELECT * FROM bank_account WHERE is_default = 1 LIMIT 1")
    LiveData<BankAccountEntity> getDefaultAccount();
}