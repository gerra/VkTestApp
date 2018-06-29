package com.german.vktestapp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public final class ToolbarWrapper extends LinearLayout {
    public ToolbarWrapper(Context context) {
        this(context, null);
    }

    public ToolbarWrapper(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToolbarWrapper(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

//        if (index != 1) {
//            View divider = new View(getContext());
//            divider.setBackgroundColor(getResources().getColor(R.color.toolbarDivider));
//            int height = getResources().getDimensionPixelSize(R.dimen.toolbar_divider_height);
//            addView(divider, 1, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
//        }
    }
}
