package com.example.voicenote.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.voicenote.data.local.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity findByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    LiveData<UserEntity> getUserById(long userId);

    // Lấy user đồng bộ (cho logic đổi mật khẩu)
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getUserByIdSync(long userId);

    // Lấy danh sách nhân viên (EMPLOYEE)
    @Query("SELECT * FROM users WHERE role = 'EMPLOYEE' ORDER BY full_name ASC")
    LiveData<List<UserEntity>> getAllEmployees();

    // Cập nhật nhân viên
    @Update
    void updateUser(UserEntity user);

    // Xoá nhân viên
    @Delete
    void deleteUser(UserEntity user);
}