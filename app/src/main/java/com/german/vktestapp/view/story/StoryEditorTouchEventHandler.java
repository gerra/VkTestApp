package com.german.vktestapp.view.story;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.german.vktestapp.InteractStickerListener;
import com.german.vktestapp.utils.ViewUtils;
import com.german.vktestapp.view.StickerView;

public class StoryEditorTouchEventHandler implements ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = "[EditorTouchHandler]";

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

        mScaleGestureDetector = new ScaleGestureDetector(storyEditorView.getContext(),
                                                         this);
    }

    @SuppressWarnings("deprecation")
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);

        int pointerIndex = MotionEventCompat.getActionIndex(event);
        int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);
        float x = MotionEventCompat.getX(event, pointerIndex);
        float y = MotionEventCompat.getY(event, pointerIndex);

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

                mState.mActivePointerId = pointerId;
                mState.setFirstTouchX(x, y);
                mState.setLastTouch(x, y);

                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (pointerIndex == 1) {
                    if (mState.mActiveSticker == null) {
                        mState.mActiveSticker = mViewFinder.findAppropriateSticker(mState.mFirstTouchX, mState.mFirstTouchY,
                                                                                   x, y);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (pointerId == mState.mActivePointerId) {
                    onMove(event, x, y);
                }

                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mState.mActiveSticker != null) {
                    mTouchListener.onStickerStopMove(mState.mActiveSticker, x, y);
                }

                if (mState.mActiveSticker == null && ViewUtils.needToPerformClick(event)) {
                    mStoryEditorView.performClick();
                }
                if (mState.mActiveSticker != null) {
                    mTouchListener.onStopInteract(mState.mActiveSticker);
                }

                mState.reset();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                if (mState.mActiveSticker != null) {
                    mTouchListener.onStickerStopMove(mState.mActiveSticker, x, y);
                    mTouchListener.onStopInteract(mState.mActiveSticker);
                }

                mState.reset();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                if (pointerId == mState.mActivePointerId) {
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;

                    float newX = MotionEventCompat.getX(event, newPointerIndex);
                    float newY = MotionEventCompat.getY(event, newPointerIndex);

                    mState.mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);

                    onMove(event, newX, newY);
                }

                break;
            }
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    private void onMove(@NonNull MotionEvent event, float x, float y) {
        float dx = x - mState.mLastTouchX;
        float dy = y - mState.mLastTouchY;

        if (mState.mActiveSticker != null) {
            int activePointerCount = MotionEventCompat.getPointerCount(event)
                    - (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_POINTER_UP ? 1 : 0);

            boolean isPureStickerMove = mState.mFirstDownWasOnSticker
                    && activePointerCount == 1;
            mTouchListener.onStickerMove(mState.mActiveSticker,
                                         isPureStickerMove,
                                         x, y,
                                         dx, dy);
        }

        mState.mLastTouchX = x;
        mState.mLastTouchY = y;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        if (mState.mActiveSticker != null) {
            mTouchListener.onStickerScale(mState.mActiveSticker, scaleFactor);
        }

        return scaleFactor > 0.01;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return mState.mActiveSticker != null;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    private static class State {
        int mActivePointerId;
        float mFirstTouchX;
        float mFirstTouchY;
        float mLastTouchX;
        float mLastTouchY;
        boolean mFirstDownWasOnSticker;
        @Nullable
        StickerView mActiveSticker;

        void setFirstTouchX(float x, float y) {
            mFirstTouchX = x;
            mFirstTouchY = y;
        }

        void setLastTouch(float x, float y) {
            mLastTouchX = x;
            mLastTouchY = y;
        }

        void reset() {
            mActivePointerId = MotionEvent.INVALID_POINTER_ID;
            mFirstDownWasOnSticker = false;
            mActiveSticker = null;
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
                           float activePointerX, float activePointerY,
                           float dx, float dy);
        void onStickerStopMove(@NonNull StickerView stickerView,
                               float activePointerX, float activePointerY);
        void onStickerScale(@NonNull StickerView stickerView, float scaleFactor);
        void onStickerRotate(@NonNull StickerView stickerView);
    }
}
