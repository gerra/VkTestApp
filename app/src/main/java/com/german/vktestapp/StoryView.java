package com.german.vktestapp;

public interface StoryView {
    void showProgress();
    void onSaveComplete();
    void onSaveError();
    void hideProgress();
}
