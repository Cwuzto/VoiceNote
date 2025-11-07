// File: com/example/voicenote/vm/AuthViewModel.java
package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.voicenote.data.local.entity.UserEntity;
import com.example.voicenote.data.repo.StoreRepository;
import com.example.voicenote.data.repo.UserRepository;
import com.example.voicenote.util.PasswordUtils;

public class AuthViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;



    // [SỬA] Dùng LiveData  để quản lý điều hướng
    private final MutableLiveData<LoginNavigationEvent> loginNavigationEvent = new MutableLiveData<>();
    // LiveData cho kết quả Đăng ký
    private final MutableLiveData<Boolean> registrationResult = new MutableLiveData<>();
    private final MutableLiveData<String> loginError = new MutableLiveData<>(); // [MỚI]

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
        this.storeRepository = new  StoreRepository(application);
    }

    // --- Getters ---
    public LiveData<LoginNavigationEvent> getLoginNavigationEvent() {
        return loginNavigationEvent;
    }

    public LiveData<Boolean> getRegistrationResult() {
        return registrationResult;
    }

    public LiveData<String> getLoginError() { // [MỚI]
        return loginError;
    }

    /**
     * Xử lý logic đăng nhập
     */
    public void login(String username, String password) {
        userRepository.findByUsername(username, user -> {
            if (user == null || !user.isActive) {
                loginError.postValue("Sai tên đăng nhập hoặc mật khẩu"); // [MỚI]
                return;
            }

            if (PasswordUtils.verifyPassword(password, user.passwordHash, user.passwordSalt)) {
                // Mật khẩu đúng! Giờ kiểm tra xem Owner này có cửa hàng chưa

                if ("OWNER".equals(user.role)) {
                    // Nếu là OWNER, kiểm tra cửa hàng
                    checkStoreExists(user);
                } else {
                    // Nếu là EMPLOYEE, vào thẳng MainActivity (logic sau này)
                    // Tạm thời cứ cho vào MainActivity
                    loginNavigationEvent.postValue(new LoginNavigationEvent(Destination.MAIN_ACTIVITY, user.id));
                }
            } else {
                loginError.postValue("Sai tên đăng nhập hoặc mật khẩu"); // [MỚI]
            }
        });
    }

    /**
     * [MỚI] Hàm kiểm tra cửa hàng (chạy sau khi login thành công)
     */
    private void checkStoreExists(UserEntity owner) {
        storeRepository.getStoreByOwnerId(owner.id, store -> {
            if (store != null) {
                // Đã có cửa hàng -> Vào Main
                loginNavigationEvent.postValue(new LoginNavigationEvent(Destination.MAIN_ACTIVITY, owner.id));
            } else {
                // Chưa có cửa hàng -> Vào Tạo Cửa Hàng
                loginNavigationEvent.postValue(new LoginNavigationEvent(Destination.CREATE_STORE, owner.id));
            }
        });
    }

    /**
     * Xử lý logic đăng ký
     */
    public void register(String fullName, String username, String password) {
        userRepository.findByUsername(username, existingUser -> {
            if (existingUser != null) {
                registrationResult.postValue(false); // Username đã tồn tại
                return;
            }

            // [SỬA] Tạo Salt và Hash
            String salt = PasswordUtils.generateSalt();
            String hash = PasswordUtils.hashPassword(password, salt);

            UserEntity newUser = new UserEntity();
            newUser.fullName = fullName;
            newUser.username = username;
            newUser.passwordHash = hash; // Lưu hash
            newUser.passwordSalt = salt; // Lưu salt
            newUser.role = "OWNER";
            newUser.isActive = true;
            newUser.createdAt = System.currentTimeMillis();
            newUser.updatedAt = System.currentTimeMillis();
            userRepository.insertUser(newUser, new UserRepository.OnUserInsertedListener() {
                @Override
                public void onSuccess() {
                    registrationResult.postValue(true); //đăng ký thành công
                }

                @Override
                public void onFailure(Exception e) {
                    registrationResult.postValue(false); //lỗi không đăng ký dc
                }
            });
        });
    }

    // --- [MỚI] Lớp Helper và Enum để điều hướng ---
    public enum Destination {
        MAIN_ACTIVITY,
        CREATE_STORE
    }

    public static class LoginNavigationEvent {
        public final Destination destination;
        public final long userId;

        public LoginNavigationEvent(Destination destination, long userId) {
            this.destination = destination;
            this.userId = userId;
        }
    }
}