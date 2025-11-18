// File: com/example/voicenote/data/local/entity/UserEntity.java
package com.example.voicenote.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {@Index(value = "username", unique = true)}
)
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;

    @ColumnInfo(name = "username")
    public String username;

    // Đổi tên 2 cột này
    @ColumnInfo(name = "password_hash")
    public String passwordHash; // Sẽ lưu hash

    @ColumnInfo(name = "password_salt")
    public String passwordSalt; // Sẽ lưu salt

    @ColumnInfo(name = "full_name")
    public String fullName;

    @ColumnInfo(name = "role")
    public String role;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "gender")
    public String gender;

    @ColumnInfo(name = "image_url")
    public String imageUrl;

    @ColumnInfo(name = "is_active")
    public boolean isActive;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public UserEntity() {}
}