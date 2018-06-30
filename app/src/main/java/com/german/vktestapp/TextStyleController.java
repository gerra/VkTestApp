package com.german.vktestapp;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.widget.EditText;

public class TextStyleController {
    private int[] COLORS = new int[] {

            Color.parseColor("#ff0000"),
            Color.parseColor("#0000ff")
    };

    private EditText mEditText;
    private int mCurrentColor = Color.parseColor("#00000000");
    private BackgroundColorSpan mSpan;

    public TextStyleController(@NonNull EditText editText) {
        mEditText = editText;
//        mSpan = new BackgroundColorSpan(mCurrentColor);
//
    }

    void toggle() {
//        mCurrentColor = mCurrentColor != COLORS[0] ? COLORS[0] : COLORS[1];
//        mSpan = new MySpan();
//        setSpan(mEditText.getText());
    }

    void setSpan(Editable s) {
        s.setSpan(mSpan,
                  0,
                  s.length(),
                  Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }
}
