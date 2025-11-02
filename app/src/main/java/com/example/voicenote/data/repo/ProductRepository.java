// File: com/example/voicenote/data/repo/ProductRepository.java
package com.example.voicenote.data.repo;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.voicenote.data.local.AppDatabase;
import com.example.voicenote.data.local.dao.ProductDao;
import com.example.voicenote.data.local.entity.ProductEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EN: Repository for managing products.
 * VI: Repository quản lý các sản phẩm (Product).
 * (Đã refactor từ QuickItemRepository)
 */
public class ProductRepository {
    private final ProductDao productDao;
    private final ExecutorService executor;

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.productDao = db.productDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * EN: Retrieve all products (LiveData for auto updates).
     * VI: Lấy danh sách sản phẩm (LiveData giúp UI tự động cập nhật).
     */
    public LiveData<List<ProductEntity>> getAllProducts() {
        return productDao.getAllProducts();
    }

    /**
     * EN: Insert a new product.
     * VI: Thêm mới một sản phẩm.
     */
    public void insertProduct(ProductEntity product) {
        executor.execute(() -> productDao.insertProduct(product));
    }

    /**
     * EN: Delete a product.
     * VI: Xoá một sản phẩm khỏi cơ sở dữ liệu.
     */
    public void deleteProduct(ProductEntity product) {
        executor.execute(() -> productDao.deleteProduct(product));
    }
}