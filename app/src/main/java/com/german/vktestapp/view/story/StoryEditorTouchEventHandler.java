package com.german.vktestapp.view.story;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.german.vktestapp.InteractStickerListener;
import com.german.vktestapp.utils.ViewUtils;
import com.german.vktestapp.view.StickerView;

public class StoryEditorTouchEventHandler {
    private static final String TAG = "[EditorTouchHandler]";

    private static PointF NIL_POINT = new PointF(0, 0);

    @NonNull
    private final ViewFinder mViewFinder;
    @NonNull
    private final TouchListener mTouchListener;

    private final State mState = new State();
    private final ZoomState mZoomState;

//    private final ScaleGestureDetector mScaleGestureDetector;

    public StoryEditorTouchEventHandler(@NonNull Context context,
                                        @NonNull ViewFinder viewFinder,
                                        @NonNull TouchListener touchListener) {
        mViewFinder = viewFinder;
        mTouchListener = touchListener;

        float spanSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
        float minSpan;
        Resources resources = context.getResources();
        int minSpanResId = resources.getIdentifier("config_minScalingSpan", "dimen", "android");
        if (minSpanResId > 0) {
            minSpan = resources.getDimensionPixelSize(minSpanResId);
        } else {
            Log.w(TAG, "minSpan resource not found");
            minSpan = 100;
        }

        mZoomState = new ZoomState(spanSlop, minSpan);
    }

    @SuppressWarnings("deprecation")
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex = MotionEventCompat.getActionIndex(event);

        PointF eventFocus = calculateFocusPoint(event);

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                View touchedView = mViewFinder.findViewUnderTouch(eventFocus.x, eventFocus.y);
                if (touchedView instanceof StickerView) {
                    mState.mActiveSticker = (StickerView) touchedView;
                    mState.mIsOnlyMove = true;
                    mTouchListener.onStartInteract(mState.mActiveSticker);
                } else {
                    // If it's not Sticker, it's background
                    // (we don't intercept touch event for EditText)
                    mTouchListener.onBackgroundTouchDown();
                }

                mState.mLastTouchForMove = eventFocus;

                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (mState.mActiveSticker == null
                        && MotionEventCompat.getPointerCount(event) == 2) {
                    mState.mActiveSticker = mViewFinder.findAppropriateSticker(MotionEventCompat.getX(event, 0),
                                                                               MotionEventCompat.getY(event, 0),
                                                                               MotionEventCompat.getX(event, 1),
                                                                               MotionEventCompat.getY(event, 1));
                    if (mState.mActiveSticker != null) {
                        mTouchListener.onStartInteract(mState.mActiveSticker);
                    }
                }

                mState.mIsOnlyMove = false;
                mState.mLastTouchForMove = eventFocus;

