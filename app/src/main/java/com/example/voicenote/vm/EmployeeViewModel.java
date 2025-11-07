// File: com/example/voicenote/vm/EmployeeViewModel.java
package com.example.voicenote.vm;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.voicenote.data.local.entity.UserEntity;
import com.example.voicenote.data.repo.UserRepository;
import com.example.voicenote.util.PasswordUtils;
import java.util.List;

public class EmployeeViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final LiveData<List<UserEntity>> allEmployees;
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public EmployeeViewModel(@NonNull Application application) {
        super(application);
        this.repository = new UserRepository(application);
        this.allEmployees = repository.getAllEmployees();
    }

    public LiveData<List<UserEntity>> getAllEmployees() {
        return allEmployees;
    }

    public LiveData<Boolean> getSaveResult() {
        return saveResult;
    }

    /**
     * Thêm nhân viên mới
     */
    public void addEmployee(String fullName, String username, String password) {
        // (Trong thực tế, bạn cần kiểm tra username trùng, nhưng tạm thời bỏ qua)

        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(password, salt);

        UserEntity employee = new UserEntity();
        employee.fullName = fullName;
        employee.username = username;
        employee.passwordHash = hash;
        employee.passwordSalt = salt;
        employee.role = "EMPLOYEE"; // Gán role
        employee.isActive = true;
        employee.createdAt = System.currentTimeMillis();
        employee.updatedAt = System.currentTimeMillis();

        repository.insertUser(employee, new UserRepository.OnUserInsertedListener() {
            @Override public void onSuccess() { saveResult.postValue(true); }
            @Override public void onFailure(Exception e) { saveResult.postValue(false); }
        });
    }

    /**
     * Cập nhật thông tin nhân viên
     */
    public void updateEmployee(UserEntity employee, String newPassword) {
        employee.updatedAt = System.currentTimeMillis();

        // Nếu có nhập mật khẩu mới
        if (newPassword != null && !newPassword.isEmpty()) {
            String salt = PasswordUtils.generateSalt();
            String hash = PasswordUtils.hashPassword(newPassword, salt);
            employee.passwordHash = hash;
            employee.passwordSalt = salt;
        }
        // (Nếu không thì giữ nguyên mật khẩu cũ)

        repository.updateUser(employee);
        saveResult.postValue(true); // Giả định update luôn thành công
    }

    /**
     * Xoá nhân viên
     */
    public void deleteEmployee(UserEntity employee) {
        repository.deleteUser(employee);
    }
}