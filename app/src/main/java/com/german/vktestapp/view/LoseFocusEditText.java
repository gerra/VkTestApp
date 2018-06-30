package com.german.vktestapp.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class LoseFocusEditText extends AppCompatEditText {
    public LoseFocusEditText(Context context) {
        super(context);
    }

    public LoseFocusEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoseFocusEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            clearFocus();
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
