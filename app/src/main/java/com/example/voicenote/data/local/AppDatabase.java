// File: com/example/voicenote/data/local/AppDatabase.java
package com.example.voicenote.data.local;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.annotation.NonNull;
import android.content.Context;

// Import 6 DAO
import com.example.voicenote.data.local.dao.BankAccountDao;
import com.example.voicenote.data.local.dao.OrderDao;
import com.example.voicenote.data.local.dao.OrderItemDao;
import com.example.voicenote.data.local.dao.ProductDao;
import com.example.voicenote.data.local.dao.StoreDao;
import com.example.voicenote.data.local.dao.UserDao;

// Import 6 Entity
import com.example.voicenote.data.local.entity.BankAccountEntity;
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.entity.ProductEntity;
import com.example.voicenote.data.local.entity.StoreEntity;
import com.example.voicenote.data.local.entity.UserEntity;

@Database(
        entities = {
                UserEntity.class,
                ProductEntity.class,
                OrderEntity.class,
                OrderItemEntity.class,
                BankAccountEntity.class,
                StoreEntity.class
        },
        version = 6, // Đảm bảo version này KHỚP với database bạn tạo
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // --- Khai báo 6 DAO ---
    public abstract UserDao userDao();
    public abstract StoreDao storeDao();
    public abstract ProductDao productDao();
    public abstract OrderDao orderDao();
    public abstract OrderItemDao orderItemDao();
    public abstract BankAccountDao bankAccountDao();

    private static volatile AppDatabase INSTANCE;

    // Tên file trong thư mục /assets/
    private static final String ASSET_DATABASE_NAME = "voicenote_db.db";
    // Tên file sẽ được tạo trên điện thoại
    private static final String DEVICE_DATABASE_NAME = "voicenote_db";

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DEVICE_DATABASE_NAME
                            )
                            // [QUAN TRỌNG] Báo Room sao chép từ assets
                            .createFromAsset(ASSET_DATABASE_NAME)

                            // (Vẫn giữ lại fallback đề phòng lỗi)
                            .fallbackToDestructiveMigration()

                            .build();
                }
            }
        }
        return INSTANCE;
    }
}