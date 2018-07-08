package com.german.vktestapp.editor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.german.vktestapp.R;
import com.german.vktestapp.backgrounds.Background;
import com.german.vktestapp.textstyling.Styleable;
import com.german.vktestapp.textstyling.StyleableProvider;
import com.german.vktestapp.utils.ViewOrderController;
import com.german.vktestapp.utils.ViewUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class StoryEditorView extends ViewGroup implements StyleableProvider {
    private static final String TAG = "[StoryEditorView]";

    // Sticker should be not greater that MAX_STICKER_DIMENSION_RATIO of any dimension of this view
    private static final float MAX_STICKER_DIMENSION_RATIO = 0.2f;

    static final long LONG_CLICK_TIME = TimeUnit.MILLISECONDS.toMillis(500);

    private boolean mNeedRemeasure = true;

    private Background mBackground;
    private ImageView mBackgroundImageView;
    private StoryEditText mEditText;
    RecyclerBinView mRecyclerBinView;

    RecycleBinState mRecyclerBinController;

    Handler mHideKeyboardHandler;
    final Runnable mHideKeyboardRunnable = this::hideKeyboard;

    final ViewOrderController mViewOrderController = new ViewOrderController();

    private final ActionListenerImpl mActionListener = new ActionListenerImpl();

    private Collection<BackgroundSetListener> mBackgroundSetListeners;
    Collection<InteractStickerListener> mInteractStickerListeners;
    @Nullable
    ActivateRecycleBinEffect mActivateRecycleBinEffect;

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

    private void init(@NonNull Context context) {
        super.setClipChildren(false);
        super.setFocusable(true);
        super.setFocusableInTouchMode(true);
        super.setChildrenDrawingOrderEnabled(true);
        super.setMotionEventSplittingEnabled(false);

        initBackgroundImageView(context);
        initEditText(context);

        mHideKeyboardHandler = new Handler();
    }

    private void initBackgroundImageView(@NonNull Context context) {
        mBackgroundImageView = new BackgroundImageView(context);
        addView(mBackgroundImageView);
        mViewOrderController.moveToBottom(mBackgroundImageView);
    }

    private void initEditText(@NonNull Context context) {
        mEditText = (StoryEditText) LayoutInflater.from(context)
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

    public void setActivateRecycleBinEffect(@Nullable ActivateRecycleBinEffect activateRecycleBinEffect) {
        mActivateRecycleBinEffect = activateRecycleBinEffect;
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child instanceof RecyclerBinView) {
            mRecyclerBinView = (RecyclerBinView) child;
            addBackgroundSetListener(mRecyclerBinView);
            mRecyclerBinController = new RecycleBinState(mRecyclerBinView);
            mViewOrderController.moveHighPriorityViewToTop(mRecyclerBinView);
        } else if (child instanceof StickerView) {
            StickerView stickerView = (StickerView) child;

            mViewOrderController.moveToTop(stickerView);

            stickerView.setOnClickListener(v -> {
                mViewOrderController.moveToTop(v);
                invalidate();
            });
            stickerView.setOnTouchListener(new StickerTouchListener(mActionListener));

            hideKeyboard();
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams
                ? p
                : new MarginLayoutParams(p);
    }

    public void addSticker(@NonNull Bitmap bitmap) {
        StickerView stickerView = new StickerView(getContext());
        stickerView.setImageBitmap(bitmap);

        addView(stickerView);
    }

    @Override
    public void setBackground(@Nullable Drawable drawable) {
        mBackgroundImageView.setImageDrawable(drawable);
        mNeedRemeasure = true;
        requestLayout();
    }

    public void setBackground(@NonNull Background background) {
        mBackground = background;

        setBackground(mBackground.getFull(getContext()));
        for (BackgroundSetListener listener : mBackgroundSetListeners) {
            listener.onBackgroundSet(mBackground);
        }
    }

    public void addBackgroundSetListener(@NonNull BackgroundSetListener listener) {
        if (mBackgroundSetListeners == null) {
            mBackgroundSetListeners = new HashSet<>();
        }
        mBackgroundSetListeners.add(listener);
        if (mBackground != null) {
            listener.onBackgroundSet(mBackground);
        }
    }

    public void removeBackgroundSetListener(@NonNull BackgroundSetListener listener) {
        if (mBackgroundSetListeners != null) {
            mBackgroundSetListeners.remove(listener);
        }
    }

    public void addInteractStickerListener(@NonNull InteractStickerListener listener) {
        if (mInteractStickerListeners == null) {
            mInteractStickerListeners = new HashSet<>();
        }
        mInteractStickerListeners.add(listener);
    }

    public void removeInteractStickerListener(@NonNull InteractStickerListener listener) {
        if (mBackgroundSetListeners != null) {
            mInteractStickerListeners.remove(listener);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        BackgroundInfo backgroundInfo = measureBackground(widthMeasureSpec, heightMeasureSpec);
        int backgroundWidth = backgroundInfo.width;
        int backgroundHeight = backgroundInfo.height;

        // Pass to children measured sizes of this view
        int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(backgroundWidth, MeasureSpec.AT_MOST);
        int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(backgroundHeight, MeasureSpec.AT_MOST);

        measureEditText(backgroundInfo);
        measureStickers(backgroundInfo);
        measureChildWithMargins(mRecyclerBinView, newWidthMeasureSpec, 0, newHeightMeasureSpec, 0);

        setMeasuredDimension(backgroundWidth, backgroundHeight);

        mNeedRemeasure = false;
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
        layoutRecycleBin(parentLeft, parentTop, parentRight, parentBottom);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return indexOfChild(mViewOrderController.getViewByOrder(i));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                int x = (int) MotionEventCompat.getX(event, 0);
                int y = (int) MotionEventCompat.getY(event, 0);
                View touchedChild = findViewUnderTouch(x, y);
                if (touchedChild == mBackgroundImageView) {
                    return true;
                }
                break;
            }
        }

        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // It's only for mBackgroundImageView
        int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            mHideKeyboardHandler.postDelayed(mHideKeyboardRunnable, LONG_CLICK_TIME);
        } else if (ViewUtils.needToPerformClick(event)) {
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        showKeyboard();
        return true;
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (child == mRecyclerBinView) {
            removeBackgroundSetListener(mRecyclerBinView);
        }
        mViewOrderController.removeView(child);
    }

    @NonNull
    private BackgroundInfo measureBackground(int widthMeasureSpec, int heightMeasureSpec) {
        // Ignore modes, our StoryEditorView should be in the FrameLayout with match parent sizes

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth;
        int desiredHeight;

        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        if (mNeedRemeasure
                || measuredWidth <= 0
                || measuredHeight <= 0
                || mBackground == null
                || mBackground.isEmpty()) {
            Drawable drawable = mBackgroundImageView.getDrawable();
            desiredWidth = drawable != null ? drawable.getIntrinsicWidth() : -1;
            desiredWidth = desiredWidth != -1 ? desiredWidth : parentWidth;

            desiredHeight = drawable != null ? drawable.getIntrinsicHeight() : -1;
            desiredHeight = desiredHeight != -1 ? desiredHeight : parentHeight;
        } else {
            desiredWidth = measuredWidth;
            desiredHeight = measuredHeight;
        }

        float widthCoef = 1.0f * parentWidth / desiredWidth;
        float heightCoef = 1.0f * parentHeight / desiredHeight;

        float coef = Math.min(widthCoef, heightCoef);

        float backgroundWidthF = desiredWidth * coef;
        float backgroundHeightF = desiredHeight * coef;

        int backgroundWidth = Math.round(backgroundWidthF);
        int backgroundHeight = Math.round(backgroundHeightF);

        mBackgroundImageView.measure(MeasureSpec.makeMeasureSpec(backgroundWidth, MeasureSpec.EXACTLY),
                                     MeasureSpec.makeMeasureSpec(backgroundHeight, MeasureSpec.EXACTLY));

        float changeWidthRatio = measuredWidth > 0 ? measuredWidth / backgroundWidthF : 1f;
        float changeHeightRatio = measuredHeight > 0 ? measuredHeight / backgroundHeightF : 1f;
        return new BackgroundInfo(backgroundWidth,
                                  backgroundHeight,
                                  changeWidthRatio,
                                  changeHeightRatio);
    }

    private void measureEditText(@NonNull BackgroundInfo backgroundInfo) {
        measureChildWithMargins(mEditText,
                                MeasureSpec.makeMeasureSpec(backgroundInfo.width, MeasureSpec.AT_MOST),
                                0,
                                MeasureSpec.makeMeasureSpec(backgroundInfo.height, MeasureSpec.AT_MOST),
                                0);
    }

    private void measureStickers(@NonNull BackgroundInfo backgroundInfo) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child instanceof StickerView)) {
                continue;
            }

            measureSticker((StickerView) child, backgroundInfo);
        }
    }

    private void measureSticker(@NonNull StickerView stickerView,
                                @NonNull BackgroundInfo backgroundInfo) {
        int measuredWidth = stickerView.getMeasuredWidth();
        int measuredHeight = stickerView.getMeasuredHeight();

        if (measuredWidth <= 0 || measuredHeight <= 0) {
            // Sticker is just added
            // Check what size is interested for it
            stickerView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        } else if (Math.abs(backgroundInfo.widthRatio - backgroundInfo.heightRatio) <= 0.00001) {
            float ratio = Math.max(backgroundInfo.widthRatio, backgroundInfo.heightRatio);
            int newWidth = (int) (measuredWidth / ratio);
            int newHeight = (int) (measuredHeight / ratio);
            stickerView.setTranslationX(stickerView.getTranslationX() / ratio);
            stickerView.setTranslationY(stickerView.getTranslationY() / ratio);
            stickerView.setPivotX(stickerView.getPivotX() / ratio);
            stickerView.setPivotY(stickerView.getPivotY() / ratio);
            stickerView.measure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
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
        // Initial sticker center is in the center of parent,
        // for positioning translation is used.
        float stickerCenterX = (parentRight + parentLeft) * 0.5f;
        float stickerCenterY = (parentBottom + parentTop) * 0.5f;

        int stickerWidth = stickerView.getMeasuredWidth();
        int stickerHeight = stickerView.getMeasuredHeight();

        int stickerLeft = Math.round(stickerCenterX - stickerWidth / 2f);
        int stickerTop = Math.round(stickerCenterY - stickerHeight / 2f);

        stickerView.layout(stickerLeft,
                           stickerTop,
                           stickerLeft + stickerWidth,
                           stickerTop + stickerHeight);
    }

    private void layoutRecycleBin(int parentLeft, int parentTop, int parentRight, int parentBottom) {
        RecyclerBinView recyclerBin = mRecyclerBinView;

        int recycleBinWidth = recyclerBin.getMeasuredWidth();
        int recycleBinHeight = recyclerBin.getMeasuredHeight();

        MarginLayoutParams lp = (MarginLayoutParams) recyclerBin.getLayoutParams();

        int recycleBinLeft = parentLeft + (parentRight - parentLeft - recycleBinWidth) / 2
                + lp.leftMargin - lp.rightMargin;
        int recycleBinTop = parentBottom - recycleBinHeight - lp.bottomMargin;

        recyclerBin.layout(recycleBinLeft,
                            recycleBinTop,
                            recycleBinLeft + recycleBinWidth,
                            recycleBinTop + recycleBinHeight);
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
    }

    @Nullable
    public View findViewUnderTouch(float touchX, float touchY) {
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(getChildDrawingOrder(childCount, i));
            if (child.getVisibility() == VISIBLE
                    && ViewUtils.isPointInView(child, (int) touchX, (int) touchY)) {
                return child;
            }
        }
        return null;
    }

    @MainThread
    @Nullable
    public Bitmap getBitmap() {
        return BitmapCreator.createBitmap(this);
    }

    @Nullable
    @Override
    public Styleable getStyleable() {
        return mEditText;
    }

    private class ActionListenerImpl implements StickerTouchListener.ActionListener {
        @Nullable
        StickerView mActiveSticker;

        private final float[] mVector = new float[2];

        @Override
        public void onClick(@NonNull StickerView stickerView) {
//            performClick();
        }

        @Override
        public void onStartInteract(@NonNull StickerView stickerView) {
            if (mViewOrderController.moveToTop(stickerView)) {
                invalidate();
            }

            if (mInteractStickerListeners != null) {
                for (InteractStickerListener listener : mInteractStickerListeners) {
                    listener.onStartInteract(stickerView);
                }
            }

            mActiveSticker = stickerView;
        }

        @Override
        public void onStickerMove(@NonNull StickerView stickerView,
                                  boolean isPureMove,
                                  float moveX, float moveY,
                                  float deltaX, float deltaY) {
            translate(stickerView, deltaX, deltaY);

            PointF relativeToParent = ViewUtils.getPointRelativeToParent(stickerView, moveX, moveY);

            if (isPureMove) {
                mRecyclerBinController.showRecycleBin();
                if (ViewUtils.isPointInView(mRecyclerBinView,
                                            (int) relativeToParent.x,
                                            (int) relativeToParent.y)) {
                    if (!mRecyclerBinController.isActive()) {
                        if (mActivateRecycleBinEffect != null) {
                            mActivateRecycleBinEffect.playEffectOnActivate(getContext());
                        }
                    }
                    mRecyclerBinController.activate();
                } else {
                    mRecyclerBinController.deactivate();
                }
            } else {
                mRecyclerBinController.hideRecycleBin();
            }
        }

        @Override
        public void onStickerStopMove(@NonNull StickerView stickerView,
                                      boolean isPureMove,
                                      float movePointX, float movePointY) {
            if (isPureMove) {
                PointF relativeToParent = ViewUtils.getPointRelativeToParent(stickerView, movePointX, movePointY);
                if (mRecyclerBinController.isActive()
                        && ViewUtils.isPointInView(mRecyclerBinView,
                                                   (int) relativeToParent.x,
                                                   (int) relativeToParent.y)) {
                    Log.d(TAG, "remove sticker: " + stickerView);
                    removeView(stickerView);
                }
            }

            mRecyclerBinController.hideRecycleBin();
        }


        @Override
        public void onStickerChangeFocus(@NonNull StickerView stickerView, float focusX, float focusY) {
            if (stickerView.getPivotX() == focusX && stickerView.getPivotY() == focusY) {
                return;
            }

            mVector[0] = mVector[1] = 0;
            stickerView.getMatrix()
                    .mapPoints(mVector);

            float prevX = mVector[0];
            float prevY = mVector[1];

            stickerView.setPivotX(focusX);
            stickerView.setPivotY(focusY);

            mVector[0] = mVector[1] = 0;
            stickerView.getMatrix()
                    .mapPoints(mVector);

            float currX = mVector[0];
            float currY = mVector[1];

            stickerView.setTranslationX(stickerView.getTranslationX() - (currX - prevX));
            stickerView.setTranslationY(stickerView.getTranslationY() - (currY - prevY));
        }

        @Override
        public void onStickerScaleAndRotate(@NonNull StickerView stickerView,
                                            float scaleFactor, float degrees,
                                            float deltaFocusX, float deltaFocusY) {
            float oldScaleFactor = stickerView.getScaleX();
            if (oldScaleFactor * scaleFactor < 10f) {
                stickerView.setScaleX(stickerView.getScaleX() * scaleFactor);
                stickerView.setScaleY(stickerView.getScaleY() * scaleFactor);
            }

            translate(stickerView, deltaFocusX, deltaFocusY);

            float newDegrees = stickerView.getRotation() + degrees;
            if (newDegrees < 180) {
                newDegrees += 360;
            } else if (newDegrees > 180) {
                newDegrees -= 360;
            }
            stickerView.setRotation(newDegrees);
        }

        @Override
        public void onStopInteract(@NonNull StickerView stickerView) {
            if (mInteractStickerListeners != null) {
                for (InteractStickerListener listener : mInteractStickerListeners) {
                    listener.onStopInteract(stickerView);
                }
            }
        }

        private void translate(@NonNull StickerView stickerView, float deltaX, float deltaY) {
            mVector[0] = deltaX;
            mVector[1] = deltaY;
            stickerView.getMatrix()
                    .mapVectors(mVector);
            stickerView.setTranslationX(stickerView.getTranslationX() + mVector[0]);
            stickerView.setTranslationY(stickerView.getTranslationY() + mVector[1]);

        }
    }

    private static class RecycleBinState {
        @NonNull
        final RecyclerBinView mRecyclerBinView;

        private boolean mIsActive;

        public RecycleBinState(@NonNull RecyclerBinView recyclerBinView) {
            mRecyclerBinView = recyclerBinView;
        }

        void showRecycleBin() {
            mRecyclerBinView.show();
        }

        void hideRecycleBin() {
            mRecyclerBinView.hide();
            mIsActive = false;
        }

        void activate() {
            mRecyclerBinView.activate();
            mIsActive = true;
        }

        void deactivate() {
            mRecyclerBinView.deactivate();
            mIsActive = false;
        }

        public boolean isActive() {
            return mIsActive;
        }
    }

    private static class BackgroundInfo {
        final int width;
        final int height;
        final float widthRatio;
        final float heightRatio;

        BackgroundInfo(int width, int height, float widthRatio, float heightRatio) {
            this.width = width;
            this.height = height;
            this.widthRatio = widthRatio;
            this.heightRatio = heightRatio;
        }
    }
}
