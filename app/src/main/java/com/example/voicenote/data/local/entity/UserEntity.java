package com.example.voicenote.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Quản lý tài khoản & phân quyền
 */
@Entity(
        tableName = "users",
        indices = {@Index(value = "username", unique = true)} // Đảm bảo tên đăng nhập là duy nhất
)
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id; // Dùng long cho PK auto-generate

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "password")
    public String password; // Lưu ý: Cần mã hoá (hash) mật khẩu này

    @ColumnInfo(name = "full_name")
    public String fullName;

    @ColumnInfo(name = "role")
    public String role; // "OWNER" / "EMPLOYEE"

    @ColumnInfo(name = "is_active")
    public boolean isActive;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public UserEntity() {}
}