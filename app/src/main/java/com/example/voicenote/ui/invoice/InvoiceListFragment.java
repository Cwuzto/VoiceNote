package com.example.voicenote.ui.invoice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InvoiceAdapter((invoice, checked) -> viewModel.setPaid(incomeSafe(invoice), checked));
        rv.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(InvoiceListViewModel.class);
        viewModel.getAllInvoices().observe(getViewLifecycleOwner(), invoices -> {
            adapter.submit(invoices);
            tvEmpty.setVisibility(invoices == null || invoices.isEmpty() ? View.VISIBLE : View.GONE);
        });
        return v;
    }

    private com.example.voicenote.data.local.entity.InvoiceEntity incomeSafe(com.example.voicenote.data.local.entity.InvoiceEntity iv) {
        return iv; // placeholder for potential mapping
    }
}