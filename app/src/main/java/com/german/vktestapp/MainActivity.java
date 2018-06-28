package com.german.vktestapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements
        StoryView, StickerPickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActionBar();
    }

    private void setActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        View actionBarView = getSupportActionBar().getCustomView();
        actionBarView.findViewById(R.id.show_sticker_picker)
                .setOnClickListener(v -> new StickerPickerDialogFragment().show(getSupportFragmentManager(), null));
    }

    @Override
    public void onStickerPicked(@NonNull Bitmap sticker) {

    }
}
