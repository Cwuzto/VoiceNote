// File: com/example/voicenote/vm/ProductViewModel.java
package com.example.voicenote.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.voicenote.data.local.entity.ProductEntity; // [SỬA]
import com.example.voicenote.data.local.rel.AlphabetHeaderItem;
import com.example.voicenote.data.repo.ProductRepository; // [SỬA]

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel cho danh sách sản phẩm (dùng trong SaleActivity).
 */
public class ProductViewModel extends AndroidViewModel {
    private final ProductRepository repository;
    private final LiveData<List<ProductEntity>> allProducts;
    private final MediatorLiveData<List<Object>> groupedProducts = new MediatorLiveData<>(); // LiveData mới để chứa danh sách đã nhóm

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        allProducts = repository.getAllProducts(); //nguồn dữ liệu gốc (List<ProductEntity>)
        groupedProducts.addSource(allProducts, products -> { //nguồn dữ liệu đã nhóm (List<Object>)
            groupedProducts.setValue(groupProductsAlphabetically(products));
        });
    }
    /**
     * Dùng cho ProductListFragment (Quản lý)
     */
    public LiveData<List<Object>> getAllProductsGrouped() {
        return groupedProducts;
    }

    /**
     * trả về danh sách sản phẩm (List<ProductEntity>) cho SaleActivity (QuickRV)
     */
    public LiveData<List<ProductEntity>> getAllProducts() {
        return allProducts;
    }

    /**
     * [MỚI] Hàm lưu có kiểm tra trùng lặp (cho ProductListFragment)
     * @return true nếu lưu thành công, false nếu tên bị trùng
     */
    public boolean saveProduct(ProductEntity productToSave) {
        List<ProductEntity> currentList = allProducts.getValue();
        if (currentList == null) {
            // Nếu LiveData chưa tải xong, cứ cho phép lưu
            // (DB sẽ tự bắt lỗi UNIQUE nếu có, nhưng hiếm)
            repository.insertProduct(productToSave);
            return true;
        }

        String newName = productToSave.name.trim();

        // 1. Kiểm tra trùng lặp (không phân biệt hoa thường)
        for (ProductEntity product : currentList) {
            // Nếu tên trùng VÀ ID khác (tức là đang sửa món này)
            if (product.name.equalsIgnoreCase(newName) && product.id != productToSave.id) {
                return false; // Báo lỗi: Đã tìm thấy trùng lặp
            }
        }

        // 2. Không trùng lặp, tiến hành lưu
        repository.insertProduct(productToSave);
        return true;
    }

    /**
     * Thêm sản phẩm mới.
     */
    public void insertProduct(ProductEntity product) {
        repository.insertProduct(product);
    }

    /**
     * EN: Delete product.
     * VI: Xoá sản phẩm.
     */
    public void deleteProduct(ProductEntity product) {
        repository.deleteProduct(product);
    }
    /**
     * Sắp xếp và nhóm danh sách theo chữ cái
     */
    private List<Object> groupProductsAlphabetically(List<ProductEntity> products) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Sắp xếp danh sách theo Tiếng Việt
        // Dùng Collator để "Đ" đứng sau "D"
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        products.sort((p1, p2) -> collator.compare(p1.name, p2.name));

        // 2. Nhóm
        List<Object> groupedList = new ArrayList<>();
        String currentLetter = "";

        for (ProductEntity product : products) {
            String firstLetter = product.name.substring(0, 1).toUpperCase(new Locale("vi"));

            // Nếu chữ cái thay đổi
            if (!firstLetter.equals(currentLetter)) {
                currentLetter = firstLetter;
                groupedList.add(new AlphabetHeaderItem(currentLetter)); // Thêm Header
            }
            groupedList.add(product); // Thêm Item
        }
        return groupedList;
    }
}