// File: com/example/voicenote/ui/product/ProductListFragment.java (MỚI)
package com.example.voicenote.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.ProductEntity;
import com.example.voicenote.ui.dialog.AddProductSheet;
import com.example.voicenote.ui.product.adapter.ProductManagementAdapter;
import com.example.voicenote.vm.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProductListFragment extends Fragment {

    private ProductViewModel productViewModel;
    private ProductManagementAdapter adapter;
    private TextView tvEmpty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_product_list, container, false);

        tvEmpty = v.findViewById(R.id.tvEmpty);
        RecyclerView rvProducts = v.findViewById(R.id.rvProducts);
        FloatingActionButton fabAddProduct = v.findViewById(R.id.fabAddProduct);

        // --- Setup Adapter ---
        adapter = new ProductManagementAdapter(new ProductManagementAdapter.OnProductClickListener() {
            @Override
            public void onEditClick(ProductEntity product) {
                openAddEditDialog(product);
            }
            @Override
            public void onDeleteClick(ProductEntity product) {
                confirmDelete(product);
            }
        });
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);

        // --- Lắng nghe dữ liệu ---
        productViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            tvEmpty.setVisibility(products == null || products.isEmpty() ? View.VISIBLE : View.GONE);
            adapter.submitList(products);
        });

        // --- Listeners ---
        fabAddProduct.setOnClickListener(view -> openAddEditDialog(null));

        return v;
    }

    private void openAddEditDialog(ProductEntity product) {
        // (product) sẽ là null nếu bấm FAB (Thêm mới)
        // (product) sẽ có giá trị nếu bấm Sửa (Edit)
        AddProductSheet dialog = AddProductSheet.newInstance(product,
                (productToSave, name, price) -> {
                    ProductEntity productToDatabase;

                    if (productToSave != null) {
                        // Chế độ Sửa: TẠO MỘT OBJECT MỚI
                        productToDatabase = new ProductEntity();
                        // SAO CHÉP ID
                        productToDatabase.id = productToSave.id;
                    } else {
                        // Chế độ Thêm: Tạo product mới (id = 0)
                        productToDatabase = new ProductEntity();
                    }
                    // Gán dữ liệu mới
                    productToDatabase.name = name;
                    productToDatabase.price = price;

                    // Gửi object (mới toanh) này đến ViewModel
                    productViewModel.insertProduct(productToDatabase);
                });
        dialog.show(getChildFragmentManager(), "AddProductSheet");
    }

    private void confirmDelete(ProductEntity product) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá sản phẩm")
                .setMessage("Bạn có chắc muốn xoá " + product.name + "?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    productViewModel.deleteProduct(product);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}