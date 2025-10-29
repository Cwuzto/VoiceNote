package com.example.voicenote.data.repo;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.voicenote.data.local.AppDatabase;
import com.example.voicenote.data.local.dao.QuickItemDAO;
import com.example.voicenote.data.local.entity.QuickItemEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EN: Repository for managing quick access items (shortcut grid).
 * VI: Repository quản lý các sản phẩm nhanh hiển thị trong lưới chọn (shortcut grid).
 */
public class QuickItemRepository {
    private final QuickItemDAO quickItemDao;
    private final ExecutorService executor;

    public QuickItemRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.quickItemDao = db.quickItemDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * EN: Retrieve all quick items (LiveData for auto updates).
     * VI: Lấy danh sách sản phẩm nhanh (LiveData giúp UI tự động cập nhật).
     */
    public LiveData<List<QuickItemEntity>> getAllQuickItems() {
        return quickItemDao.getAllQuickItems();
    }

    /**
     * EN: Insert a new quick item.
     * VI: Thêm mới một sản phẩm nhanh.
     */
    public void insertQuickItem(QuickItemEntity item) {
        executor.execute(() -> quickItemDao.insertQuickItem(item));
    }

    /**
     * EN: Delete a quick item.
     * VI: Xoá một sản phẩm nhanh khỏi cơ sở dữ liệu.
     */
    public void deleteQuickItem(QuickItemEntity item) {
        executor.execute(() -> quickItemDao.deleteQuickItem(item));
    }
}