// File: com/example/voicenote/data/local/entity/BankAccountEntity.java
package com.example.voicenote.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Quản lý thông tin tài khoản ngân hàng (để tạo QR)
 */
@Entity(tableName = "bank_account")
public class BankAccountEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id; // Dùng long cho PK auto-generate

    @ColumnInfo(name = "bank_name")
    public String bankName;

    @ColumnInfo(name = "account_number")
    public String accountNumber;

    @ColumnInfo(name = "account_holder_name")
    public String accountHolderName;

    @ColumnInfo(name = "qr_template")
    public String qrTemplate; // (nullable)

    @ColumnInfo(name = "is_default")
    public boolean isDefault;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public BankAccountEntity() {}
}