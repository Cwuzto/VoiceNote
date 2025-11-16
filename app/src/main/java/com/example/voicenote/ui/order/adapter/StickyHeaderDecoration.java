// File: com/example/voicenote/ui/order/adapter/StickyHeaderDecoration.java (ĐÃ SỬA LỖI CĂN LỀ)
package com.example.voicenote.ui.order.adapter;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.R;
import com.example.voicenote.data.local.rel.OrderHeaderItem;

public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {

    private final OrderAdapter adapter;
    private final LinearLayoutManager layoutManager;
    private View headerView;
    private OrderAdapter.VHHeader headerViewHolder;
    private Drawable stickyHeaderBackground;

    public StickyHeaderDecoration(OrderAdapter adapter, LinearLayoutManager layoutManager) {
        this.adapter = adapter;
        this.layoutManager = layoutManager;
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        View firstVisibleView = layoutManager.findViewByPosition(layoutManager.findFirstVisibleItemPosition());
        if (firstVisibleView == null || adapter.getItemCount() == 0) {
            return;
        }

        int firstVisiblePos = layoutManager.findFirstVisibleItemPosition();

        if (adapter.getItemViewType(firstVisiblePos) == 0 && firstVisibleView.getTop() >= 0) {
            return;
        }

        OrderHeaderItem headerData = adapter.getHeaderDataForPosition(firstVisiblePos);
        if (headerData == null) {
            return;
        }

        prepareHeaderView(parent, headerData);

        float translationY = 0;

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int adapterPos = parent.getChildAdapterPosition(child);

            // [SỬA] Thêm parent.getPaddingLeft() vào phép so sánh
            int childLeft = child.getLeft();
            if (adapterPos != -1 && adapter.getItemViewType(adapterPos) == 0 && child.getTop() <= headerView.getHeight() && child.getTop() > 0) {
                translationY = child.getTop() - headerView.getHeight();
                break;
            }
        }

        c.save();

        // [SỬA LỖI CĂN LỀ] Dịch chuyển header ghim sang phải
        // bằng đúng padding của RecyclerView
        c.translate(parent.getPaddingLeft(), translationY);

        stickyHeaderBackground.setBounds(0, 0, headerView.getMeasuredWidth(), headerView.getMeasuredHeight());
        stickyHeaderBackground.draw(c);

        headerView.draw(c);
        c.restore();
    }

    private void prepareHeaderView(RecyclerView parent, OrderHeaderItem headerData) {
        if (headerView == null) {
            headerView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_header, parent, false);
            headerViewHolder = new OrderAdapter.VHHeader(headerView);

            stickyHeaderBackground = ContextCompat.getDrawable(parent.getContext(), R.drawable.bg_soft_gradient);

            // [SỬA LỖI CĂN LỀ] Đo chiều rộng của header
            // bằng chiều rộng của parent TRỪ ĐI padding trái và phải
            int widthSpec = View.MeasureSpec.makeMeasureSpec(
                    parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight(),
                    View.MeasureSpec.EXACTLY
            );
            int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

            headerView.measure(widthSpec, heightSpec);
            headerView.layout(0, 0, headerView.getMeasuredWidth(), headerView.getMeasuredHeight());
        }

        headerViewHolder.bind(headerData);
    }
}