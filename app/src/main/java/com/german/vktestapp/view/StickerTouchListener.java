package com.german.vktestapp.view;

import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.german.vktestapp.InteractStickerListener;
import com.german.vktestapp.utils.ViewUtils;
import com.german.vktestapp.view.story.Vector2D;

public class StickerTouchListener implements View.OnTouchListener {
    private static final String TAG = "[StickerTouchListener]";

    @NonNull
    private final ActionListener mActionListener;
    @NonNull
    private final ScaleGestureDetector mScaleGestureDetector;

    private float mInitialX;
    private float mInitialY;
    private int mActivePointerId;
    private boolean mIsPureMove;

    public StickerTouchListener(@NonNull ActionListener actionListener) {
        mActionListener = actionListener;
        mScaleGestureDetector = new ScaleGestureDetector(new ScaleGestureListener(mActionListener));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!(v instanceof StickerView)) {
            return false;
        }

        Log.d(TAG, v.hashCode() + " " + event.toString());

        StickerView stickerView = ((StickerView) v);

        mScaleGestureDetector.onTouchEvent(v, event);

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                mIsPureMove = true;

                mInitialX = event.getX();
                mInitialY = event.getY();
                mActivePointerId = event.getPointerId(0);
                mActionListener.onStartInteract(stickerView);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                mIsPureMove = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int pointerIndexMove = event.findPointerIndex(mActivePointerId);
                if (pointerIndexMove != -1) {
                    float currX = event.getX(pointerIndexMove);
                    float currY = event.getY(pointerIndexMove);
                    if (!mScaleGestureDetector.isInProgress()) {
                        mActionListener.onStickerMove(stickerView,
                                                      mIsPureMove,
                                                      currX, currY,
                                                      currX - mInitialX, currY - mInitialY);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = MotionEventCompat.getActionIndex(event);
                int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mInitialX = event.getX(newPointerIndex);
                    mInitialY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (ViewUtils.needToPerformClick(event)) {
                    stickerView.performClick();
                    mActionListener.onClick(stickerView);
                }

                onStopInteract(stickerView, mInitialX, mInitialY);
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                onStopInteract(stickerView, mInitialX, mInitialY);
                break;
            }
        }
        return true;
    }

    private void onStopInteract(@NonNull StickerView stickerView, float lastPointX, float lastPointY) {
        mActionListener.onStickerStopMove(stickerView, mIsPureMove, lastPointX, lastPointY);
        mActionListener.onStopInteract(stickerView);
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @NonNull
        private final ActionListener mActionListener;

        private float mPivotX;
        private float mPivotY;
        private final Vector2D mPrevSpanVector = new Vector2D();

        public ScaleGestureListener(@NonNull ActionListener actionListener) {
            mActionListener = actionListener;
        }

        @Override
        public boolean onScaleBegin(View view, ScaleGestureDetector detector) {
            if (!(view instanceof StickerView)) {
                return false;
            }

            mPivotX = detector.getFocusX();
            mPivotY = detector.getFocusY();
            mPrevSpanVector.set(detector.getCurrentSpanVector());

            mActionListener.onStickerChangeFocus((StickerView) view, mPivotX, mPivotY);

            return true;
        }

        @Override
        public boolean onScale(View view, ScaleGestureDetector detector) {
            if (!(view instanceof StickerView)) {
                return false;
            }
            float deltaFocusX = detector.getFocusX() - mPivotX;
            float deltaFocusY = detector.getFocusY() - mPivotY;
            float deltaScale = detector.getScaleFactor();
            float deltaAngle = Vector2D.getAngle(mPrevSpanVector, detector.getCurrentSpanVector());

            mActionListener.onStickerScaleAndRotate((StickerView) view, deltaScale, deltaAngle, deltaFocusX, deltaFocusY);

            return false;
        }
    }

    public interface ActionListener extends InteractStickerListener {
        void onClick(@NonNull StickerView stickerView);

        void onStickerMove(@NonNull StickerView stickerView,
                           boolean isPureMove,
                           float moveX, float moveY,
                           float deltaX, float deltaY);
        void onStickerStopMove(@NonNull StickerView stickerView,
                               boolean isPureMove,
                               float movePointX, float movePointY);

        void onStickerChangeFocus(@NonNull StickerView stickerView,
                                  float focusX, float focusY);

        void onStickerScaleAndRotate(@NonNull StickerView stickerView,
                                     float scaleFactor, float degrees,
                                     float deltaFocusX, float deltaFocusY);
    }
}
