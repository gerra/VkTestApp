package com.german.vktestapp.textstyling;

import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.text.style.LineBackgroundSpan;
import android.util.SparseIntArray;

public class RoundedBackgroundSpan implements LineBackgroundSpan {
    private static final String TAG = "[RoundedBackgroundSpan]";

    private static final Rect RECT = new Rect();

    private final SparseIntArray mLefts = new SparseIntArray(20);
    private final SparseIntArray mRights = new SparseIntArray(20);
    private final SparseIntArray mTops = new SparseIntArray(20);
    private final SparseIntArray mBottoms = new SparseIntArray(20);

    private final Path mBorderPath = new Path();
    private final Paint mPathPaint = new Paint();

    private final float mSidePadding;

    private int mStartLine = -1;
    private int mPrevLine;
    private int mLastLine;

    public RoundedBackgroundSpan(float sidePadding,
                                 @ColorInt int color,
                                 float backgroundRadius,
                                 float shadowRadius,
                                 float shadowDy,
                                 @ColorInt int shadowColor) {
        mSidePadding = sidePadding;

        mPathPaint.setPathEffect(new CornerPathEffect(backgroundRadius));
        mPathPaint.setColor(color);
        mPathPaint.setShadowLayer(shadowRadius, 0, shadowDy, shadowColor);
    }

    @Override
    public void drawBackground(Canvas canvas,
                               Paint p,
                               int left, int right,
                               int top,
                               int baseline,
                               int bottom,
                               CharSequence text, int start, int end,
                               int lnum) {
        if (mStartLine == -1) {
            mStartLine = lnum;
        } else if (lnum == mStartLine) {
            mLastLine = mPrevLine;
        } else if (lnum != mPrevLine + 1) {
            if (lnum > mStartLine) {
                mLastLine = lnum;
            }
            mStartLine = lnum;
        }

        mPrevLine = lnum;
        mLastLine = Math.max(mLastLine, lnum);

        int width = right - left;
        float textLineWidth = p.measureText(text, start, end);
        float sidePadding = (width - textLineWidth) / 2;
        left = (int) (left + sidePadding);
        right = (int) (right - sidePadding);

        mLefts.put(lnum, left);
        mRights.put(lnum, right);
        mBottoms.put(lnum, bottom);
        mTops.put(lnum, top);

        if (end - start <= 0 || lnum != mLastLine) {
            return;
        }

        int saveCount = canvas.save();

        canvas.getClipBounds(RECT);
        RECT.inset((int) (-mSidePadding * 2) - 50, -50);
        canvas.clipRect(RECT);

        calculatePath();

        canvas.drawPath(mBorderPath, mPathPaint);

        canvas.restoreToCount(saveCount);
    }

    private void calculatePath() {
        mBorderPath.reset();

        // Right side
        for (int i = mStartLine; i <= mLastLine; i++) {
            int right = mRights.get(i);
            int top = mTops.get(i);
            int bottom = mBottoms.get(i);

            if (i == mStartLine) {
                mBorderPath.moveTo(right + mSidePadding, top);
            } else {
                mBorderPath.lineTo(right + mSidePadding, top);
            }

            mBorderPath.lineTo(right + mSidePadding, bottom);
        }

        // Left side
        for (int i = mLastLine; i >= mStartLine; i--) {
            int left = mLefts.get(i);
            int top = mTops.get(i);
            int bottom = mBottoms.get(i);

            mBorderPath.lineTo(left - mSidePadding, bottom);
            mBorderPath.lineTo(left - mSidePadding, top);
        }

        mBorderPath.close();
    }
}
