package com.german.vktestapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

public class StoryEditorView extends ViewGroup {
    private ImageView mBackgroundImageView;

    public StoryEditorView(Context context) {
        super(context);
    }

    public StoryEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StoryEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StoryEditorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void addSticker(@NonNull Bitmap bitmap) {
        ImageView stickerView = new ImageView(getContext());
        stickerView.setImageBitmap(bitmap);

        addView(stickerView);
    }

    @Override
    public void setBackground(@Nullable Drawable drawable) {
        if (mBackgroundImageView == null) {
            mBackgroundImageView = new ImageView(getContext());
            mBackgroundImageView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                                                                            LayoutParams.WRAP_CONTENT));
            mBackgroundImageView.setAdjustViewBounds(true);

            addView(mBackgroundImageView);
        }

        mBackgroundImageView.setImageDrawable(drawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int backgroundWidth;
        int backgroundHeight;
        if (mBackgroundImageView != null) {
            measureChild(mBackgroundImageView, widthMeasureSpec, heightMeasureSpec);
            Drawable drawable = mBackgroundImageView.getDrawable();
            if (drawable != null) {
                int drawableWidth = drawable.getIntrinsicWidth();
                drawableWidth = drawableWidth != -1 ? drawableWidth : width;

                int drawableHeight = drawable.getIntrinsicHeight();
                drawableHeight = drawableHeight != -1 ? drawableHeight : height;

                float widthCoef = 1.0f * width / drawableWidth;
                float heightCoef = 1.0f * height / drawableHeight;

                float coef = Math.min(widthCoef, heightCoef);

                backgroundWidth = Math.round(drawableWidth * coef);
                backgroundHeight = Math.round(drawableHeight * coef);
            } else {
                backgroundWidth = width;
                backgroundHeight = height;
            }
        } else {
            backgroundWidth = width;
            backgroundHeight = height;
        }

        setMeasuredDimension(backgroundWidth, backgroundHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mBackgroundImageView != null) {
            int childLeft = getPaddingLeft();
            int childTop = getPaddingTop();

            mBackgroundImageView.layout(childLeft,
                                        childTop,
                                        childLeft + getMeasuredWidth(),
                                        childTop + getMeasuredHeight());
        }
    }
}
