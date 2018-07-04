package com.german.vktestapp.view.story;

// !!!
// Use MotionEventCompat because of on my OnePlus5 for some cases deprecated values are received
// !!!
public class StoryEditorTouchEventHandler {
//    private static final String TAG = "[EditorTouchHandler]";
//
//    private static final PointF NIL_POINT = new PointF(0, 0);
//
//    @NonNull
//    private final ViewFinder mViewFinder;
//    @NonNull
//    private final TouchListener mTouchListener;
//
//    private final State mState = new State();
//    private final ZoomState mZoomState;
//    private final RotateState mRotateState = new RotateState();
//
//    private final ScaleGestureDetector mScaleGestureDetector;
//
//    public StoryEditorTouchEventHandler(@NonNull Context context,
//                                        @NonNull ViewFinder viewFinder,
//                                        @NonNull TouchListener touchListener) {
//        mViewFinder = viewFinder;
//        mTouchListener = touchListener;
//
//        float spanSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
//        float minSpan;
//        Resources resources = context.getResources();
//        int minSpanResId = resources.getIdentifier("config_minScalingSpan", "dimen", "android");
//        if (minSpanResId > 0) {
//            minSpan = resources.getDimensionPixelSize(minSpanResId);
//        } else {
//            Log.w(TAG, "minSpan resource not found");
//            minSpan = 100;
//        }
//
//        mZoomState = new ZoomState(spanSlop, minSpan);
//
//        mScaleGestureDetector = new ScaleGestureDetector()
//    }
//
//    @SuppressWarnings("deprecation")
//    public boolean onTouchEvent(MotionEvent event) {
//        final int pointerIndex = MotionEventCompat.getActionIndex(event);
//        final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);
//        final float x = MotionEventCompat.getX(event, pointerIndex);
//        final float y = MotionEventCompat.getY(event, pointerIndex);
//        final PointF currentPoint = new PointF(x, y);
//
//        final PointF eventFocus = calculateFocusPoint(event);
//
//        switch (MotionEventCompat.getActionMasked(event)) {
//            case MotionEvent.ACTION_DOWN: {
//                View touchedView = mViewFinder.findViewUnderTouch(eventFocus.x, eventFocus.y);
//                if (touchedView instanceof StickerView) {
//                    mState.mActiveSticker = (StickerView) touchedView;
//                    mState.mIsOnlyMove = true;
//                    mTouchListener.onStartInteract(mState.mActiveSticker);
//                } else {
//                    // If it's not Sticker, it's background
//                    // (we don't intercept touch event for EditText)
//                    mTouchListener.onBackgroundTouchDown();
//                }
//
//                handleRotate(event, mState.mActiveSticker, eventFocus, mState.mCoordinates);
//
//                mState.mLastTouchForMove = eventFocus;
//                mState.mCoordinates.put(pointerId, currentPoint);
//
//                break;
//            }
//            case MotionEvent.ACTION_POINTER_DOWN: {
//                if (mState.mActiveSticker == null
//                        && MotionEventCompat.getPointerCount(event) == 2) {
//                    mState.mActiveSticker = mViewFinder.findAppropriateSticker(MotionEventCompat.getX(event, 0),
//                                                                               MotionEventCompat.getY(event, 0),
//                                                                               MotionEventCompat.getX(event, 1),
//                                                                               MotionEventCompat.getY(event, 1));
//                    if (mState.mActiveSticker != null) {
//                        mTouchListener.onStartInteract(mState.mActiveSticker);
//                    }
//                }
//
//                if (mState.mActiveSticker != null) {
//                    handleZoom(event, mState.mActiveSticker, eventFocus);
//                    handleRotate(event, mState.mActiveSticker, eventFocus, mState.mCoordinates);
//                }
//
//                mState.mIsOnlyMove = false;
//                mState.mLastTouchForMove = eventFocus;
//                mState.mCoordinates.put(pointerId, currentPoint);
//
//                break;
//            }
//            case MotionEvent.ACTION_MOVE: {
//                if (mState.mActiveSticker != null) {
//                    float dx = eventFocus.x - mState.mLastTouchForMove.x;
//                    float dy = eventFocus.y - mState.mLastTouchForMove.y;
//
//                    if (Math.abs(dx) > 0 || Math.abs(dy) > 0) {
//                        mTouchListener.onStickerMove(mState.mActiveSticker,
//                                                     isPureMove(event),
//                                                     eventFocus.x, eventFocus.y,
//                                                     dx, dy);
//                    }
//                }
//
//                if (mState.mActiveSticker != null && MotionEventCompat.getPointerCount(event) >= 2) {
//                    handleZoom(event, mState.mActiveSticker, eventFocus);
//                    handleRotate(event, mState.mActiveSticker, eventFocus, mState.mCoordinates);
//                }
//
//                mState.mLastTouchForMove = eventFocus;
//                updateMoveCoordinates(event);
//
//                break;
//            }
//            case MotionEvent.ACTION_POINTER_UP: {
//                if (mState.mActiveSticker != null) {
//                    handleZoom(event, mState.mActiveSticker, eventFocus);
//                    handleRotate(event, mState.mActiveSticker, eventFocus, mState.mCoordinates);
//                }
//
//                mState.mLastTouchForMove = eventFocus;
//                mState.mCoordinates.remove(pointerId);
//
//                break;
//            }
//            case MotionEvent.ACTION_UP: {
//                if (ViewUtils.needToPerformClick(event)) {
//                    mTouchListener.onCLick();
//                }
//
//                onStopInteract(event, eventFocus);
//                break;
//            }
//            case MotionEvent.ACTION_CANCEL: {
//                onStopInteract(event, eventFocus);
//                break;
//            }
//        }
//
//        return true;
//    }
//
//    @NonNull
//    private PointF calculateFocusPoint(@NonNull MotionEvent event) {
//        return calculateDeviationPoint(event, NIL_POINT);
//    }
//
//    @NonNull
//    private PointF calculateDeviationPoint(@NonNull MotionEvent event, @NonNull PointF focus) {
//        int count = MotionEventCompat.getPointerCount(event);
//        int skipIndex = MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_POINTER_UP
//                ? MotionEventCompat.getActionIndex(event)
//                : -1;
//
//        float sumX = 0f;
//        float sumY = 0f;
//        int div = count - (skipIndex != -1 ? 1 : 0);
//        for (int i = 0; i < count; i++) {
//            if (i == skipIndex) {
//                continue;
//            }
//            sumX += Math.abs(focus.x - MotionEventCompat.getX(event, i));
//            sumY += Math.abs(focus.y - MotionEventCompat.getY(event, i));
//        }
//
//        return new PointF(sumX / div, sumY / div);
//    }
//
//    private boolean isPureMove(@NonNull MotionEvent event) {
//        int activePointerCount = MotionEventCompat.getPointerCount(event)
//                - (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_POINTER_UP ? 1 : 0);
//
//        return mState.mIsOnlyMove && activePointerCount == 1;
//    }
//
//    // For ACTION_MOVE, ACTION_POINTER_DOWN, ACTION_POINTER_UP
//    // count(event) >= 2
//    private void handleZoom(@NonNull MotionEvent event,
//                            @NonNull StickerView stickerView,
//                            @NonNull PointF focus) {
//        int action = MotionEventCompat.getActionMasked(event);
//
//        boolean isPointerAction = action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_POINTER_UP;
//
//        PointF deviation = calculateDeviationPoint(event, focus);
//        float span = (float) Math.hypot(deviation.x * 2, deviation.y * 2);
//        boolean wasInProgress = mZoomState.mInProgress;
//
//        if (mZoomState.mInProgress && (span < mZoomState.mMinSpan || isPointerAction)) {
//            mZoomState.mInitialSpan = span;
//            mZoomState.mInProgress = false;
//        }
//        if (isPointerAction) {
//            mZoomState.mInitialSpan = mZoomState.mPrevSpan = mZoomState.mCurSpan = span;
//        }
//
//        if (!mZoomState.mInProgress
//                && span > mZoomState.mMinSpan
//                && (wasInProgress || Math.abs(span - mZoomState.mInitialSpan) > mZoomState.mSpanSlop)) {
//            mZoomState.mPrevSpan = mZoomState.mCurSpan = span;
//            mZoomState.mInProgress = true;
//            mTouchListener.onStickerChangeFocus(stickerView, focus.x, focus.y);
//        }
//
//        if (action == MotionEvent.ACTION_MOVE) {
//            mZoomState.mCurSpan = span;
//            if (mZoomState.mInProgress) {
//                float scaleFactor = mZoomState.mPrevSpan > 0
//                        ? mZoomState.mCurSpan / mZoomState.mPrevSpan
//                        : 1;
//                mTouchListener.onStickerScaleAndRotate(stickerView, scaleFactor);
//            }
//
//            mZoomState.mPrevSpan = mZoomState.mCurSpan;
//        }
//    }
//
//    // For ACTION_DOWN, ACTION_MOVE, ACTION_POINTER_DOWN, ACTION_POINTER_UP
//    // count(event) >= 2
//    private void handleRotate(@NonNull MotionEvent event,
//                              @Nullable StickerView stickerView,
//                              @NonNull PointF focus,
//                              @NonNull SparseArray<PointF> coordinates) {
//        int action = MotionEventCompat.getActionMasked(event);
//        int index = MotionEventCompat.getActionIndex(event);
//        int id = MotionEventCompat.getPointerId(event, index);
//
//        boolean updatePivot;
//        if (action == MotionEvent.ACTION_DOWN) {
//            mRotateState.mFirstPointerId = id;
//            return;
//        } else if (action == MotionEvent.ACTION_POINTER_UP) {
//            if (id == mRotateState.mFirstPointerId) {
//                mRotateState.reset();
//                return;
//            } else if (id == mRotateState.mSecondPointerId) { // ??
//                mRotateState.mSecondPointerId = -1;
//                return;
//            } else {
//                updatePivot = false;
//            }
//        } else if (mRotateState.mFirstPointerId == -1) {
//            return;
//        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
//            mRotateState.mSecondPointerId = id;
//            updatePivot = true;
//        } else /*if (action == MotionEvent.ACTION_MOVE)*/ {
//            if (id != mRotateState.mFirstPointerId && id != mRotateState.mSecondPointerId) {
//                return;
//            }
//
//            updatePivot = (id == mRotateState.mFirstPointerId);
//        }
//
//        if (stickerView == null || mRotateState.mFirstPointerId == -1 || mRotateState.mSecondPointerId == -1) {
//            return;
//        }
//
//        int firstIndex = MotionEventCompat.findPointerIndex(event, mRotateState.mFirstPointerId);
//        int secondIndex = MotionEventCompat.findPointerIndex(event, mRotateState.mSecondPointerId);
//
//        float x1 = MotionEventCompat.getX(event, firstIndex);
//        float y1 = MotionEventCompat.getY(event, firstIndex);
//
//        float x2 = MotionEventCompat.getX(event, secondIndex);
//        float y2 = MotionEventCompat.getY(event, secondIndex);
//
//        if (updatePivot) {
//            mRotateState.mInProgress = true;
//            mRotateState.mCurrentFocus = focus;
//            mTouchListener.onStickerStartRotate(stickerView, x1, y1);
//        }
//
//        if (action == MotionEvent.ACTION_MOVE) {
//            PointF prevCoord = coordinates.get(mRotateState.mSecondPointerId);
//            if (prevCoord == null) {
//                Log.e(TAG, "WTF???");
//                return;
//            }
//
////            Log.d(TAG, "coords: (" + mRotateState.mFirstPointerId + " " + mRotateState.mSecondPointerId + ") " +
////                    + prevCoord.x + "," + prevCoord.y + " -> " + x2 + "," + y2 + "; " + coordinates.toString());
//
//            float vx1 = prevCoord.x - x1;
//            float vy1 = prevCoord.y - y1;
////
//            float vx2 = x2 - x1;
//            float vy2 = y2 - y1;
//
////            float dx = x2 - x1;
////            float dy = y2 - y1;
//
//            float lengthMultiply = (float) (Math.hypot(vx1, vy1) * Math.hypot(vx2, vy2));
//            float scalar = vx1 * vy2 - vy1 * vx2;
////            Log.d(TAG, "cos=" + scalar / lengthMultiply);
//            float radians = (float) Math.asin(scalar / lengthMultiply);
//            float degrees = (float) Math.toDegrees(radians);
//
////            Log.d(TAG, "coords: " + prevCoord.x + "," + prevCoord.y + " -> " + x2 + "," + y2);
//            Log.d(TAG, "degrees: " + "(" + vx1 + "," + vy1 + "), " + "(" + vx2 + "," + vy2 + "), " + degrees);
//
//            mTouchListener.onStickerRotate(stickerView, degrees);
//        }
//
//        return;
//    }
//
//    private void onStopInteract(@NonNull MotionEvent event, @NonNull PointF lastPoint) {
//        if (mState.mActiveSticker != null) {
//            mTouchListener.onStickerStopMove(mState.mActiveSticker,
//                                             isPureMove(event),
//                                             lastPoint.x, lastPoint.y);
//            mTouchListener.onStopInteract(mState.mActiveSticker);
//        }
//
//        mState.reset();
//        mZoomState.reset();
//        mRotateState.reset();
//    }
//
//    private void updateMoveCoordinates(@NonNull MotionEvent event) {
//        int count = event.getPointerCount();
//        for (int i = 0; i < count; i++) {
//            int id = MotionEventCompat.getPointerId(event, i);
//            mState.mCoordinates.put(id, new PointF(MotionEventCompat.getX(event, i),
//                                                   MotionEventCompat.getY(event, i)));
//        }
//    }
//
//    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//        private float mPivotX;
//        private float mPivotY;
//        private final Vector2D mPrevSpanVector = new Vector2D();
//
//        @Override
//        public boolean onScaleBegin(View view, ScaleGestureDetector detector) {
//            mPivotX = detector.getFocusX();
//            mPivotY = detector.getFocusY();
//            mPrevSpanVector.set(detector.getCurrentSpanVector());
//            return true;
//        }
//
//        @Override
//        public boolean onScale(View view, ScaleGestureDetector detector) {
//            mTouchListener.onStickerChangeFocus();
//
//            MultiTouchListener.TransformInfo info = new MultiTouchListener.TransformInfo();
//            info.deltaScale = detector.getScaleFactor();
//            info.deltaAngle = Vector2D.getAngle(mPrevSpanVector, detector.getCurrentSpanVector());
//            info.deltaX = detector.getFocusX() - mPivotX;
//            info.deltaY = detector.getFocusY() - mPivotY;
//            info.pivotX = mPivotX;
//            info.pivotY = mPivotY;
//            info.minimumScale = minimumScale;
//            info.maximumScale = maximumScale;
//            move(view, info);
//            return !mIsTextPinchZoomable;
//        }
//    }
//
//    private static float adjustAngle(float degrees) {
//        if (degrees > 180.0f) {
//            degrees -= 360.0f;
//        } else if (degrees < -180.0f) {
//            degrees += 360.0f;
//        }
//
//        return degrees;
//    }
//
//    private static void move(View view, MultiTouchListener.TransformInfo info) {
//        computeRenderOffset(view, info.pivotX, info.pivotY);
//        adjustTranslation(view, info.deltaX, info.deltaY);
//
//        float scale = view.getScaleX() * info.deltaScale;
//        scale = Math.max(info.minimumScale, Math.min(info.maximumScale, scale));
//        view.setScaleX(scale);
//        view.setScaleY(scale);
//
//        float rotation = adjustAngle(view.getRotation() + info.deltaAngle);
//        view.setRotation(rotation);
//    }
//
//    private static void adjustTranslation(View view, float deltaX, float deltaY) {
//        float[] deltaVector = {deltaX, deltaY};
//        view.getMatrix().mapVectors(deltaVector);
//        view.setTranslationX(view.getTranslationX() + deltaVector[0]);
//        view.setTranslationY(view.getTranslationY() + deltaVector[1]);
//    }
//
//    private class TransformInfo {
//        float deltaX;
//        float deltaY;
//        float deltaScale;
//        float deltaAngle;
//        float pivotX;
//        float pivotY;
//        float minimumScale;
//        float maximumScale;
//    }
//
//    private static class State {
//        @Nullable
//        StickerView mActiveSticker;
//        PointF mLastTouchForMove;
//        boolean mIsOnlyMove;
//
//        @NonNull
//        SparseArray<PointF> mCoordinates = new SparseArray<>(5);
//
//        void reset() {
//            mActiveSticker = null;
//            mIsOnlyMove = false;
//            mCoordinates.clear();
//        }
//    }
//
//    private static class ZoomState {
//        final float mSpanSlop;
//        final float mMinSpan;
//
//        boolean mInProgress;
//        float mInitialSpan;
//        float mPrevSpan;
//        float mCurSpan;
//
//        ZoomState(float spanSlop, float minSpan) {
//            mSpanSlop = spanSlop;
//            mMinSpan = minSpan;
//        }
//
//        void reset() {
//            mInProgress = false;
//            mInitialSpan = 0;
//        }
//    }
//
//    private static class RotateState {
//        int mFirstPointerId = -1;
//        int mSecondPointerId = -1;
//
//        boolean mInProgress;
//
//        PointF mCurrentFocus;
//
//        void reset() {
//            mInProgress = false;
//            mFirstPointerId = -1;
//            mSecondPointerId = -1;
//        }
//    }
//
//    interface ViewFinder {
//        @Nullable
//        View findViewUnderTouch(float touchX, float touchY);
//        @Nullable
//        StickerView findAppropriateSticker(float x1, float y1, float x2, float y2);
//    }


}
