package com.german.vktestapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.LinkedList;

public class ViewOrderController {
    private LinkedList<View> mOrder = new LinkedList<>();

    public boolean moveToBottom(@NonNull View view) {
        if (!mOrder.isEmpty() && mOrder.getFirst() == view) {
            return false;
        }
        mOrder.remove(view);
        mOrder.addFirst(view);
        return true;
    }

    public boolean moveToTop(@NonNull View view) {
        if (!mOrder.isEmpty() && mOrder.getLast() == view) {
            return false;
        }
        mOrder.remove(view);
        mOrder.addLast(view);
        return true;
    }

    public void removeView(@NonNull View view) {
        mOrder.remove(view);
    }

    @Nullable
    public View getViewByOrder(int order) {
        return order < mOrder.size()
                ? mOrder.get(order)
                : null;
    }
}
