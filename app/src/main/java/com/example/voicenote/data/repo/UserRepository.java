// File: com/example/voicenote/data/repo/UserRepository.java
package com.example.voicenote.data.repo;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.voicenote.data.local.AppDatabase;
import com.example.voicenote.data.local.dao.UserDao;
import com.example.voicenote.data.local.entity.UserEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final UserDao userDao;
    private final ExecutorService executor;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.userDao = db.userDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    // Lấy LiveData User (cho ProfileActivity)
    public LiveData<UserEntity> getUserById(long userId) {
        return userDao.getUserById(userId);
    }

    // Lấy User đồng bộ (cho kiểm tra mật khẩu)
    public void getUserByIdSync(long userId, OnUserFoundListener listener) {
        executor.execute(() -> {
            UserEntity user = userDao.getUserByIdSync(userId);
            listener.onFound(user);
        });
    }

    /**
     * Lấy user bằng username (chạy background)
     */
    public void findByUsername(String username, OnUserFoundListener listener) {
        executor.execute(() -> {
            UserEntity user = userDao.findByUsername(username);
            listener.onFound(user);
        });
    }

    /**
     * Thêm user mới (chạy background)
     */
    public void insertUser(UserEntity user, OnUserInsertedListener listener) {
        executor.execute(() -> {
            try {
                userDao.insertUser(user);
                listener.onSuccess();
            } catch (Exception e) {
                // Thường là lỗi "UNIQUE constraint failed: Users.Username"
                listener.onFailure(e);
            }
        });
    }

    // [MỚI] Lấy danh sách nhân viên
    public LiveData<List<UserEntity>> getAllEmployees() {
        return userDao.getAllEmployees();
    }

    // [MỚI] Cập nhật nhân viên
    public void updateUser(UserEntity user) {
        executor.execute(() -> userDao.updateUser(user));
    }

    // [MỚI] Xoá nhân viên
    public void deleteUser(UserEntity user) {
        executor.execute(() -> userDao.deleteUser(user));
    }

    // Interfaces để trả kết quả về Main thread
    public interface OnUserFoundListener {
        void onFound(UserEntity user);
    }
    public interface OnUserInsertedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}