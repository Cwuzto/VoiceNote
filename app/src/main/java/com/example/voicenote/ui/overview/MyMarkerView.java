// File: com/example/voicenote/ui/overview/MyMarkerView.java
package com.example.voicenote.ui.overview;

import android.content.Context;
import android.widget.TextView;
import com.example.voicenote.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import java.util.Locale;

public class MyMarkerView extends MarkerView {

    private final TextView tvContent;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        // Ánh xạ TextView từ file chart_marker_view.xml
        tvContent = findViewById(R.id.tvMarkerContent);
    }

    // Được gọi mỗi khi Marker được vẽ lại
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        // Lấy giá trị Y (doanh thu) và định dạng nó
        long revenue = (long) e.getY();
        tvContent.setText(String.format(Locale.US, "%,dđ", revenue));
        super.refreshContent(e, highlight);
    }

    // Căn chỉnh vị trí (hiển thị popup bên trên điểm được chọn)
    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight() * 1.2f);
    }
}