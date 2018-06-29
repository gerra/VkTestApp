package com.german.vktestapp.utils;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class RoundCornersDrawable extends DrawableWrapper {
    @Nullable
    private float[] mCorners;

    @NonNull
    private final RectF mRect = new RectF(0, 0, 0, 0);
    @NonNull
    private final Path mCornersPath = new Path();
    private boolean mAreCornersNeeded;

    public RoundCornersDrawable(@NonNull Drawable drawable, @FloatRange(from = 0.0) float cornerRadius) {
        super(drawable);

        setRoundCorners(cornerRadius);

        if (drawable instanceof BitmapDrawable) {
            ((BitmapDrawable) drawable).getPaint().setAntiAlias(true);
        }
    }

    private void setRoundCorners(@FloatRange(from = 0.0) float cornerRadius) {
        mAreCornersNeeded = Float.compare(cornerRadius, +0.0f) > 0;

        if (mAreCornersNeeded) {
            mCorners = new float[8];
            for (int i = 0; i < mCorners.length; i++) {
                mCorners[i] = cornerRadius;
            }
        }

        updateCornersPath(getBounds());
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);

        updateCornersPath(bounds);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!mCornersPath.isEmpty()) {
            Rect savedClipRect = canvas.getClipBounds();

            canvas.clipPath(mCornersPath);

            try {
                super.draw(canvas);
            } finally {
                canvas.clipRect(savedClipRect);
            }
        } else {
            super.draw(canvas);
        }
    }

    private void updateCornersPath(@NonNull Rect rect) {
        mCornersPath.reset();

        if (mAreCornersNeeded) {
            mRect.left = rect.left;
            mRect.top = rect.top;
            mRect.right = rect.right;
            mRect.bottom = rect.bottom;

            mCornersPath.addRoundRect(mRect, mCorners, Path.Direction.CW);
        }
    }
}
