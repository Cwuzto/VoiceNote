package com.example.voicenote.data.local;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.content.Context;

// [SỬA] Import các DAO và Entity mới
import com.example.voicenote.data.local.dao.BankAccountDao;
import com.example.voicenote.data.local.dao.OrderDao;
import com.example.voicenote.data.local.dao.OrderItemDao;
import com.example.voicenote.data.local.dao.ProductDao;
import com.example.voicenote.data.local.dao.UserDao;
import com.example.voicenote.data.local.entity.BankAccountEntity;
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.entity.ProductEntity;
import com.example.voicenote.data.local.entity.UserEntity;

/**
 * EN: Central Room database class for VoiceNote app.
 * VI: Lớp cơ sở dữ liệu trung tâm của ứng dụng VoiceNote, quản lý toàn bộ bảng và DAO.
 */
@Database(
        // [SỬA] Khai báo 5 entity mới
        entities = {
                UserEntity.class,
                ProductEntity.class,
                OrderEntity.class,
                OrderItemEntity.class,
                BankAccountEntity.class
        },
        version = 1, // Bạn có thể cần tăng version nếu migrate
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // --- [SỬA] Khai báo 5 DAO mới ---
    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract OrderDao orderDao();
    public abstract OrderItemDao orderItemDao();
    public abstract BankAccountDao bankAccountDao();
    // --- (Xoá các DAO cũ: invoiceDao, lineItemDao, quickItemDao) ---


    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "voicenote_db"
                            )
                            // [QUAN TRỌNG] Vì bạn thay đổi schema,
                            // bạn cần .fallbackToDestructiveMigration()
                            // để xoá DB cũ và tạo lại.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}