package com.german.vktestapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.german.vktestapp.utils.ViewUtils;
import com.german.vktestapp.view.StickerView;

public class StoryEditorTouchEventHandler {
    private static final String TAG = "[EditorTouchHandler]";

    @NonNull
    private final StoryEditorView mStoryEditorView;
    @NonNull
    private final ViewFinder mViewFinder;
    @NonNull
    private final TouchListener mTouchListener;

    private final State mState = new State();

    public StoryEditorTouchEventHandler(@NonNull StoryEditorView storyEditorView,
                                        @NonNull ViewFinder viewFinder,
                                        @NonNull TouchListener touchListener) {
        mStoryEditorView = storyEditorView;
        mViewFinder = viewFinder;
        mTouchListener = touchListener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, event.toString());

        int pointerIndex = MotionEventCompat.getActionIndex(event);
        int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);
        float x = MotionEventCompat.getX(event, pointerIndex);
        float y = MotionEventCompat.getY(event, pointerIndex);

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                View touchedView = mViewFinder.findView(x, y);
                if (!(touchedView instanceof StickerView)) {
                    // If it's not Sticker, it's background
                    mTouchListener.onBackgroundTouchDown();
                } else {
                    mState.mActiveSticker = (StickerView) touchedView;
                    mState.mFirstDownWasOnSticker = true;
                    mTouchListener.onStickerTouchDown(mState.mActiveSticker);
                }

                mState.mActivePointerId = pointerId;
                mState.mLastTouchX = x;
                mState.mLastTouchY = y;

                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {

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

                mState.reset();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                if (mState.mActiveSticker != null) {
                    mTouchListener.onStickerStopMove(mState.mActiveSticker, x, y);
                }

                mState.reset();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                if (pointerId == mState.mActivePointerId) {
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    Log.d(TAG, "Pointer up, New pointer index: " + newPointerIndex);
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

    private static class State {
        int mActivePointerId;
        float mLastTouchX;
        float mLastTouchY;
        boolean mFirstDownWasOnSticker;
        @Nullable
        StickerView mActiveSticker;

        void reset() {
            mActivePointerId = MotionEvent.INVALID_POINTER_ID;
            mFirstDownWasOnSticker = false;
            mActiveSticker = null;
        }
    }

    interface ViewFinder {
        @Nullable
        View findView(float touchX, float touchY);
    }

    interface TouchListener {
        void onStickerTouchDown(@NonNull StickerView stickerView);
        void onBackgroundTouchDown();

        void onStickerMove(@NonNull StickerView stickerView,
                           boolean isPureMove,
                           float activePointerX, float activePointerY,
                           float dx, float dy);
        void onStickerStopMove(@NonNull StickerView stickerView,
                               float activePointerX, float activePointerY);
        void onStickerScale(@NonNull StickerView stickerView);
        void onStickerRotate(@NonNull StickerView stickerView);
    }
}
