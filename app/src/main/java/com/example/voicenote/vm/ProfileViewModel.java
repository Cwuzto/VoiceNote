// File: com/example/voicenote/vm/ProfileViewModel.java
package com.example.voicenote.vm;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.voicenote.data.local.entity.UserEntity;
import com.example.voicenote.data.repo.UserRepository;
import com.example.voicenote.util.PasswordUtils;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final MutableLiveData<Boolean> updateResult = new MutableLiveData<>();
    private final MutableLiveData<String> passwordChangeResult = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.repository = new UserRepository(application);
    }

    public LiveData<UserEntity> getUser(long userId) {
        return repository.getUserById(userId);
    }

    public LiveData<Boolean> getUpdateResult() {
        return updateResult;
    }

    public LiveData<String> getPasswordChangeResult() {
        return passwordChangeResult;
    }

    /**
     * Cập nhật thông tin cơ bản (Tên, SĐT, Email)
     */
    public void updateProfile(UserEntity user) {
        repository.updateUser(user);
        updateResult.postValue(true);
    }

    /**
     * Đổi mật khẩu
     */
    public void changePassword(long userId, String oldPassword, String newPassword) {
        repository.getUserByIdSync(userId, user -> {
            if (user == null) {
                passwordChangeResult.postValue("Lỗi: không tìm thấy user");
                return;
            }

            // 1. Kiểm tra mật khẩu cũ
            if (!PasswordUtils.verifyPassword(oldPassword, user.passwordHash, user.passwordSalt)) {
                passwordChangeResult.postValue("Mật khẩu cũ không đúng");
                return;
            }

            // 2. Băm mật khẩu mới
            String newSalt = PasswordUtils.generateSalt();
            String newHash = PasswordUtils.hashPassword(newPassword, newSalt);
            user.passwordHash = newHash;
            user.passwordSalt = newSalt;
            user.updatedAt = System.currentTimeMillis();

            // 3. Cập nhật user
            repository.updateUser(user);
            passwordChangeResult.postValue("Đổi mật khẩu thành công");
        });
    }
}