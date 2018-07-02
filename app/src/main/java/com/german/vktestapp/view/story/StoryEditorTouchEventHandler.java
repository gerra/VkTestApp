package com.german.vktestapp.view.story;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.german.vktestapp.InteractStickerListener;
import com.german.vktestapp.utils.ViewUtils;
import com.german.vktestapp.view.StickerView;

public class StoryEditorTouchEventHandler implements ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = "[EditorTouchHandler]";

    private static final int STATE_NONE = 0;
    private static final int STATE_DRAG = 1;
    private static final int STATE_SCALE = 2;

    @NonNull
    private final StoryEditorView mStoryEditorView;
    @NonNull
    private final ViewFinder mViewFinder;
    @NonNull
    private final TouchListener mTouchListener;

    private final State mState = new State();
    private final ScaleGestureDetector mScaleGestureDetector;

    public StoryEditorTouchEventHandler(@NonNull StoryEditorView storyEditorView,
                                        @NonNull ViewFinder viewFinder,
                                        @NonNull TouchListener touchListener) {
        mStoryEditorView = storyEditorView;
        mViewFinder = viewFinder;
        mTouchListener = touchListener;

        mScaleGestureDetector = new ScaleGestureDetector(storyEditorView.getContext(), this);
    }

    @SuppressWarnings("deprecation")
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);

        int pointerIndex = MotionEventCompat.getActionIndex(event);
        int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);
        float x = MotionEventCompat.getX(event, pointerIndex);
        float y = MotionEventCompat.getY(event, pointerIndex);

//        PointF pointForMove = calculatePointForMove(event);

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                View touchedView = mViewFinder.findViewUnderTouch(x, y);
                if (!(touchedView instanceof StickerView)) {
                    // If it's not Sticker, it's background
                    // (we don't intercept touch event for EditText)
                    mTouchListener.onBackgroundTouchDown();
                } else {
                    mState.mActiveSticker = (StickerView) touchedView;
                    mState.mFirstDownWasOnSticker = true;
                    mTouchListener.onStartInteract(mState.mActiveSticker);
                }

                mState.setFirstTouchX(x, y);

                // TODO: pointForMove?
                mState.mLastTouchForMove = new PointF(x, y);

                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (pointerIndex == 1) {
                    if (mState.mActiveSticker == null) {
                        mState.mActiveSticker = mViewFinder.findAppropriateSticker(mState.mFirstTouchX, mState.mFirstTouchY,
                                                                                   x, y);
                        if (mState.mActiveSticker != null) {
                            mTouchListener.onStartInteract(mState.mActiveSticker);
                        }
                    }
                }

                mState.mLastTouchForMove = calculatePointForMove(event);

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                PointF pointForMove = calculatePointForMove(event);

                if (mState.mActiveSticker != null) {
                    float dx = pointForMove.x - mState.mLastTouchForMove.x;
                    float dy = pointForMove.y - mState.mLastTouchForMove.y;

                    if (Math.abs(dx) > 0 && Math.abs(dy) > 0) {
                        mTouchListener.onStickerMove(mState.mActiveSticker,
                                                     isPureMove(event),
                                                     pointForMove.x, pointForMove.y,
                                                     dx, dy);
                    }
                }

                mState.mLastTouchForMove = pointForMove;

                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                mState.mLastTouchForMove = calculatePointForMove(event);
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mState.mActiveSticker != null) {
                    mTouchListener.onStickerStopMove(mState.mActiveSticker, isPureMove(event), x, y);
                    mTouchListener.onStopInteract(mState.mActiveSticker);
                }

                if (mState.mActiveSticker == null && ViewUtils.needToPerformClick(event)) {
                    mStoryEditorView.performClick();
                }

                mState.reset();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                if (mState.mActiveSticker != null) {
                    mTouchListener.onStickerStopMove(mState.mActiveSticker, isPureMove(event), x, y);
                    mTouchListener.onStopInteract(mState.mActiveSticker);
                }

                mState.reset();
                break;
            }
        }

        return true;
    }

    @NonNull
    private PointF calculatePointForMove(@NonNull MotionEvent event) {
        int count = MotionEventCompat.getPointerCount(event);
        int skipIndex = MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_POINTER_UP
                ? MotionEventCompat.getActionIndex(event)
                : -1;

        float sumX = 0f;
        float sumY = 0f;
        int div = count - (skipIndex != -1 ? 1 : 0);
        for (int i = 0; i < count; i++) {
            if (i == skipIndex) {
                continue;
            }
            sumX += MotionEventCompat.getX(event, i);
            sumY += MotionEventCompat.getY(event, i);
        }

        return new PointF(sumX / div, sumY / div);
    }

    private boolean isPureMove(@NonNull MotionEvent event) {
        int activePointerCount = MotionEventCompat.getPointerCount(event)
                - (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_POINTER_UP ? 1 : 0);

        return mState.mFirstDownWasOnSticker && activePointerCount == 1;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.d(TAG, "onScale");

        float scaleFactor = detector.getScaleFactor();
        if (mState.mActiveSticker != null) {
            mTouchListener.onStickerScale(mState.mActiveSticker,
                                          mState.mFocusX,
                                          mState.mFocusY,
                                          scaleFactor);
        }

        return scaleFactor > 0;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Log.d(TAG, "onScaleBegin");

//        Log.d(TAG, "onScaleBegin()");
        mState.mFocusX = detector.getFocusX();
        mState.mFocusY = detector.getFocusY();
        mState.mState = STATE_SCALE;
        return mState.mActiveSticker != null;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        Log.d(TAG, "onScaleEnd");

        mState.mState = STATE_DRAG;
    }

    private static void fixScaleGestureDetector(@NonNull ScaleGestureDetector scaleGestureDetector) {

    }

    private static class State {
        int mState = STATE_NONE;
        float mFirstTouchX;
        float mFirstTouchY;
        PointF mLastTouchForMove;
        boolean mFirstDownWasOnSticker;
        @Nullable
        StickerView mActiveSticker;

        // For zoom:
        float mFocusX;
        float mFocusY;

        void setFirstTouchX(float x, float y) {
            mFirstTouchX = x;
            mFirstTouchY = y;
        }

        void reset() {
            mFirstDownWasOnSticker = false;
            mActiveSticker = null;
            mState = STATE_NONE;
        }
    }

    interface ViewFinder {
        @Nullable
        View findViewUnderTouch(float touchX, float touchY);
        @Nullable
        StickerView findAppropriateSticker(float x1, float y1, float x2, float y2);
    }

    interface TouchListener extends InteractStickerListener {
        void onBackgroundTouchDown();

        void onStickerMove(@NonNull StickerView stickerView,
                           boolean isPureMove,
                           float movePointX, float movePointY,
                           float dx, float dy);
        void onStickerStopMove(@NonNull StickerView stickerView,
                               boolean isPureMove,
                               float movePointX, float movePointY);
        void onStickerScale(@NonNull StickerView stickerView, float focusX, float focusY, float scaleFactor);
        void onStickerRotate(@NonNull StickerView stickerView);
    }
}
