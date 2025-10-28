package com.example.voicenote.ui.invoice;

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
import com.example.voicenote.ui.invoice.adapter.InvoiceAdapter;
import com.example.voicenote.vm.InvoiceListViewModel;

public class InvoiceListFragment extends Fragment {
    private InvoiceListViewModel viewModel;
    private InvoiceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_invoice, container, false);

        RecyclerView rv = v.findViewById(R.id.rvInvoice);
        TextView tvEmpty = v.findViewById(R.id.tvEmptyHint);
        View searchBar = v.findViewById(R.id.searchBar);
        EditText edtSearch = v.findViewById(R.id.edtSearch);
        TextView btnCancelSearch = v.findViewById(R.id.btnCancelSearch);
        ImageView btnSearch = v.findViewById(R.id.btnSearch);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InvoiceAdapter((invoice, checked) -> viewModel.setPaid(incomeSafe(invoice), checked));
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(InvoiceListViewModel.class);
        viewModel.getAllInvoices().observe(getViewLifecycleOwner(), invoices -> {
            adapter.submit(invoices);
            tvEmpty.setVisibility(invoices == null || invoices.isEmpty() ? View.VISIBLE : View.GONE);
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
                viewModel.filterInvoices(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return v;
    }

    private com.example.voicenote.data.local.entity.InvoiceEntity incomeSafe(com.example.voicenote.data.local.entity.InvoiceEntity iv) {
        return iv; // placeholder for potential mapping
    }
}