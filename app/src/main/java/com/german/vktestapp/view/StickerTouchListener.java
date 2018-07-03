package com.german.vktestapp.view;

import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import com.german.vktestapp.InteractStickerListener;
import com.german.vktestapp.utils.ViewUtils;

public class StickerTouchListener implements View.OnTouchListener {
    @NonNull
    private final ActionListener mActionListener;
    @NonNull
    private final ScaleGestureDetector mScaleGestureDetector;

    private float mPrevX;
    private float mPrevY;
    private float mPrevRawX;
    private float mPrevRawY;
    private int mActivePointerId;
    private boolean mIsPureMove;

    public StickerTouchListener(@NonNull ActionListener actionListener) {
        mActionListener = actionListener;
        mScaleGestureDetector = new ScaleGestureDetector(new ScaleGestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!(v instanceof StickerView)) {
            return false;
        }

        StickerView stickerView = ((StickerView) v);

//        mScaleGestureDetector.onTouchEvent(v, event);

        int action = event.getAction();

        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                mIsPureMove = true;

                mPrevX = event.getX();
                mPrevY = event.getY();
                mPrevRawX = event.getRawX();
                mPrevRawY = event.getRawY();
                mActivePointerId = event.getPointerId(0);
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
                                                      currX - mPrevX, currY - mPrevY);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndexPointerUp = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndexPointerUp);
                if (pointerId == mActivePointerId) {
                    int newPointerIndex = pointerIndexPointerUp == 0 ? 1 : 0;
                    mPrevX = event.getX(newPointerIndex);
                    mPrevY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (ViewUtils.needToPerformClick(event)) {
                    mActionListener.onClick(stickerView);
                }

                onStopInteract(stickerView, mPrevX, mPrevY);
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                onStopInteract(stickerView, mPrevX, mPrevY);
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

        void onStickerChangeFocus(@NonNull StickerView stickerView, float focusX, float focusY);

        void onStickerScale(@NonNull StickerView stickerView, float scaleFactor);

        void onStickerRotate(@NonNull StickerView stickerView, float degrees);
    }
}
