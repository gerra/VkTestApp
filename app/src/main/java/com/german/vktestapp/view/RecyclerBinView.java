package com.german.vktestapp.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.german.vktestapp.BackgroundSetListener;
import com.german.vktestapp.R;
import com.german.vktestapp.backgrounds.Background;

public class RecyclerBinView extends FrameLayout implements BackgroundSetListener {
    private static final int STATE_NOT_SHOWN = 0;
    private static final int STATE_SHOWN_DEACTIVATED = 1;
    private static final int STATE_SHOWN_ACTIVATED = 2;

    private int mDeactivatedSize;
    private int mActivatedSize;

    private RecyclerBinImageView mIconView;

    private int mState = STATE_NOT_SHOWN;

    public RecyclerBinView(Context context) {
        this(context, null);
    }

    public RecyclerBinView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerBinView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        Resources resources = context.getResources();

        mDeactivatedSize = resources.getDimensionPixelSize(R.dimen.recycle_bin_size_deactivated);
        mActivatedSize = resources.getDimensionPixelSize(R.dimen.recycle_bin_size_activated);

        int iconWidth = context.getResources()
                .getDimensionPixelSize(R.dimen.recycle_bin_icon_width);
        mIconView = new RecyclerBinImageView(context);
        mIconView.setLayoutParams(new LayoutParams(0, 0, Gravity.CENTER));
        mIconView.setWidth(iconWidth);

        addView(mIconView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Whatever what they want...
        int actualWidthMeasureSpec;
        int actualHeightMeasureSpec;

        if (mState == STATE_SHOWN_DEACTIVATED) {
            actualWidthMeasureSpec = actualHeightMeasureSpec
                    = MeasureSpec.makeMeasureSpec(mDeactivatedSize, MeasureSpec.EXACTLY);
        } else if (mState == STATE_SHOWN_ACTIVATED) {
            actualWidthMeasureSpec = actualHeightMeasureSpec
                    = MeasureSpec.makeMeasureSpec(mActivatedSize, MeasureSpec.EXACTLY);
        } else {
            actualWidthMeasureSpec = widthMeasureSpec;
            actualHeightMeasureSpec = heightMeasureSpec;
        }

        super.onMeasure(actualWidthMeasureSpec, actualHeightMeasureSpec);
    }

    public void show() {
        if (mState == STATE_NOT_SHOWN) {
            setImageResource(R.drawable.ic_fab_trash);
            setVisibility(VISIBLE);
            mState = STATE_SHOWN_DEACTIVATED;
        }
    }

    public void hide() {
        setVisibility(GONE);
        mState = STATE_NOT_SHOWN;
    }

    public void activate() {
        setVisibility(VISIBLE);
        if (mState != STATE_SHOWN_ACTIVATED) {
            setImageResource(R.drawable.ic_fab_trash_released);
            if (mState == STATE_SHOWN_DEACTIVATED) {
                requestLayout();
            }
            mState = STATE_SHOWN_ACTIVATED;
        }
    }

    public void deactivate() {
        setVisibility(VISIBLE);
        if (mState != STATE_SHOWN_DEACTIVATED) {
            setImageResource(R.drawable.ic_fab_trash);
            if (mState == STATE_SHOWN_ACTIVATED) {
                requestLayout();
            }
            mState = STATE_SHOWN_DEACTIVATED;
        }
    }

    private void setImageResource(@DrawableRes int drawableRes) {
        mIconView.setImageResource(drawableRes);
    }

    @Override
    public void onBackgroundSet(@NonNull Background background) {
        if (!background.isEmpty()) {
            setBackgroundResource(R.drawable.recycle_bin_background);
        } else {
            setBackgroundResource(R.drawable.recycle_bin_background_behind_null);
        }
    }
}
