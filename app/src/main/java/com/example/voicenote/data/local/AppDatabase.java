package com.example.voicenote.data.local;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.content.Context;

import com.example.voicenote.data.local.dao.InvoiceDAO;
import com.example.voicenote.data.local.dao.LineItemDAO;
import com.example.voicenote.data.local.dao.QuickItemDAO;
import com.example.voicenote.data.local.entity.InvoiceEntity;
import com.example.voicenote.data.local.entity.LineItemEntity;
import com.example.voicenote.data.local.entity.QuickItemEntity;

/**
 * EN: Central Room database class for VoiceNote app.
 * VI: Lớp cơ sở dữ liệu trung tâm của ứng dụng VoiceNote, quản lý toàn bộ bảng và DAO.
 */
@Database(
        entities = {InvoiceEntity.class, LineItemEntity.class, QuickItemEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // --- DAO accessors ---
    public abstract InvoiceDAO invoiceDao();

    public abstract LineItemDAO lineItemDao();

    public abstract QuickItemDAO quickItemDao();

    private static volatile AppDatabase INSTANCE;

    /**
     * EN: Singleton pattern to get a single instance of the database.
     * VI: Áp dụng Singleton để tạo một thể hiện duy nhất của cơ sở dữ liệu.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "voicenote_db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}