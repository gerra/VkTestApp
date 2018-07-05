package com.german.vktestapp.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Px;
import android.support.v7.widget.AppCompatImageView;

// Fixed width
public class FixedWidthImageView extends AppCompatImageView {
    private static final int UNSET = -1;

    @Px
    private int mWidth;

    public FixedWidthImageView(Context context) {
        super(context);
    }

    public void setWidth(@Px int width) {
        mWidth = width;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mWidth != UNSET) {
            Drawable drawable = getDrawable();

            if (drawable != null) {
                int drawableWidth = drawable.getIntrinsicWidth();
                int drawableHeight = drawable.getIntrinsicWidth();

                if (drawableWidth > 0 && drawableHeight > 0) {
                    int height = Math.round(mWidth * drawableHeight / drawableWidth);
                    setMeasuredDimension(mWidth, height);
                } else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
