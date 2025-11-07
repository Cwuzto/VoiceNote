package com.example.voicenote.util;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 512;
    private static final int SALT_LENGTH = 16;

    // [MỚI] Định nghĩa cờ (flag) để sử dụng
    // Dùng NO_WRAP để đảm bảo không có ký tự ngắt dòng trong chuỗi lưu vào DB
    private static final int BASE64_FLAGS = Base64.NO_WRAP;

    /**
     * Tạo một chuỗi Salt ngẫu nhiên
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        // [SỬA] Gọi hàm static encodeToString với cờ (flag)
        return Base64.encodeToString(salt, BASE64_FLAGS);
    }

    /**
     * Băm mật khẩu với một Salt cho trước
     */
    public static String hashPassword(String password, String salt) {
        try {
            // [SỬA] Gọi hàm static decode với cờ (flag)
            byte[] saltBytes = Base64.decode(salt, BASE64_FLAGS);

            KeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    saltBytes,
                    ITERATIONS,
                    KEY_LENGTH
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // [SỬA] Gọi hàm static encodeToString với cờ (flag)
            return Base64.encodeToString(hash, BASE64_FLAGS);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Lỗi khi băm mật khẩu", e);
        }
    }

    /**
     * Kiểm tra mật khẩu (đầu vào) với hash (đã lưu) và salt (đã lưu)
     */
    public static boolean verifyPassword(String passwordAttempt, String storedHash, String storedSalt) {
        // Băm mật khẩu người dùng nhập với ĐÚNG salt đã lưu
        String newHash = hashPassword(passwordAttempt, storedSalt);

        // So sánh 2 hash
        return newHash.equals(storedHash);
    }
}