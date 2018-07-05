package com.german.vktestapp.textstyling;

import android.support.annotation.Nullable;

public interface SpanStyle {
    SpanStyle EMPTY = () -> null;

    @Nullable
    Object[] getSpans();
}
