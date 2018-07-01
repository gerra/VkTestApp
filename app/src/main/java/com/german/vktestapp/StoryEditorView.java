package com.german.vktestapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.german.vktestapp.stickers.StickerLayoutInfo;
import com.german.vktestapp.stickers.StickersController;
import com.german.vktestapp.utils.ViewUtils;
import com.german.vktestapp.view.StickerView;

import java.util.concurrent.TimeUnit;

public class StoryEditorView extends ViewGroup {
    private static final String TAG = "[StoryEditorView]";

    static final long LONG_CLICK_TIME = TimeUnit.MILLISECONDS.toMillis(500);

    @NonNull
    private static Rect sRect = new Rect();

    private ImageView mBackgroundImageView;
    private EditText mEditText;

    Handler mHideKeyboardHandler;
    final Runnable mHideKeyboardRunnable = this::hideKeyboard;

    final ViewOrderController mViewOrderController = new ViewOrderController();
    StickersController mStickersController;

    private StoryEditorTouchEventHandler mStoryEditorTouchEventHandler;

    private TextStyleController mTextStyleController;

    public StoryEditorView(Context context) {
        this(context, null);
    }

    public StoryEditorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StoryEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StoryEditorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(@NonNull Context context) {
        setClipChildren(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setChildrenDrawingOrderEnabled(true);

        initBackgroundImageView(context);
        initEditText(context);

        mTextStyleController = new TextStyleController(mEditText);
        mHideKeyboardHandler = new Handler();

        StoryEditorTouchEventHandler.ViewFinder viewFinder =
                (x, y) -> findViewUnderTouch((int) x, (int) y);
        StoryEditorTouchEventHandler.TouchListener touchListener = new TouchListenerImpl();
        mStoryEditorTouchEventHandler = new StoryEditorTouchEventHandler(this, viewFinder, touchListener);
    }

    private void initBackgroundImageView(@NonNull Context context) {
        mBackgroundImageView = new ImageView(context);
        mBackgroundImageView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                                                                        LayoutParams.WRAP_CONTENT));
        mBackgroundImageView.setAdjustViewBounds(true);
        addView(mBackgroundImageView);
        mViewOrderController.moveToBottom(mBackgroundImageView);
    }

    private void initEditText(@NonNull Context context) {
        mEditText = (EditText) LayoutInflater.from(context)
                .inflate(R.layout.story_edit_text, this, false);
        ViewUtils.setEditTextGravity(mEditText, Gravity.START, Gravity.CENTER);
        mEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mViewOrderController.moveToTop(mEditText);
            } else {
                hideKeyboard();
            }
        });
        mEditText.setOnClickListener(v -> mViewOrderController.moveToTop(mEditText));

        addView(mEditText);
        mViewOrderController.moveToTop(mEditText);

        mEditText.requestFocus();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    public void changeTextStyle() {
        mTextStyleController.toggle();
    }

    public void addSticker(@NonNull Bitmap bitmap) {
        StickerView stickerView = new StickerView(getContext());
        stickerView.setImageBitmap(bitmap);

        addView(stickerView);
        mViewOrderController.moveToTop(stickerView);

        if (mStickersController == null) {
            mStickersController = new StickersController(this, mViewOrderController);
        }
        mStickersController.addSticker(stickerView, 0.5f, 0.5f);

        hideKeyboard();
    }

    @Override
    public void setBackground(@Nullable Drawable drawable) {
        mBackgroundImageView.setImageDrawable(drawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int backgroundWidth;
        int backgroundHeight;
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

        measureChildWithMargins(mEditText,
                                MeasureSpec.makeMeasureSpec(backgroundWidth, MeasureSpec.AT_MOST),
                                0,
                                MeasureSpec.makeMeasureSpec(backgroundHeight, MeasureSpec.AT_MOST),
                                0);

        measureStickers(backgroundWidth, backgroundHeight);

        setMeasuredDimension(backgroundWidth, backgroundHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int parentLeft = getPaddingLeft();
        int parentRight = right - left - getPaddingRight();

        int parentTop = getPaddingTop();
        int parentBottom = bottom - top - getPaddingBottom();

        layoutBackgroundImageView();
        layoutEditText(parentLeft, parentTop, parentRight, parentBottom);
        layoutStickers(parentLeft, parentTop, parentRight, parentBottom);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return indexOfChild(mViewOrderController.getViewByOrder(i));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                int x = (int) MotionEventCompat.getX(event, 0);
                int y = (int) MotionEventCompat.getX(event, 0);
                View touchedChild = findViewUnderTouch(x, y);
                if (touchedChild instanceof EditText) {
                    return false;
                }
                break;
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mStoryEditorTouchEventHandler.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        showKeyboard();
        return true;
    }

    private void measureStickers(int width, int height) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child instanceof StickerView)) {
                continue;
            }

            StickerView stickerView = (StickerView) child;

            boolean wasMeasured = stickerView.getMeasuredHeight() != 0
                    && stickerView.getMeasuredWidth() != 0;
            if (!wasMeasured) {
                measureChild(stickerView,
                             MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED),
                             MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));

                // This just added sticker, we need to save it sizes for change background in future
                float widthRatio = 1f * stickerView.getMeasuredWidth() / width;
                float heightRatio = 1f * stickerView.getMeasuredHeight() / height;
                mStickersController.setRatios(stickerView, widthRatio, heightRatio);
            } else {
                StickerLayoutInfo info = mStickersController.getLayoutInfo(stickerView);
                if (info != null) {
                    if (!info.isTouched()) {
                        int oldStickerWidth = stickerView.getMeasuredWidth();
                        int oldStickerHeight = stickerView.getMeasuredHeight();

                        float oldWidthRatio = info.getWidthRatio();
                        float oldHeightRatio = info.getHeightRatio();

                        float currentWidthRatio = 1f * oldStickerWidth / width;
                        float currentHeightRatio = 1f * oldStickerHeight / height;

                        float ratio = Math.min(oldWidthRatio / currentWidthRatio,
                                               oldHeightRatio / currentHeightRatio);

                        float specWidth = oldStickerWidth * ratio;
                        float specHeight = oldStickerHeight * ratio;

                        measureChild(stickerView,
                                     MeasureSpec.makeMeasureSpec(Math.round(specWidth), MeasureSpec.EXACTLY),
                                     MeasureSpec.makeMeasureSpec(Math.round(specHeight), MeasureSpec.EXACTLY));
                    }
                } else {
                    Log.w(TAG, "wtf? There is no StickerView in controller?");
                }
            }
        }
    }

    private void layoutBackgroundImageView() {
        if (mBackgroundImageView != null) {
            int childLeft = getPaddingLeft();
            int childTop = getPaddingTop();

            mBackgroundImageView.layout(childLeft,
                                        childTop,
                                        childLeft + getMeasuredWidth(),
                                        childTop + getMeasuredHeight());
        }
    }

    private void layoutEditText(int parentLeft, int parentTop, int parentRight, int parentBottom) {
        int editTextWidth = mEditText.getMeasuredWidth();
        int editTextHeight = mEditText.getMeasuredHeight();

        MarginLayoutParams lp = (MarginLayoutParams) mEditText.getLayoutParams();

        int editTextLeft = parentLeft + (parentRight - parentLeft - editTextWidth) / 2
                + lp.leftMargin - lp.rightMargin;
        int editTextTop = parentTop + (parentBottom - parentTop - editTextHeight) / 2
                + lp.topMargin - lp.bottomMargin;

        mEditText.layout(editTextLeft,
                         editTextTop,
                         editTextLeft + editTextWidth,
                         editTextTop + editTextHeight);
    }

    private void layoutStickers(int parentLeft, int parentTop, int parentRight, int parentBottom) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child instanceof StickerView)) {
                continue;
            }

            layoutSticker((StickerView) child, parentLeft, parentTop, parentRight, parentBottom);
        }
    }

    private void layoutSticker(@NonNull StickerView stickerView,
                               int parentLeft,
                               int parentTop,
                               int parentRight,
                               int parentBottom) {
        StickerLayoutInfo coordinates = mStickersController.getLayoutInfo(stickerView);
        if (coordinates == null) {
            Log.w(TAG, "wtf? StickerView is\'nt in StickersController?");
            return;
        }
        float stickerCenterX = (parentRight - parentLeft) * coordinates.getX();
        float stickerCenterY = (parentBottom - parentTop) * coordinates.getY();

        int stickerWidth = stickerView.getMeasuredWidth();
        int stickerHeight = stickerView.getMeasuredHeight();

        int stickerLeft = Math.round(stickerCenterX - stickerWidth / 2f);
        int stickerTop = Math.round(stickerCenterY - stickerHeight / 2f);

        stickerView.layout(stickerLeft,
                           stickerTop,
                           stickerLeft + stickerWidth,
                           stickerTop + stickerHeight);
    }

    private void showKeyboard() {
        mHideKeyboardHandler.removeCallbacks(mHideKeyboardRunnable);

        mEditText.setVisibility(VISIBLE);
        mEditText.requestFocus();
        ViewUtils.showKeyboard(mEditText, true);
        mViewOrderController.moveToTop(mEditText);
    }

    private void hideKeyboard() {
        ViewUtils.hideKeyboard(this);
        mEditText.clearFocus();
        if (TextUtils.isEmpty(mEditText.getText())) {
            mEditText.setVisibility(INVISIBLE);
        }
    }

    @Nullable
    private View findViewUnderTouch(int x, int y) {
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(getChildDrawingOrder(childCount, i));
            child.getHitRect(sRect);
            if (sRect.contains(x, y)) {
                return child;
            }
        }
        return null;
    }

    static boolean isLayoutInfoValid(@Nullable StickerLayoutInfo layoutInfo) {
        if (layoutInfo == null) {
            Log.w(TAG, "wtf? LayoutInfo is null?");
            return false;
        }
        return true;
    }

    private class TouchListenerImpl implements StoryEditorTouchEventHandler.TouchListener {

        @Override
        public void onStickerTouchDown(@NonNull StickerView stickerView) {
            if (mViewOrderController.moveToTop(stickerView)) {
                invalidate();
            }

            StickerLayoutInfo layoutInfo = mStickersController.getLayoutInfo(stickerView);
            if (!isLayoutInfoValid(layoutInfo)) {
                return;
            }

            layoutInfo.setTouched(true);
        }

        @Override
        public void onBackgroundTouchDown() {
            mHideKeyboardHandler.postDelayed(mHideKeyboardRunnable, LONG_CLICK_TIME);
        }

        @Override
        public void onStickerMove(@NonNull StickerView stickerView,
                                  boolean isPureMove,
                                  float activePointerX, float activePointerY,
                                  float dx, float dy) {
            float deltaPercentageX = dx / getMeasuredWidth();
            float deltaPercentageY = dy / getMeasuredHeight();

            StickerLayoutInfo layoutInfo = mStickersController.getLayoutInfo(stickerView);
            if (!isLayoutInfoValid(layoutInfo)) {
                return;
            }

            layoutInfo.setX(layoutInfo.getX() + deltaPercentageX);
            layoutInfo.setY(layoutInfo.getY() + deltaPercentageY);

            // TODO: may be use translations and invalidate?
            requestLayout();
        }

        @Override
        public void onStickerStopMove(@NonNull StickerView stickerView,
                                      float activePointerX, float activePointerY) {
            StickerLayoutInfo layoutInfo = mStickersController.getLayoutInfo(stickerView);
            if (!isLayoutInfoValid(layoutInfo)) {
                return;
            }

            layoutInfo.setTouched(false);
        }

        @Override
        public void onStickerScale(@NonNull StickerView stickerView) {

        }

        @Override
        public void onStickerRotate(@NonNull StickerView stickerView) {

        }
    }
}
