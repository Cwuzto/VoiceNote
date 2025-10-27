package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voicenote.data.local.entity.QuickItemEntity;
import com.example.voicenote.data.repo.QuickItemRepository;

import java.util.List;

/**
 * EN: ViewModel for quick item grid (bottomsheet or voice command list).
 * VI: ViewModel cho danh sách sản phẩm nhanh (hiển thị trong bottomsheet hoặc lưới).
 */
public class QuickItemsViewModel extends AndroidViewModel {
    private final QuickItemRepository repository;
    private final LiveData<List<QuickItemEntity>> allQuickItems;

    public QuickItemsViewModel(@NonNull Application application) {
        super(application);
        repository = new QuickItemRepository(application);
        allQuickItems = repository.getAllQuickItems();
    }

    /**
     * EN: Return LiveData of quick items for observing.
     * VI: Trả về LiveData danh sách sản phẩm nhanh để UI quan sát.
     */
    public LiveData<List<QuickItemEntity>> getQuickItems() {
        return allQuickItems;
    }

    /**
     * EN: Insert new quick item.
     * VI: Thêm sản phẩm nhanh mới.
     */
    public void addQuickItem(QuickItemEntity item) {
        repository.insertQuickItem(item);
    }

    /**
     * EN: Delete quick item.
     * VI: Xoá sản phẩm nhanh.
     */
    public void deleteQuickItem(QuickItemEntity item) {
        repository.deleteQuickItem(item);
    }
}
