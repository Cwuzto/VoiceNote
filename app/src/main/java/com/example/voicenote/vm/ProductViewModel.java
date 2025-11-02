// File: com/example/voicenote/vm/ProductViewModel.java
package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.voicenote.data.local.entity.ProductEntity; // [SỬA]
import com.example.voicenote.data.repo.ProductRepository; // [SỬA]

import java.util.List;

/**
 * EN: ViewModel for product grid (used in SaleActivity).
 * VI: ViewModel cho danh sách sản phẩm (dùng trong SaleActivity).
 * (Đã refactor từ QuickItemsViewModel)
 */
public class ProductViewModel extends AndroidViewModel {
    private final ProductRepository repository; // [SỬA]
    private final LiveData<List<ProductEntity>> allProducts; // [SỬA]

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application); // [SỬA]
        allProducts = repository.getAllProducts(); // [SỬA]
    }

    /**
     * EN: Return LiveData of products for observing.
     * VI: Trả về LiveData danh sách sản phẩm để UI quan sát.
     */
    public LiveData<List<ProductEntity>> getAllProducts() { // [SỬA]
        return allProducts;
    }

    /**
     * EN: Insert new product.
     * VI: Thêm sản phẩm mới.
     */
    public void insertProduct(ProductEntity product) { // [SỬA]
        repository.insertProduct(product); // [SỬA]
    }

    /**
     * EN: Delete product.
     * VI: Xoá sản phẩm.
     */
    public void deleteProduct(ProductEntity product) { // [SỬA]
        repository.deleteProduct(product); // [SỬA]
    }
}