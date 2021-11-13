package com.example.zgbeautyandhairstaff.Common;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacesItemDecorationGrid extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecorationGrid(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (parent.getChildLayoutPosition(view) %2 != 0)
        {
            outRect.top = 60;
            outRect.bottom = -50;
        }
    }
}