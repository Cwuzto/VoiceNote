package com.example.voicenote.data.repo;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.voicenote.data.local.AppDatabase;
import com.example.voicenote.data.local.dao.StoreDao;
import com.example.voicenote.data.local.entity.StoreEntity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoreRepository {
    private final StoreDao storeDao;
    private final ExecutorService executor;

    public StoreRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.storeDao = db.storeDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Lấy cửa hàng bằng OwnerId (chạy background)
     */
    public void getStoreByOwnerId(long ownerId, OnStoreFoundListener listener) {
        executor.execute(() -> {
            StoreEntity store = storeDao.getStoreByOwnerId(ownerId);
            listener.onFound(store);
        });
    }

    // Lấy LiveData Store (cho StoreInfoActivity)
    public LiveData<StoreEntity> getStoreByOwnerIdLiveData(long ownerId) {
        return storeDao.getStoreByOwnerIdLiveData(ownerId);
    }

    // Thêm hàm update
    public void updateStore(StoreEntity store) {
        executor.execute(() -> storeDao.insertStore(store)); // insert (REPLACE)
    }

    /**
     * Thêm cửa hàng mới (chạy background)
     */
    public void insertStore(StoreEntity store, Runnable onFinished) {
        executor.execute(() -> {
            storeDao.insertStore(store);
            onFinished.run();
        });
    }

    public interface OnStoreFoundListener {
        void onFound(StoreEntity store);
    }
}