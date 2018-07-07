package com.german.vktestapp.editor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;

import com.german.vktestapp.R;
import com.german.vktestapp.textstyling.Style;
import com.german.vktestapp.textstyling.Styleable;
import com.german.vktestapp.view.LoseFocusEditText;
import com.german.vktestapp.view.StaticDrawable;

public class StoryEditText extends LoseFocusEditText
        implements StaticDrawable, Styleable {
    private static final int DEFAULT_BACKGROUND_SIDE_PADDING = 0;
    private static final int DEFAULT_BACKGROUND_CORNER_RADIUS = 0;
    private static final int DEFAULT_BACKGROUND_SIDE_DELTA = 0;

    private static final Rect RECT = new Rect();

    /////////////////////////////////////////////////////////////////////////
    // Helper arrays for not to allocate for calculating path on every draw:
    @NonNull
    private final SparseIntArray mLefts = new SparseIntArray(20);
    @NonNull
    private final SparseIntArray mRights = new SparseIntArray(20);
    @NonNull
    private final SparseIntArray mTops = new SparseIntArray(20);
    @NonNull
    private final SparseIntArray mBottoms = new SparseIntArray(20);
    /////////////////////////////////////////////////////////////////////////

    private int mBackgroundSidePadding;
    private int mBackgroundSideDelta;

    @NonNull
    private final Path mTextBackgroundPath = new Path();
    @NonNull
    private final Paint mTextBackgroundPaint = new Paint();

    private final OnFocusChangeListener mOwnOnFocusChangeListener = (v, hasFocus) -> {
        if (!hasFocus && TextUtils.isEmpty(getText())) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    };

    @NonNull
    private Style mDefaultStyle;

    @ColorInt
    private int mUserTextColor;
    private boolean mOwnCall;

    public StoryEditText(Context context) {
        super(context);
        init(context, null);
    }

    public StoryEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StoryEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        float backgroundCornerRadius;

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.StoryEditText, 0, 0);
        try {
            mBackgroundSidePadding = array.getDimensionPixelSize(R.styleable.StoryEditText_background_side_padding,
                                                                 DEFAULT_BACKGROUND_SIDE_PADDING);
            mBackgroundSideDelta = array.getDimensionPixelSize(R.styleable.StoryEditText_background_side_delta,
                                                               DEFAULT_BACKGROUND_SIDE_DELTA);
            backgroundCornerRadius = array.getDimension(R.styleable.StoryEditText_background_corner_radius,
                                                        DEFAULT_BACKGROUND_CORNER_RADIUS);
        } finally {
            array.recycle();
        }

        mTextBackgroundPaint.setColor(getResources().getColor(R.color.text_style_3_background_color));
        mTextBackgroundPaint.setPathEffect(new CornerPathEffect(backgroundCornerRadius));

        super.setOnFocusChangeListener(mOwnOnFocusChangeListener);

        mUserTextColor = super.getCurrentTextColor();
        mDefaultStyle = new Style(new Style.TextStyle(mUserTextColor, null),
                                  new Style.BackgroundStyle(null, null));
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        OnFocusChangeListener oldListener = getOnFocusChangeListener();

        OnFocusChangeListener actualListener = oldListener != null
                ? new CombinedOnFocusChangeListener(oldListener, listener)
                : listener;

        super.setOnFocusChangeListener(actualListener);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        if (!mOwnCall) {
            mUserTextColor = color;
        }
    }

    @Override
    public void setShadowLayer(float radius, float dx, float dy, int color) {
        super.setShadowLayer(radius, dx, dy, color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int backgroundColor = mTextBackgroundPaint.getColor();
        if (Color.alpha(backgroundColor) > 0) {
            drawBackground(canvas, getLayout());
        }
        super.onDraw(canvas);
    }

    @Override
    public void drawStatic(@NonNull Canvas canvas) {
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(getCurrentTextColor());
        textPaint.setTypeface(getTypeface());
        textPaint.setTextSize(getTextSize());
        textPaint.setShadowLayer(getShadowRadius(),
                                 getShadowDx(),
                                 getShadowDy(),
                                 getShadowColor());

        Rect rect = new Rect();
        canvas.getClipBounds(rect);

        StaticLayout staticLayout = new StaticLayout(getText().toString(),
                                                     0,
                                                     length(),
                                                     textPaint,
                                                     getWidth(),
                                                     Layout.Alignment.ALIGN_CENTER,
                                                     getLineSpacingMultiplier(),
                                                     getLineSpacingExtra(),
                                                     getIncludeFontPadding());
        drawBackground(canvas, staticLayout);
        staticLayout.draw(canvas);
    }

    @Override
    public void setStyle(@NonNull Style style) {
        int textColor = style.mTextStyle.mColor != null
                ? style.mTextStyle.mColor
                : mUserTextColor;
        if (getCurrentTextColor() != textColor) {
            mOwnCall = true;
            setTextColor(textColor);
            mOwnCall = false;
        }

        Style.Shadow textShadow = style.mTextStyle.mShadow;
        if (textShadow != null) {
            setShadowLayer(textShadow.mRadius, 0, textShadow.mSize, textShadow.mColor);
        } else {
            setShadowLayer(0, 0, 0, 0);
        }

        Integer backgroundColor = style.mBackgroundStyle.mColor;
        if (backgroundColor != null) {
            if (mTextBackgroundPaint.getColor() != backgroundColor) {
                mTextBackgroundPaint.setColor(backgroundColor);
            }
        } else {
            // Color.alpha(0) = 0, so we will not draw background
            mTextBackgroundPaint.setColor(0);
        }

        Style.Shadow backgroundShadow = style.mBackgroundStyle.mShadow;
        if (backgroundShadow != null) {
            mTextBackgroundPaint.setShadowLayer(backgroundShadow.mRadius,
                                                0, backgroundShadow.mSize,
                                                backgroundShadow.mColor);

        } else {
            mTextBackgroundPaint.clearShadowLayer();
        }

        invalidate();
    }

    @Override
    public void clearStyle() {
        setStyle(mDefaultStyle);
    }

    private void drawBackground(@NonNull Canvas canvas, @Nullable Layout layout) {
        if (layout == null) {
            return;
        }

        int rTop;
        int rBottom;
        synchronized (RECT) {
            if (!canvas.getClipBounds(RECT)) {
                return;
            }
            rTop = RECT.top;
            rBottom = RECT.bottom;
        }

        int lineCount = layout.getLineCount();

        final int top = Math.max(0, rTop);
        final int bottom = Math.min(layout.getLineTop(lineCount), rBottom);

        if (top >= bottom) {
            return;
        }

        final int lineFrom = layout.getLineForVertical(top);
        final int lineTo = layout.getLineForVertical(bottom);

        if (lineTo < 0) {
            return;
        }

        drawBackground(canvas, layout, lineFrom, lineTo);
    }

    private void drawBackground(@NonNull Canvas canvas,
                                @NonNull Layout layout,
                                int lineFrom, int lineTo) {
        int previousEnd = layout.getLineStart(lineFrom);
        int prevPartStart = lineFrom;
        int lastLine = layout.getLineCount() - 1;
        for (int i = 0; i <= lastLine; i++) {
            int start = previousEnd;
            int end = layout.getLineStart(i + 1);
            previousEnd = end;

            int wantFrom = prevPartStart;
            int wantTo = i - 1;
            boolean paragraphFound = false;
            if (start == end - 1 && layout.getText().toString().charAt(start) == '\n') {
                if (prevPartStart != i) {
                    paragraphFound = true;
                }
                prevPartStart = i + 1;
            } else if (i == lineTo) {
                paragraphFound = true;
                wantTo = lineTo;
            }

            if (paragraphFound) {
                if (wantFrom >= lineFrom && wantFrom <= lineTo
                        || wantTo >= lineFrom && wantTo <= lineTo) {
                    calculatePath(layout, wantFrom, wantTo);
                    canvas.drawPath(mTextBackgroundPath, mTextBackgroundPaint);
                    mTextBackgroundPath.reset();
                }
            }
        }
    }

    private void calculatePath(@NonNull Layout layout, int lineFrom, int lineTo) {
        mLefts.clear();
        mRights.clear();
        mBottoms.clear();
        mTops.clear();
        calculateHelperArrays(layout, lineFrom, lineTo);

        mTextBackgroundPath.reset();

        // Right side
        for (int i = lineFrom; i <= lineTo; i++) {
            int start = layout.getLineStart(i);
            int end = layout.getLineEnd(i);

            if (start == end) {
                continue;
            }

            int right = mRights.get(i);
            int top = mTops.get(i);
            int bottom = mBottoms.get(i);

            if (i == lineFrom) {
                mTextBackgroundPath.moveTo(right, top);
            } else {
                mTextBackgroundPath.lineTo(right, top);
            }

            mTextBackgroundPath.lineTo(right, bottom);
        }

        // Left side
        for (int i = lineTo; i >= lineFrom; i--) {
            int start = layout.getLineStart(i);
            int end = layout.getLineEnd(i);

            if (start == end) {
                continue;
            }

            int left = mLefts.get(i);
            int top = mTops.get(i);
            int bottom = mBottoms.get(i);

            mTextBackgroundPath.lineTo(left, bottom);
            mTextBackgroundPath.lineTo(left, top);
        }

        mTextBackgroundPath.close();
    }

    private void calculateHelperArrays(@NonNull Layout layout, int lineFrom, int lineTo) {
        if (lineTo < lineFrom) {
            return;
        }

        int previousBottom = layout.getLineTop(lineFrom);
        int previousWidth = -1;
        int previousDescent = -1;

        int previousLeft = -1;
        int previousRight = -1;

        for (int i = lineFrom; i <= lineTo; i++) {
            int descent = layout.getLineDescent(i);

            int top = previousBottom;
            int bottom = layout.getLineTop(i + 1) + descent;

            int left = (int) layout.getLineLeft(i) - mBackgroundSidePadding;
            int right = (int) layout.getLineRight(i) + mBackgroundSidePadding;

            if (previousLeft != -1 && Math.abs(left - previousLeft) < mBackgroundSideDelta) {
                if (left < previousLeft) { // right > prevRight
                    int j = i - 1;
                    while (j >= lineFrom) {
                        int curLeft = mLefts.get(j);
                        if (Math.abs(curLeft - left) < mBackgroundSideDelta) {
                            left = Math.min(curLeft, left);
                            right = Math.max(mRights.get(j), right);

                            mLefts.put(j, left);
                            mRights.put(j, right);
                        } else {
                            break;
                        }
                        j--;
                    }
                } else {
                    left = previousLeft;
                    right = previousRight;
                }
            }

            int width = right - left;

            if (i > lineFrom && width > previousWidth) {
                top -= (int) (1.5f * previousDescent);
                mBottoms.put(i - 1, top);
            }

            mLefts.put(i, left);
            mTops.put(i, top);
            mRights.put(i, right);
            mBottoms.put(i, bottom);

            previousBottom = bottom;
            previousWidth = width;
            previousDescent = descent;
            previousLeft = left;
            previousRight = right;
        }
    }

    private static class CombinedOnFocusChangeListener implements OnFocusChangeListener {
        @NonNull
        private final OnFocusChangeListener[] mListeners;

        CombinedOnFocusChangeListener(@NonNull OnFocusChangeListener... listeners) {
            mListeners = listeners;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            for (OnFocusChangeListener listener : mListeners) {
                if (listener != null) {
                    listener.onFocusChange(v, hasFocus);
                }
            }
        }
    }
}
