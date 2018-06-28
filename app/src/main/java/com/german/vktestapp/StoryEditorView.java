package com.german.vktestapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

public class StoryEditorView extends ViewGroup {
    public StoryEditorView(Context context) {
        super(context);
    }

    public StoryEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StoryEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StoryEditorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setBackground(@NonNull Uri uri) {

    }

    public void addSticker(@NonNull Bitmap bitmap) {
        ImageView stickerView = new ImageView(getContext());
        stickerView.setImageBitmap(bitmap);

        addView(stickerView);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
