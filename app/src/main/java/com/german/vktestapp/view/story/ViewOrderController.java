package com.german.vktestapp.view.story;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.LinkedList;

public class ViewOrderController {
    @NonNull
    private final LinkedList<View> mOrdinaryViews = new LinkedList<>();
    @NonNull
    private final LinkedList<View> mHighPriorityViews = new LinkedList<>();

    public boolean moveToBottom(@NonNull View view) {
        if (!mOrdinaryViews.isEmpty() && mOrdinaryViews.getFirst() == view) {
            return false;
        }
        mOrdinaryViews.remove(view);
        mOrdinaryViews.addFirst(view);
        return true;
    }

    public boolean moveToTop(@NonNull View view) {
        if (!mOrdinaryViews.isEmpty() && mOrdinaryViews.getLast() == view) {
            return false;
        }
        mOrdinaryViews.remove(view);
        mOrdinaryViews.addLast(view);
        return true;
    }

    public boolean moveHighPriorityViewToTop(@NonNull View view) {
        if (!mHighPriorityViews.isEmpty() && mHighPriorityViews.getLast() == view) {
            return false;
        }
        mHighPriorityViews.remove(view);
        mHighPriorityViews.addLast(view);
        return true;
    }

    public boolean removeView(@NonNull View view) {
        return mOrdinaryViews.remove(view) || mHighPriorityViews.remove(view);
    }

    @Nullable
    public View getViewByOrder(int order) {
        int ordinaryViewsSize = mOrdinaryViews.size();
        if (order < ordinaryViewsSize) {
            return mOrdinaryViews.get(order);
        } else if (order - ordinaryViewsSize < mHighPriorityViews.size()) {
            return mHighPriorityViews.get(order - ordinaryViewsSize);
        }
        return null;
    }
}
