package com.example.voicenote.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.voicenote.data.local.entity.QuickItemEntity;

import java.util.List;

/**
 * EN: DAO for quick access items displayed in the shortcut grid.
 * VI: DAO cho các sản phẩm nhanh hiển thị trong lưới chọn.
 */
@Dao
public interface QuickItemDao {

    /**
     * EN: Insert new quick item.
     * VI: Thêm một sản phẩm nhanh mới.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertQuickItem(QuickItemEntity item);

    /**
     * EN: Retrieve all quick items.
     * VI: Lấy danh sách tất cả sản phẩm nhanh.
     */
    @Query("SELECT * FROM quick_items ORDER BY id DESC")
    LiveData<List<QuickItemEntity>> getAllQuickItems();

    /**
     * EN: Delete a quick item.
     * VI: Xoá một sản phẩm nhanh.
     */
    @Delete
    void deleteQuickItem(QuickItemEntity item);
}