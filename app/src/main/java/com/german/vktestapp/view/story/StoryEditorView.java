package com.german.vktestapp.view.story;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
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

import com.german.vktestapp.ActivateRecycleBinEffect;
import com.german.vktestapp.BackgroundSetListener;
import com.german.vktestapp.EditTextProvider;
import com.german.vktestapp.InteractStickerListener;
import com.german.vktestapp.R;
import com.german.vktestapp.backgrounds.Background;
import com.german.vktestapp.stickers.StickerLayoutInfo;
import com.german.vktestapp.utils.ViewUtils;
import com.german.vktestapp.view.RecyclerBinView;
import com.german.vktestapp.view.StickerView;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class StoryEditorView extends ViewGroup
        implements StoryEditorTouchEventHandler.ViewFinder, EditTextProvider {
    private static final String TAG = "[StoryEditorView]";
    
    // Sticker should be not greater that MAX_STICKER_DIMENSION_RATIO of any dimension of this view
    private static final float MAX_STICKER_DIMENSION_RATIO = 0.2f;

    static final long LONG_CLICK_TIME = TimeUnit.MILLISECONDS.toMillis(500);

    private Background mBackground;
    private ImageView mBackgroundImageView;
    private EditText mEditText;
    RecyclerBinView mRecyclerBinView;

    RecycleBinState mRecyclerBinController;

    Handler mHideKeyboardHandler;
    final Runnable mHideKeyboardRunnable = this::hideKeyboard;

    final ViewOrderController mViewOrderController = new ViewOrderController();
    StickersController mStickersController;

    private StoryEditorTouchEventHandler mTouchEventHandler;

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

        mHideKeyboardHandler = new Handler();

        StoryEditorTouchEventHandler.TouchListener touchListener = new TouchListenerImpl();
        mTouchEventHandler = new StoryEditorTouchEventHandler(this, this, touchListener);
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

    public void setActivateRecycleBinEffect(@Nullable ActivateRecycleBinEffect activateRecycleBinEffect) {
        mActivateRecycleBinEffect = activateRecycleBinEffect;
    }

    @Override
    public void addView(View child, LayoutParams params) {
        super.addView(child, params);
        if (child instanceof RecyclerBinView) {
            mRecyclerBinView = (RecyclerBinView) child;
            addBackgroundSetListener(mRecyclerBinView);
            mRecyclerBinController = new RecycleBinState(mRecyclerBinView);
            mViewOrderController.moveHighPriorityViewToTop(mRecyclerBinView);
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
        mViewOrderController.moveToTop(stickerView);

        if (mStickersController == null) {
            mStickersController = new StickersController(this, mViewOrderController);
        }
        // This just added sticker, we need to save it sizes for change background in future
        mStickersController.addSticker(stickerView, getMeasuredWidth(), getMeasuredHeight());

        hideKeyboard();
    }

    @Override
    public void setBackground(@Nullable Drawable drawable) {
        mBackgroundImageView.setImageDrawable(drawable);
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
        Point backgroundSize = measureBackground(widthMeasureSpec, heightMeasureSpec);
        int backgroundWidth = backgroundSize.x;
        int backgroundHeight = backgroundSize.y;

        // Pass to children measured sizes of this view
        int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(backgroundWidth, MeasureSpec.AT_MOST);
        int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(backgroundHeight, MeasureSpec.AT_MOST);

        measureChildWithMargins(mEditText, newWidthMeasureSpec, 0, newHeightMeasureSpec, 0);
        measureStickers(backgroundWidth, backgroundHeight);
        measureChild(mRecyclerBinView, newWidthMeasureSpec, newHeightMeasureSpec);

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
        layoutRecycleBin(parentLeft, parentTop, parentRight, parentBottom);
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
                int y = (int) MotionEventCompat.getY(event, 0);
                View touchedChild = findViewUnderTouch(x, y);
                if (touchedChild instanceof EditText) {
                    return false;
                }
                break;
        }

        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mTouchEventHandler.onTouchEvent(event);
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
        if (child instanceof StickerView) {
            mStickersController.removeSticker((StickerView) child);
        }
        if (child == mRecyclerBinView) {
            removeBackgroundSetListener(mRecyclerBinView);
        }
        mViewOrderController.removeView(child);
    }

    @NonNull
    private Point measureBackground(int widthMeasureSpec, int heightMeasureSpec) {
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

        return new Point(backgroundWidth, backgroundHeight);
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
                // At first check what size sticker wants to be
                measureChild(stickerView,
                             MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED),
                             MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                // TODO:
                // And now check that any its dimension is not greater than 1/5 of corresponding
                // parent dimension
//                int stickerWidth = stickerView.getMeasuredWidth();
//                int stickerHeight = stickerView.getMeasuredHeight();
//                float widthRatio = 1f * stickerWidth / width;
//                float heightRatio = 1f * stickerHeight / height;
//                if (widthRatio > MAX_STICKER_DIMENSION_RATIO || heightRatio > MAX_STICKER_DIMENSION_RATIO) {
//                    
//                }
            } else {
                StickerLayoutInfo info = mStickersController.getLayoutInfo(stickerView);
                if (info != null) {
                    float initialBackgroundWidth = info.getHolderBackgroundWidth();
                    float initialBackgroundHeight = info.getHolderBackgroundHeight();

                    float prevSelfRatioX = info.getSelfRatioX();
                    float prevSelfRatioY = info.getSelfRatioY();
                    float prevSelfRatio = Math.min(prevSelfRatioX, prevSelfRatioY);

                    float selfRatioX = width / initialBackgroundWidth;
                    float selfRatioY = height / initialBackgroundHeight;
                    float selfRatio = Math.min(selfRatioX, selfRatioY);
                    info.setSelfRatios(selfRatioX, selfRatioY);

                    float scaleFactor = stickerView.getScaleX() / prevSelfRatio;
                    stickerView.setScaleX(scaleFactor * selfRatio);
                    stickerView.setScaleY(scaleFactor * selfRatio);

                    float prevBackgroundWidth = prevSelfRatioX * initialBackgroundWidth;
                    float prevBackgroundHeight = prevSelfRatioY * initialBackgroundHeight;

                    float translationXRatio = width / prevBackgroundWidth;
                    float translationYRatio = height / prevBackgroundHeight;

                    stickerView.setTranslationX(translationXRatio * stickerView.getTranslationX());
                    stickerView.setTranslationY(translationYRatio * stickerView.getTranslationY());
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
        StickerLayoutInfo layoutInfo = mStickersController.getLayoutInfo(stickerView);
        if (layoutInfo == null) {
            Log.w(TAG, "wtf? StickerView is\'nt in StickersController?");
            return;
        }

        // Initial sticker center is in the center of parent,
        // for positioning translation is used.
        float stickerCenterX = (parentRight - parentLeft) * 0.5f;
        float stickerCenterY = (parentBottom - parentTop) * 0.5f;

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
        if (TextUtils.isEmpty(mEditText.getText())) {
            mEditText.setVisibility(INVISIBLE);
        }
    }

    static boolean isLayoutInfoInvalid(@Nullable StickerLayoutInfo layoutInfo) {
        if (layoutInfo == null) {
            Log.w(TAG, "wtf? LayoutInfo is null?");
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public View findViewUnderTouch(float touchX, float touchY) {
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(getChildDrawingOrder(childCount, i));
            if (ViewUtils.isPointInView(child, (int) touchX, (int) touchY)) {
                return child;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public StickerView findAppropriateSticker(float x1, float y1, float x2, float y2) {
        float centerX = (x1 + x2) / 2;
        float centerY = (y1 + y2) / 2;

        StickerView appropriateStickerView = null;
        double minDistance = Double.POSITIVE_INFINITY;

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof StickerView) {
                StickerView stickerView = ((StickerView) child);
                double distance = Math.hypot(stickerView.getPivotX() - centerX,
                                             stickerView.getPivotY() - centerY);
                if (appropriateStickerView == null || distance < minDistance) {
                    appropriateStickerView = stickerView;
                    minDistance = distance;
                }
            }
        }

        return appropriateStickerView;
    }

    @UiThread
    @Nullable
    public Bitmap getBitmap() {
        if (getMeasuredWidth() <= 0 || getMeasuredHeight() <= 0) {
            return null;
        }

        // Save state
        int editTextVisibility = mEditText.getVisibility();
        boolean editTextCursorVisible = mEditText.isCursorVisible();
        int recycleBinVisibility = mRecyclerBinView.getVisibility();

        // Hide unnecessary views
        if (mEditText.getVisibility() == VISIBLE
                && TextUtils.isEmpty(mEditText.getText())) {
            mEditText.setVisibility(INVISIBLE);
        }
        if (mEditText.getVisibility() == VISIBLE) {
            mEditText.setCursorVisible(false);
        }
        if (mRecyclerBinView.getVisibility() == VISIBLE) {
            mRecyclerBinView.setVisibility(INVISIBLE);
        }

        setDrawingCacheEnabled(true);
        Bitmap bitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, false);
        setDrawingCacheEnabled(false);

        // Restore state
        mEditText.setVisibility(editTextVisibility);
        mEditText.setCursorVisible(editTextCursorVisible);
        mRecyclerBinView.setVisibility(recycleBinVisibility);

        return bitmap;
    }

    @Nullable
    @Override
    public EditText getEditText() {
        return mEditText;
    }

    private class TouchListenerImpl implements StoryEditorTouchEventHandler.TouchListener {
        @Override
        public void onBackgroundTouchDown() {
            mHideKeyboardHandler.postDelayed(mHideKeyboardRunnable, LONG_CLICK_TIME);
        }

        @Override
        public void onStickerMove(@NonNull StickerView stickerView,
                                  boolean isPureMove,
                                  float activePointerX, float activePointerY,
                                  float dx, float dy) {
            StickerLayoutInfo layoutInfo = mStickersController.getLayoutInfo(stickerView);
            if (isLayoutInfoInvalid(layoutInfo)) {
                return;
            }

            stickerView.setTranslationX(stickerView.getTranslationX() + dx);
            stickerView.setTranslationY(stickerView.getTranslationY() + dy);

            if (isPureMove) {
                mRecyclerBinController.showRecycleBin(stickerView);
                if (ViewUtils.isPointInView(mRecyclerBinView,
                                            (int) activePointerX,
                                            (int) activePointerY)) {
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
                                      float activePointerX, float activePointerY) {
            if (ViewUtils.isPointInView(mRecyclerBinView,
                                        (int) activePointerX,
                                        (int) activePointerY)
                    && mRecyclerBinController.checkDelete(stickerView)) {
                removeView(stickerView);
            }

            mRecyclerBinController.hideRecycleBin();
        }

        @Override
        public void onStickerScale(@NonNull StickerView stickerView, float scaleFactor) {
            stickerView.setScaleX(stickerView.getScaleX() * scaleFactor);
            stickerView.setScaleY(stickerView.getScaleY() * scaleFactor);
        }

        @Override
        public void onStickerRotate(@NonNull StickerView stickerView) {

        }

        @Override
        public void onStartInteract(@NonNull StickerView stickerView) {
            if (mViewOrderController.moveToTop(stickerView)) {
                invalidate();
            }

            StickerLayoutInfo layoutInfo = mStickersController.getLayoutInfo(stickerView);
            if (isLayoutInfoInvalid(layoutInfo)) {
                return;
            }

            // Wtf, AndroidStudio?
            //noinspection ConstantConditions
            layoutInfo.setTouched(true);

            if (mInteractStickerListeners != null) {
                for (InteractStickerListener listener : mInteractStickerListeners) {
                    listener.onStartInteract(stickerView);
                }
            }
        }

        @Override
        public void onStopInteract(@NonNull StickerView stickerView) {
            StickerLayoutInfo layoutInfo = mStickersController.getLayoutInfo(stickerView);
            if (isLayoutInfoInvalid(layoutInfo)) {
                return;
            }

            // Wtf, AndroidStudio?
            //noinspection ConstantConditions
            layoutInfo.setTouched(false);

            if (mInteractStickerListeners != null) {
                for (InteractStickerListener listener : mInteractStickerListeners) {
                    listener.onStopInteract(stickerView);
                }
            }
        }
    }

    private static class RecycleBinState {
        final RecyclerBinView mRecyclerBinView;

        @Nullable
        private View mDeletingView;
        private boolean mIsActive;

        public RecycleBinState(RecyclerBinView recyclerBinView) {
            mRecyclerBinView = recyclerBinView;
        }

        void showRecycleBin(@NonNull View deletingView) {
            mRecyclerBinView.show();
            mDeletingView = deletingView;
        }

        void hideRecycleBin() {
            mRecyclerBinView.hide();
            mDeletingView = null;
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

        boolean checkDelete(@NonNull View deletingView) {
            return deletingView == mDeletingView;
        }

        public boolean isActive() {
            return mIsActive;
        }
    }
}