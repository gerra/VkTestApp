package com.german.vktestapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.german.vktestapp.backgrounds.Background;
import com.german.vktestapp.backgrounds.BackgroundsAdapter;
import com.german.vktestapp.backgrounds.BackgroundsHelper;
import com.german.vktestapp.stickerpicker.StickerPickerDialogFragment;
import com.german.vktestapp.utils.PermissionsUtils;
import com.german.vktestapp.utils.Utils;

// TODO: save selected item
// TODO: only portrait
public class MainActivity extends AppCompatActivity implements
        StoryView, StickerPickListener, BackgroundPickListener, AddBackgroundClickListener {
    private static final String TAG = "[MainActivity]";

    private static final String KEY_SELECTED_POSITION = "selectedPosition";

    private static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int REQUEST_CODE_READ_PERMISSION = 50;

    private static final int REQUEST_CODE_SELECT_PHOTO = 100;

    private StoryEditorView mStoryEditorView;

    private BackgroundsAdapter mBackgroundsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStoryEditorView = findViewById(R.id.story_editor_view);

        setActionBar();
        int selectedPosition = savedInstanceState != null
                ? savedInstanceState.getInt(KEY_SELECTED_POSITION, BackgroundsAdapter.UNKNOWN_POSITION)
                : 0;
        setBackgroundsPanel(selectedPosition);
    }

    private void setActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        View actionBarView = getSupportActionBar().getCustomView();
        ((Toolbar) actionBarView.getParent()).setContentInsetsAbsolute(0, 0);
        actionBarView.findViewById(R.id.show_sticker_picker)
                .setOnClickListener(v -> new StickerPickerDialogFragment().show(getSupportFragmentManager(), null));
        actionBarView.findViewById(R.id.change_text_style)
                .setOnClickListener(v -> mStoryEditorView.changeTextStyle());
    }

    private void setBackgroundsPanel(int selectedPosition) {
        mBackgroundsAdapter = new BackgroundsAdapter(BackgroundsHelper.getDefaultBackgrounds(), this, this);

        RecyclerView backgroundsListView = findViewById(R.id.backgrounds_list);
        backgroundsListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        backgroundsListView.setAdapter(mBackgroundsAdapter);
        backgroundsListView.addItemDecoration(new BackgroundsItemDecoration(this));
        mBackgroundsAdapter.setSelectedPosition(selectedPosition);
    }

    @Override
    public void onStickerPicked(@NonNull Bitmap sticker) {
        mStoryEditorView.addSticker(sticker);
    }

    @Override
    public void onBackgroundPicked(@NonNull Background background) {
        mStoryEditorView.setBackground(background.getFull(this));
    }

    @Override
    public void onAddBackgroundClick() {
        if (!PermissionsUtils.checkPermission(this, PERMISSION_READ_STORAGE)) {
            if (PermissionsUtils.isRuntimePermissionsAvailable(this)) {
                ActivityCompat.requestPermissions(this,
                                                  new String[] { PERMISSION_READ_STORAGE },
                                                  REQUEST_CODE_READ_PERMISSION);
            } else {
                throw new IllegalStateException("There is no permission for read storage in manifest?");
            }
        } else {
            startImagePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_PERMISSION: {
                if (permissions.length > 0
                        && PERMISSION_READ_STORAGE.equals(permissions[0])
                        && grantResults.length > 0
                        && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    startImagePicker();
                }
                break;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SELECT_PHOTO:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    onPhotoSelected(data.getData());
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_POSITION, mBackgroundsAdapter.getSelectedPosition());
    }

    private void onPhotoSelected(@NonNull Uri selectedUri) {
        String[] filePath = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver()
                .query(selectedUri, filePath, null, null, null);
        if (cursor == null) {
            return;
        }
        String imagePath;
        try {
            cursor.moveToFirst();
            imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
        } finally {
            Utils.close(cursor);
        }

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        mStoryEditorView.setBackground(new BitmapDrawable(getResources(), bitmap));
        mBackgroundsAdapter.setSelectedPosition(BackgroundsAdapter.UNKNOWN_POSITION);
    }

    private void startImagePicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_SELECT_PHOTO);
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
