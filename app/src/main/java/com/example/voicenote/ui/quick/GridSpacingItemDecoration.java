// File: com/example/voicenote/ui/quick/GridSpacingItemDecoration.java
package com.example.voicenote.ui.quick;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount, spacing, includeEdge;
    public GridSpacingItemDecoration(int spanCount, int spacingPx, boolean includeEdge){
        this.spanCount = spanCount; this.spacing = spacingPx; this.includeEdge = includeEdge?1:0;
    }
    @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int pos = parent.getChildAdapterPosition(view);
        int column = pos % spanCount;
        if (includeEdge==1) {
            outRect.left  = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;
            if (pos < spanCount) outRect.top = spacing;
            outRect.bottom = spacing;
        } else {
            outRect.left  = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;
            if (pos >= spanCount) outRect.top = spacing;
        }
    }
}
