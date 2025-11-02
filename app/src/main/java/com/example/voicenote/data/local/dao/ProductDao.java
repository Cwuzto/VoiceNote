package com.example.voicenote.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.voicenote.data.local.entity.ProductEntity;

import java.util.List;

/**
 * DAO cho bảng Product (thay thế QuickItemDao)
 */
@Dao
public interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProduct(ProductEntity product);

    @Query("SELECT * FROM product ORDER BY name ASC")
    LiveData<List<ProductEntity>> getAllProducts();

    @Delete
    void deleteProduct(ProductEntity product);
}