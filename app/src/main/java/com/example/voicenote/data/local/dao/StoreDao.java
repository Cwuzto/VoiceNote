// File: com/example/voicenote/data/local/dao/StoreDao.java
package com.example.voicenote.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.voicenote.data.local.entity.StoreEntity;

@Dao
public interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStore(StoreEntity store);

    /**
     * Lấy cửa hàng bằng ID của chủ sở hữu
     */
    @Query("SELECT * FROM store WHERE owner_id = :ownerId LIMIT 1")
    StoreEntity getStoreByOwnerId(long ownerId);

    /**
     * Dùng LiveData để kiểm tra (nếu cần)
     */
    @Query("SELECT * FROM store WHERE owner_id = :ownerId LIMIT 1")
    LiveData<StoreEntity> getStoreByOwnerIdLiveData(long ownerId);
}