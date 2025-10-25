package com.example.voicenote.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voicenote.R;
import com.example.voicenote.adapter.InvoiceAdapter;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class InvoiceFragment extends Fragment {

    private TextView chipTime, chipStatus, tvEmptyHint, tvTodayTotal;
    private RecyclerView rv;
    private InvoiceAdapter adapter;
    private View searchBar;
    private EditText edtSearch;
    private TextView btnCancelSearch;
    private ImageView btnSearch;


    // dữ liệu mẫu (sau bạn nối DB)
    private final List<Invoice> all = new ArrayList<>();

    enum TimeFilter { ALL, YESTERDAY, LAST7, THIS_MONTH, LAST_MONTH, CUSTOM }
    enum StatusFilter { ALL, PAID, UNPAID }

    private TimeFilter timeFilter = TimeFilter.ALL;
    private StatusFilter statusFilter = StatusFilter.ALL;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_invoice, parent, false);

        chipTime   = v.findViewById(R.id.chipTime);
        chipStatus = v.findViewById(R.id.chipStatus);
        tvEmptyHint= v.findViewById(R.id.tvEmptyHint);
        tvTodayTotal = v.findViewById(R.id.tvTodayTotal);
        rv = v.findViewById(R.id.rvInvoice);
        searchBar = v.findViewById(R.id.searchBar);
        edtSearch = v.findViewById(R.id.edtSearch);
        btnCancelSearch = v.findViewById(R.id.btnCancelSearch);
        btnSearch = v.findViewById(R.id.btnSearch);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InvoiceAdapter(this::onPaidToggle);
        rv.setAdapter(adapter);

        chipTime.setOnClickListener(this::showTimeMenu);
        chipStatus.setOnClickListener(this::showStatusMenu);
        ImageView btnSearch = v.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v1 -> showSearchBar());
        btnCancelSearch.setOnClickListener(v12 -> hideSearchBar());

        edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { applyFilter(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        seedDemoData();   // dữ liệu mẫu
        applyFilter();

        return v;
    }

    private void seedDemoData() {
        if (!all.isEmpty()) return;
        // hôm nay
        all.add(Invoice.make("Khách lẻ", nowMillis() - 5*60_000, 100_000,
                new String[]{"5 x 3 rọi"}, false));
        // hôm qua
        all.add(Invoice.make("khách 1", nowMillis() - oneDayMs() - 23*60_000, 70_000,
                new String[]{"1 x Phở bò", "1 x Bún bò"}, false));
        // hôm qua khác
        all.add(Invoice.make("bàn 1", nowMillis() - oneDayMs() - 6*60_000, 70_000,
                new String[]{"1 x Phở bò", "1 x Bún bò"}, true));
    }

    private void showTimeMenu(View anchor){
        PopupMenu pm = new PopupMenu(requireContext(), anchor);
        pm.getMenu().add(0,1,0,"Tất cả");
        pm.getMenu().add(0,2,1,"Hôm qua");
        pm.getMenu().add(0,3,2,"7 ngày qua");
        pm.getMenu().add(0,4,3,"Tháng này");
        pm.getMenu().add(0,5,4,"Tháng trước");
        pm.getMenu().add(0,6,5,"Tuỳ chỉnh");
        pm.setOnMenuItemClickListener(mi -> {
            switch (mi.getItemId()){
                case 1: timeFilter=TimeFilter.ALL; chipTime.setText("Tất cả"); break;
                case 2: timeFilter=TimeFilter.YESTERDAY; chipTime.setText("Hôm qua"); break;
                case 3: timeFilter=TimeFilter.LAST7; chipTime.setText("7 ngày qua"); break;
                case 4: timeFilter=TimeFilter.THIS_MONTH; chipTime.setText("Tháng này"); break;
                case 5: timeFilter=TimeFilter.LAST_MONTH; chipTime.setText("Tháng trước"); break;
                case 6: timeFilter=TimeFilter.CUSTOM; chipTime.setText("Tuỳ chỉnh"); /* TODO: date range picker */ break;
            }
            applyFilter();
            return true;
        });
        pm.show();
    }

    private void showStatusMenu(View anchor){
        PopupMenu pm = new PopupMenu(requireContext(), anchor);
        pm.getMenu().add(0,1,0,"Tất cả");
        pm.getMenu().add(0,2,1,"Đã nhận tiền");
        pm.getMenu().add(0,3,2,"Chưa nhận tiền");
        pm.setOnMenuItemClickListener(mi -> {
            switch (mi.getItemId()){
                case 1: statusFilter=StatusFilter.ALL; chipStatus.setText("Tất cả"); break;
                case 2: statusFilter=StatusFilter.PAID; chipStatus.setText("Đã nhận tiền"); break;
                case 3: statusFilter=StatusFilter.UNPAID; chipStatus.setText("Chưa nhận tiền"); break;
            }
            applyFilter();
            return true;
        });
        pm.show();
    }

    private void applyFilter() {
        String q = edtSearch != null ? edtSearch.getText().toString().trim().toLowerCase() : "";
        long now = nowMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);

        List<Invoice> filtered = new ArrayList<>();
        for (Invoice iv : all){
            if (statusFilter==StatusFilter.PAID && !iv.paid) continue;
            if (statusFilter==StatusFilter.UNPAID && iv.paid) continue;

            if (timeFilter!=TimeFilter.ALL){
                if (timeFilter==TimeFilter.YESTERDAY){
                    long start = startOfDay(now - oneDayMs());
                    long end   = startOfDay(now);
                    if (!(iv.time >= start && iv.time < end)) continue;
                } else if (timeFilter==TimeFilter.LAST7){
                    long start = now - TimeUnit.DAYS.toMillis(7);
                    if (iv.time < start) continue;
                } else if (timeFilter==TimeFilter.THIS_MONTH){
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.DAY_OF_MONTH,1);
                    setZeroHMS(c);
                    long start = c.getTimeInMillis();
                    if (iv.time < start) continue;
                } else if (timeFilter==TimeFilter.LAST_MONTH){
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.MONTH,-1);
                    c.set(Calendar.DAY_OF_MONTH,1);
                    setZeroHMS(c);
                    long start = c.getTimeInMillis();
                    Calendar d = Calendar.getInstance();
                    d.set(Calendar.DAY_OF_MONTH,1);
                    setZeroHMS(d);
                    long end = d.getTimeInMillis();
                    if (!(iv.time>=start && iv.time<end)) continue;
                }
                // CUSTOM: bỏ qua ở bản demo
            }
            // lọc theo text: tên khách hoặc dòng hàng hoá
            if (!q.isEmpty()) {
                boolean match = (iv.customer != null && iv.customer.toLowerCase().contains(q));
                if (!match && iv.lines != null) {
                    for (String s: iv.lines) {
                        if (s.toLowerCase().contains(q)) { match = true; break; }
                    }
                }
                if (!match) continue;
            }
            filtered.add(iv);
        }

        Collections.sort(filtered, (a,b)-> Long.compare(b.time, a.time));
        adapter.submit(listWithHeaders(filtered));

        // tổng hôm nay
        long startToday = startOfDay(now);
        long totalToday = 0;
        for (Invoice iv: filtered) if (iv.time>=startToday) totalToday += iv.total;
        tvTodayTotal.setText(totalToday>0? formatMoney(totalToday): "");

        tvEmptyHint.setVisibility(filtered.isEmpty()? View.VISIBLE: View.GONE);
    }

    private List<InvoiceRow> listWithHeaders(List<Invoice> src){
        List<InvoiceRow> out = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi","VN"));
        long curDay = -1;
        for (Invoice iv: src){
            long day = startOfDay(iv.time);
            if (day != curDay){
                curDay = day;
                out.add(InvoiceRow.header(df.format(new Date(day))));
            }
            out.add(InvoiceRow.item(iv));
        }
        return out;
    }

    private void onPaidToggle(Invoice iv, boolean checked){
        iv.paid = checked;
        // TODO: cập nhật DB
    }

    // ================= utils & models ================
    private static long nowMillis(){ return System.currentTimeMillis(); }
    private static long oneDayMs(){ return TimeUnit.DAYS.toMillis(1); }
    private static long startOfDay(long t){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(t); setZeroHMS(c);
        return c.getTimeInMillis();
    }
    private static void setZeroHMS(Calendar c){
        c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0);
    }
    public static String formatMoney(long v){
        return String.format(Locale.US, "%,d", v);
    }

    public static class Invoice {
        public String customer; public String[] lines;
        public long time; public long total; public boolean paid;
        public static Invoice make(String c, long t, long total, String[] lines, boolean paid){
            Invoice i = new Invoice(); i.customer=c; i.time=t; i.total=total; i.lines=lines; i.paid=paid; return i;
        }
    }

    // hàng cho adapter (header / item)
    public static class InvoiceRow {
        public static final int TYPE_HEADER=0, TYPE_ITEM=1;
        public int type; public String header; public Invoice item;
        public static InvoiceRow header(String h){ InvoiceRow r=new InvoiceRow(); r.type=TYPE_HEADER; r.header=h; return r; }
        public static InvoiceRow item(Invoice i){ InvoiceRow r=new InvoiceRow(); r.type=TYPE_ITEM; r.item=i; return r; }
    }

    private void showSearchBar() {
        searchBar.setVisibility(View.VISIBLE);
        edtSearch.requestFocus();
        // hiện bàn phím
        edtSearch.post(() -> {
            android.view.inputmethod.InputMethodManager im =
                    (android.view.inputmethod.InputMethodManager) requireContext()
                            .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            im.showSoftInput(edtSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        });
    }

    private void hideSearchBar() {
        edtSearch.setText("");
        searchBar.setVisibility(View.GONE);
        // ẩn bàn phím
        View v = requireActivity().getCurrentFocus();
        if (v != null) {
            android.view.inputmethod.InputMethodManager im =
                    (android.view.inputmethod.InputMethodManager) requireContext()
                            .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        applyFilter(); // bỏ text -> về trạng thái cũ
    }

    //khi searchBar đang mở, bấm Back sẽ đóng search thay vì thoát Fragment.
    @Override public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override public void handleOnBackPressed() {
                        if (searchBar.getVisibility() == View.VISIBLE) {
                            hideSearchBar();
                        } else {
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                }
        );
    }
}
