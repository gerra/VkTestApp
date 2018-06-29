package com.german.vktestapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.german.vktestapp.backgrounds.Background;
import com.german.vktestapp.backgrounds.BackgroundsAdapter;
import com.german.vktestapp.backgrounds.BackgroundsHelper;
import com.german.vktestapp.stickerpicker.StickerPickerDialogFragment;

public class MainActivity extends AppCompatActivity implements
        StoryView, StickerPickListener, BackgroundPickListener, AddBackgroundClickListener {
    private static final String TAG = "[MainActivity]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActionBar();
        setBackgroundsPanel();
    }

    private void setActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        View actionBarView = getSupportActionBar().getCustomView();
        ((Toolbar) actionBarView.getParent()).setContentInsetsAbsolute(0, 0);
        actionBarView.findViewById(R.id.show_sticker_picker)
                .setOnClickListener(v -> new StickerPickerDialogFragment().show(getSupportFragmentManager(), null));
    }

    private void setBackgroundsPanel() {
        BackgroundsAdapter adapter = new BackgroundsAdapter(BackgroundsHelper.getDefaultBackgrounds(), this, this);

        RecyclerView backgroundsListView = findViewById(R.id.backgrounds_list);
        backgroundsListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        backgroundsListView.setAdapter(adapter);
        backgroundsListView.addItemDecoration(new BackgroundsItemDecoration(this));
        adapter.setSelectedPosition(0);
    }

    @Override
    public void onStickerPicked(@NonNull Bitmap sticker) {
        Log.d(TAG, "onStickerPicked()");
    }

    @Override
    public void onBackgroundPicked(@NonNull Background background) {
        Log.d(TAG, "onBGPicked()");
    }

    @Override
    public void onAddBackgroundClick() {
        Log.d(TAG, "onAddBG()");
    }

    private static class BackgroundsItemDecoration extends RecyclerView.ItemDecoration {
        private final int mSpaceBetweenItems;
        private final int mSidePadding;

        BackgroundsItemDecoration(@NonNull Context context) {
            mSpaceBetweenItems = context.getResources()
                    .getDimensionPixelSize(R.dimen.backgrounds_list_space_between_items);
            mSidePadding = context.getResources()
                    .getDimensionPixelSize(R.dimen.backgrounds_list_side_padding);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int adapterPosition = parent.getChildAdapterPosition(view);
            int totalItemsCount = parent.getAdapter()
                    .getItemCount();
            outRect.left += mSpaceBetweenItems;
            outRect.right += mSpaceBetweenItems;
            if (adapterPosition == 0) {
                outRect.left += mSidePadding;
            } else if (adapterPosition == totalItemsCount - 1) {
                outRect.right += mSidePadding;
            }
        }
    }
}
