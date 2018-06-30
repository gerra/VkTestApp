package com.german.vktestapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.german.vktestapp.stickers.StickersController;
import com.german.vktestapp.utils.ViewUtils;
import com.german.vktestapp.view.StickerView;

import java.util.concurrent.TimeUnit;

public class StoryEditorView extends ViewGroup {
    private static final String TAG = "[StoryEditorView]";

    private static final long CLICK_DOWN_TIME = TimeUnit.MILLISECONDS.toMillis(500);

    private ImageView mBackgroundImageView;
    private EditText mEditText;
    private int mChildCountOnLastModifyEditText;

    private Handler mHideKeyboardHandler;
    private final Runnable mHideKeyboardRunnable = () -> {
        ViewUtils.hideKeyboard(this);
        mEditText.clearFocus();
        if (TextUtils.isEmpty(mEditText.getText())) {
            mEditText.setVisibility(INVISIBLE);
        }
    };

    private StickersController mStickersController;
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
    }

    private void initBackgroundImageView(@NonNull Context context) {
        mBackgroundImageView = new ImageView(context);
        mBackgroundImageView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                                                                        LayoutParams.WRAP_CONTENT));
        mBackgroundImageView.setAdjustViewBounds(true);
        addView(mBackgroundImageView);
    }

    private void initEditText(@NonNull Context context) {
        mEditText = (EditText) LayoutInflater.from(context)
                .inflate(R.layout.story_edit_text, this, false);
        addView(mEditText);
        ViewUtils.setEditTextGravity(mEditText, Gravity.START, Gravity.CENTER);
        mEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mChildCountOnLastModifyEditText = getChildCount();
            } else {
                ViewUtils.hideKeyboard(this);
            }
        });
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
        StickerView stickerView = new StickerView(getContext(), 0.5f, 0.5f);
        stickerView.setImageBitmap(bitmap);

        addView(stickerView);

        if (mStickersController == null) {
            mStickersController = new StickersController(this);
        }
        mStickersController.addSticker(stickerView, 0.5f, 0.5f);
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

        measureChildWithMargins(mEditText,
                                MeasureSpec.makeMeasureSpec(backgroundWidth, MeasureSpec.AT_MOST),
                                0,
                                MeasureSpec.makeMeasureSpec(backgroundHeight, MeasureSpec.AT_MOST),
                                0);

        measureStickers(backgroundWidth, backgroundHeight);

        setMeasuredDimension(backgroundWidth, backgroundHeight);
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
                StickersController.StickerLayoutInfo info = mStickersController.getLayoutInfo(stickerView);
                if (info != null) {
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
                } else {
                    Log.w(TAG, "wtf? There is no StickerView in controller?");
                }
            }
        }
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
        if (i == 0) {
            return indexOfChild(mBackgroundImageView);
        }

        if (i == 1 && mEditText.getVisibility() != VISIBLE
                || i == mChildCountOnLastModifyEditText - 1) {
            return indexOfChild(mEditText);
        }

        boolean editTextWasDrawn = i > 1 && mEditText.getVisibility() != VISIBLE
                || i > mChildCountOnLastModifyEditText - 1;

        int stickerIndex = i
                - 1 // for mBackgroundImage
                - (editTextWasDrawn ? 1 : 0);

        StickerView stickerView = mStickersController.getStickerView(stickerIndex);
        if (stickerView == null) {
            Log.d(TAG, "wtf? Incorrect order? " + i);
        }

        return indexOfChild(stickerView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // If there is no children for intercept
                handled = true;
                mHideKeyboardHandler.postDelayed(mHideKeyboardRunnable, CLICK_DOWN_TIME);
                break;
            case MotionEvent.ACTION_UP:
                if (event.getEventTime() - event.getDownTime() < CLICK_DOWN_TIME) {
                    handled = performClick();
                    mHideKeyboardHandler.removeCallbacks(mHideKeyboardRunnable);
                }
                break;
            default:
                handled = super.onTouchEvent(event);
                break;
        }
        return handled;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        mEditText.setVisibility(VISIBLE);
        mEditText.requestFocus();
        ViewUtils.showKeyboard(mEditText, true);
        return true;
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
        StickersController.StickerLayoutInfo coordinates = mStickersController.getLayoutInfo(stickerView);
        if (coordinates == null) {
            Log.w(TAG, "wtf? StickerView is\'nt in StickersController? So, okay... Let\'s remove it");
            removeView(stickerView);
            mStickersController.removeSticker(stickerView);
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
}
