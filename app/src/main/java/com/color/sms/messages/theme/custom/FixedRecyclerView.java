package com.color.sms.messages.theme.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

public class FixedRecyclerView extends RecyclerView {
    public FixedRecyclerView(Context context) {
        super(context);
    }

    public FixedRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        // check if scrolling up
        if (direction < 1) {
            boolean original = super.canScrollVertically(direction);
            return original || getChildAt(0) != null && getChildAt(0).getTop() < 0;
        }
        return super.canScrollVertically(direction);

    }
}