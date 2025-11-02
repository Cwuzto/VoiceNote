// File: com/example/voicenote/ui/invoice/OrderListFragment.java
package com.example.voicenote.ui.order;

import android.content.Context;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
// [SỬA] Import adapter và viewmodel mới
import com.example.voicenote.ui.order.adapter.OrderAdapter;
import com.example.voicenote.vm.OrderListViewModel;

/**
 * Fragment hiển thị danh sách Order (đã refactor từ InvoiceListFragment)
 */
public class OrderListFragment extends Fragment {
    private OrderListViewModel viewModel; // [SỬA]
    private OrderAdapter adapter; // [SỬA]

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_order_list, container, false);

        RecyclerView rv = v.findViewById(R.id.rvInvoice);
        TextView tvEmpty = v.findViewById(R.id.tvEmptyHint);
        View searchBar = v.findViewById(R.id.searchBar);
        EditText edtSearch = v.findViewById(R.id.edtSearch);
        TextView btnCancelSearch = v.findViewById(R.id.btnCancelSearch);
        ImageView btnSearch = v.findViewById(R.id.btnSearch);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // [SỬA] Khởi tạo adapter và viewmodel mới
        adapter = new OrderAdapter((order, checked) -> viewModel.updatePaymentStatus(order, checked));
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(OrderListViewModel.class);
        viewModel.getAllOrders().observe(getViewLifecycleOwner(), orders -> { // [SỬA]
            adapter.submit(orders); // [SỬA]
            tvEmpty.setVisibility(orders == null || orders.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // --- Xử lý hiển thị search bar ---
        btnSearch.setOnClickListener(v1 -> {
            searchBar.setVisibility(View.VISIBLE);
            btnSearch.setVisibility(View.GONE);
            edtSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
        });

        btnCancelSearch.setOnClickListener(v2 -> {
            searchBar.setVisibility(View.GONE);
            btnSearch.setVisibility(View.VISIBLE);
            edtSearch.setText("");
            edtSearch.clearFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
        });

        // --- Lọc danh sách khi người dùng nhập ---
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filterOrders(s.toString()); // [SỬA]
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return v;
    }

    // [XOÁ] Hàm incomeSafe không còn cần thiết
}