                if (mState.mActiveSticker != null) {
                    handleZoom(event, mState.mActiveSticker, eventFocus);
                }

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mState.mActiveSticker != null) {
                    float dx = eventFocus.x - mState.mLastTouchForMove.x;
                    float dy = eventFocus.y - mState.mLastTouchForMove.y;

                    if (Math.abs(dx) > 0 || Math.abs(dy) > 0) {
                        mTouchListener.onStickerMove(mState.mActiveSticker,
                                                     isPureMove(event),
                                                     eventFocus.x, eventFocus.y,
                                                     dx, dy);
                    }
                }

                mState.mLastTouchForMove = eventFocus;

                if (mState.mActiveSticker != null && MotionEventCompat.getPointerCount(event) >= 2) {
                    handleZoom(event, mState.mActiveSticker, eventFocus);
                }

                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                mState.mLastTouchForMove = eventFocus;

                if (mState.mActiveSticker != null) {
                    handleZoom(event, mState.mActiveSticker, eventFocus);
                }

                break;
            }
            case MotionEvent.ACTION_UP: {
                if (ViewUtils.needToPerformClick(event)) {
                    mTouchListener.onCLick();
                }

                onStopInteract(event, eventFocus);
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                onStopInteract(event, eventFocus);
                break;
            }
        }

        return true;
    }

    @NonNull
    private PointF calculateFocusPoint(@NonNull MotionEvent event) {
        return calculateDeviationPoint(event, NIL_POINT);
    }

    @NonNull
    private PointF calculateDeviationPoint(@NonNull MotionEvent event, @NonNull PointF focus) {
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
            sumX += Math.abs(focus.x - MotionEventCompat.getX(event, i));
            sumY += Math.abs(focus.y - MotionEventCompat.getY(event, i));
        }

        return new PointF(sumX / div, sumY / div);
    }

    private boolean isPureMove(@NonNull MotionEvent event) {
        int activePointerCount = MotionEventCompat.getPointerCount(event)
                - (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_POINTER_UP ? 1 : 0);

        return mState.mIsOnlyMove && activePointerCount == 1;
    }

    // For ACTION_MOVE, ACTION_POINTER_DOWN, ACTION_POINTER_UP
    private void handleZoom(@NonNull MotionEvent event,
                            @NonNull StickerView stickerView,
                            @NonNull PointF focus) {
        boolean isPointerAction = MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_POINTER_DOWN
                || MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_POINTER_UP;

        mZoomState.mCurrentFocus = focus;

        PointF deviation = calculateDeviationPoint(event, focus);
        float span = (float) Math.hypot(deviation.x * 2, deviation.y * 2);
        boolean wasInProgress = mZoomState.mInProgress;

        mZoomState.mCurrentFocus = focus;
        if (mZoomState.mInProgress && (span < mZoomState.mMinSpan || isPointerAction)) {
            mZoomState.mInitialSpan = span;
            mZoomState.mInProgress = false;
        }
        if (isPointerAction) {
            mZoomState.mInitialSpan = mZoomState.mPrevSpan = mZoomState.mCurSpan = span;
        }

        if (!mZoomState.mInProgress
                && span > mZoomState.mMinSpan
                && (wasInProgress || Math.abs(span - mZoomState.mInitialSpan) > mZoomState.mSpanSlop)) {
            mZoomState.mPrevSpan = mZoomState.mCurSpan = span;
            mZoomState.mInProgress = true;
            mTouchListener.onStickerStartScale(stickerView, focus.x, focus.y);
        }

        if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_MOVE) {
            mZoomState.mCurSpan = span;
            if (mZoomState.mInProgress) {
                float scaleFactor = mZoomState.mPrevSpan > 0
                        ? mZoomState.mCurSpan / mZoomState.mPrevSpan
                        : 1;
                mTouchListener.onStickerScale(stickerView, scaleFactor);
            }

            mZoomState.mPrevSpan = mZoomState.mCurSpan;
        }
    }

    private void onStopInteract(@NonNull MotionEvent event, @NonNull PointF lastPoint) {
        if (mState.mActiveSticker != null) {
            mTouchListener.onStickerStopMove(mState.mActiveSticker,
                                             isPureMove(event),
                                             lastPoint.x, lastPoint.y);
            mTouchListener.onStopInteract(mState.mActiveSticker);
        }

        mState.reset();
        mZoomState.reset();
    }

//    @Override
//    public boolean onScale(ScaleGestureDetector detector) {
//        float scaleFactor = detector.getScaleFactor();
//        if (mState.mActiveSticker != null) {
//            mTouchListener.onStickerScale(mState.mActiveSticker, scaleFactor);
//        }
//
//        return scaleFactor > 0;
//    }
//
//    @Override
//    public boolean onScaleBegin(ScaleGestureDetector detector) {
//        if (mState.mActiveSticker != null) {
//            mTouchListener.onStickerStartScale(mState.mActiveSticker,
//                                               detector.getFocusX(),
//                                               detector.getFocusY());
//
//            return true;
//        }
//
//        return false;
//    }
//
//    @Override
//    public void onScaleEnd(ScaleGestureDetector detector) {
//    }

    private static class State {
        @Nullable
        StickerView mActiveSticker;
        PointF mLastTouchForMove;
        boolean mIsOnlyMove;

        void reset() {
            mActiveSticker = null;
            mIsOnlyMove = false;
        }
    }

    private static class ZoomState {
        final float mSpanSlop;
        final float mMinSpan;

        PointF mCurrentFocus;

        boolean mInProgress;
        float mInitialSpan;
        float mPrevSpan;
        float mCurSpan;

        ZoomState(float spanSlop, float minSpan) {
            mSpanSlop = spanSlop;
            mMinSpan = minSpan;
        }

        void reset() {
            mInProgress = false;
            mInitialSpan = 0;
        }
    }

    interface ViewFinder {
        @Nullable
        View findViewUnderTouch(float touchX, float touchY);
        @Nullable
        StickerView findAppropriateSticker(float x1, float y1, float x2, float y2);
    }

    interface TouchListener extends InteractStickerListener {
        void onCLick();

        void onBackgroundTouchDown();

        void onStickerMove(@NonNull StickerView stickerView,
                           boolean isPureMove,
                           float movePointX, float movePointY,
                           float dx, float dy);
        void onStickerStopMove(@NonNull StickerView stickerView,
                               boolean isPureMove,
                               float movePointX, float movePointY);

        void onStickerStartScale(@NonNull StickerView stickerView, float focusX, float focusY);
        void onStickerScale(@NonNull StickerView stickerView, float scaleFactor);

        void onStickerStartRotate(@NonNull StickerView stickerView, float focusX, float focusY);
        void onStickerRotate(@NonNull StickerView stickerView, float degrees);
    }
}
