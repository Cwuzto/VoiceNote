// File: com/example/voicenote/vm/BestSellerViewModel.java
package com.example.voicenote.vm;

import static java.text.Normalizer.normalize;

import android.app.Application;
import androidx.core.util.Pair;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.voicenote.data.local.rel.BestSellerItem;
import com.example.voicenote.data.repo.OrderRepository;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BestSellerViewModel extends AndroidViewModel {

    private final OrderRepository repository;
    private final MutableLiveData<Pair<Long, Long>> timeRange = new MutableLiveData<>();
    private final MutableLiveData<String> sortCriteria = new MutableLiveData<>("QUANTITY");
    private final MutableLiveData<String> searchKeyword = new MutableLiveData<>("");

    private LiveData<List<BestSellerItem>> currentSource;
    private final MediatorLiveData<List<BestSellerItem>> bestSellersList = new MediatorLiveData<>();

    public BestSellerViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);

        // Mặc định là "Tháng này"
        timeRange.setValue(calculateTimeRange("THIS_MONTH"));

        // Lắng nghe cả 3
        bestSellersList.addSource(timeRange, range -> updateQuery());
        bestSellersList.addSource(sortCriteria, sort -> updateQuery());
        bestSellersList.addSource(searchKeyword, keyword -> updateQuery());
    }

    // --- Getter ---
    public LiveData<List<BestSellerItem>> getBestSellersList() {
        return bestSellersList;
    }

    // --- Setters ---
    public void setTimeRange(String rangeKey) {
        timeRange.setValue(calculateTimeRange(rangeKey));
    }
    public void setTimeRangeCustom(long startTime, long endTime) {
        timeRange.setValue(new Pair<>(startTime, endTime));
    }
    public void setSortCriteria(String sort) {
        sortCriteria.setValue(sort);
    }
    public void setSearchKeyword(String keyword) {
        searchKeyword.setValue(keyword);
    }

    // Hàm này sẽ tự động gọi đúng query
    private void updateQuery() {
        Pair<Long, Long> range = timeRange.getValue();
        String sort = sortCriteria.getValue();
        String keyword = searchKeyword.getValue();

        if (range == null || sort == null || keyword == null) return;

        // Xóa nguồn cũ
        if (currentSource != null) {
            bestSellersList.removeSource(currentSource);
        }

        // Chọn nguồn mới từ Repository (đã lọc Time và Sort)
        if ("REVENUE".equals(sort)) {
            currentSource = repository.getBestSellersByRevenue(range.first, range.second);
        } else {
            currentSource = repository.getBestSellersByQuantity(range.first, range.second);
        }

        // 2. Lọc theo Keyword (bằng Java)
        bestSellersList.addSource(currentSource, data -> {
            if (keyword.isEmpty()) {
                bestSellersList.setValue(data); // Không tìm -> Hiển thị tất cả
            } else {
                List<BestSellerItem> filteredList = new ArrayList<>();
                String normalizedKeyword = normalize(keyword);
                for (BestSellerItem item : data) {
                    if (normalize(item.productName).contains(normalizedKeyword)) {
                        filteredList.add(item);
                    }
                }
                bestSellersList.setValue(filteredList);
            }
        });
    }

    /**
     * Chuẩn hoá Tiếng Việt (bỏ dấu) để tìm kiếm
     */
    private String normalize(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase(Locale.US);
    }

    /**
     * Tính toán startTime dựa trên Key, hàm này sẽ trả về 1 Pair (Start/End)
     */
    private androidx.core.util.Pair<Long, Long> calculateTimeRange(String rangeKey) {
        Calendar cal = Calendar.getInstance();
        long startTime, endTime;

        // Set endTime về 23:59:59 của ngày HÔM NAY
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        endTime = cal.getTimeInMillis();

        // Set startTime về 00:00:00 của ngày HÔM NAY
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startTime = cal.getTimeInMillis();

        switch(rangeKey) {
            case "TODAY":
                // Đã set (Hôm nay 00:00 -> Hôm nay 23:59)
                break;
            case "YESTERDAY":
                cal.add(Calendar.DAY_OF_YEAR, -1); // Về 00:00 hôm qua
                startTime = cal.getTimeInMillis();
                cal.set(Calendar.HOUR_OF_DAY, 23); // Về 23:59 hôm qua
                cal.set(Calendar.MINUTE, 59);
                endTime = cal.getTimeInMillis();
                break;
            case "7DAYS":
                cal.add(Calendar.DAY_OF_YEAR, -6); // Về 00:00 của 6 ngày trước
                startTime = cal.getTimeInMillis();
                // endTime là hôm nay 23:59 (đã set)
                break;
            case "THIS_MONTH":
                cal.set(Calendar.DAY_OF_MONTH, 1); // Về 00:00 ngày 1
                startTime = cal.getTimeInMillis();
                // endTime là hôm nay 23:59 (đã set)
                break;
            case "LAST_MONTH":
                cal.set(Calendar.DAY_OF_MONTH, 1); // Về 00:00 ngày 1 tháng này
                cal.add(Calendar.MILLISECOND, -1); // Về 23:59 ngày cuối tháng TRƯỚC
                endTime = cal.getTimeInMillis();
                cal.set(Calendar.DAY_OF_MONTH, 1); // Về 00:00 ngày 1 tháng TRƯỚC
                startTime = cal.getTimeInMillis();
                break;
            case "THIS_YEAR":
                cal.set(Calendar.DAY_OF_YEAR, 1); // Về 00:00 ngày 1/1
                startTime = cal.getTimeInMillis();
                // endTime là hôm nay 23:59 (đã set)
                break;
            default: // "ALL"
                startTime = 0;
                endTime = System.currentTimeMillis(); // (Mặc dù query ko dùng)
                break;
        }
        return new androidx.core.util.Pair<>(startTime, endTime);
    }
}