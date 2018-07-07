package com.german.vktestapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.german.vktestapp.backgrounds.AddBackgroundClickListener;
import com.german.vktestapp.backgrounds.Background;
import com.german.vktestapp.backgrounds.BackgroundPickListener;
import com.german.vktestapp.backgrounds.BackgroundsAdapter;
import com.german.vktestapp.backgrounds.BackgroundsHelper;
import com.german.vktestapp.backgrounds.SimpleBackground;
import com.german.vktestapp.editor.InteractStickerListener;
import com.german.vktestapp.editor.StickerView;
import com.german.vktestapp.editor.StoryEditorView;
import com.german.vktestapp.stickerpicker.StickerPickListener;
import com.german.vktestapp.stickerpicker.StickerPickerDialogFragment;
import com.german.vktestapp.textstyling.Style;
import com.german.vktestapp.textstyling.StyleableProvider;
import com.german.vktestapp.utils.PermissionsUtils;
import com.german.vktestapp.utils.Utils;
import com.german.vktestapp.utils.ViewUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements
        StoryView, StickerPickListener, BackgroundPickListener, AddBackgroundClickListener {
    private static final String TAG = "[MainActivity]";

    private static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private static final int REQUEST_CODE_READ_PERMISSION = 50;
    private static final int REQUEST_CODE_WRITE_PERMISSION = 51;

    private static final int REQUEST_CODE_SELECT_PHOTO = 100;

    private StoryPresenter<BitmapStory> mStoryPresenter;

    private StoryEditorView mStoryEditorView;
    private InteractStickerListenerImpl mInteractStickerListener;
    private TextOnBackgroundStyleController mTextStyleController;

    private BackgroundsAdapter mBackgroundsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStoryEditorView = findViewById(R.id.story_editor_view);
        mStoryEditorView.setActivateRecycleBinEffect(new VibrateEffect());

        initActionBar();
        initTextStyleController(mStoryEditorView);
        initBackgroundsPanel(0);
        initStoryPresenter();

        View saveButton = findViewById(R.id.save_story);
        saveButton.setOnClickListener(v -> {
            PermissionsUtils.checkAndRequestPermission(this,
                                                       PERMISSION_WRITE_STORAGE,
                                                       REQUEST_CODE_WRITE_PERMISSION,
                                                       this::saveStory);
        });
        mInteractStickerListener = new InteractStickerListenerImpl(saveButton);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStoryPresenter.attachView(this);
        mStoryEditorView.addInteractStickerListener(mInteractStickerListener);
        mStoryEditorView.addBackgroundSetListener(mTextStyleController);
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        View actionBarView = getSupportActionBar().getCustomView();
        ((Toolbar) actionBarView.getParent()).setContentInsetsAbsolute(0, 0);
        actionBarView.findViewById(R.id.show_sticker_picker)
                .setOnClickListener(v -> new StickerPickerDialogFragment().show(getSupportFragmentManager(), null));
        actionBarView.findViewById(R.id.change_text_style)
                .setOnClickListener(v -> mTextStyleController.toggle());
    }

    private void initTextStyleController(@NonNull StyleableProvider styleableProvider) {
        Resources resources = getResources();

        int shadowColor = resources.getColor(R.color.text_style_shadow);
        float shadowRadius = resources.getDimension(R.dimen.text_style_shadow_radius);
        float shadowDy = resources.getDimension(R.dimen.text_style_shadow_dy);

        int secondBackgroundColor = resources.getColor(R.color.text_style_2_background_color);
        int thirdBackgroundColor = resources.getColor(R.color.text_style_3_background_color);

        int styledTextColor = resources.getColor(R.color.text_style_text_color);

        Style.Shadow shadow = new Style.Shadow(shadowColor, shadowDy, shadowRadius);

        Style firstStyle = new Style(new Style.TextStyle(styledTextColor, shadow),
                                     new Style.BackgroundStyle(null, null));
        Style secondStyle = new Style(new Style.TextStyle(null, null),
                                      new Style.BackgroundStyle(secondBackgroundColor, shadow));
        Style thirdStyle = new Style(new Style.TextStyle(styledTextColor, shadow),
                                     new Style.BackgroundStyle(thirdBackgroundColor, shadow));

        List<Style> styles = Arrays.asList(firstStyle, secondStyle, thirdStyle);
        mTextStyleController = new TextOnBackgroundStyleController(styleableProvider, styles);
    }

    private void initBackgroundsPanel(int selectedPosition) {
        mBackgroundsAdapter = new BackgroundsAdapter(BackgroundsHelper.getDefaultBackgrounds(), this, this);

        RecyclerView backgroundsListView = findViewById(R.id.backgrounds_list);
        backgroundsListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        backgroundsListView.setAdapter(mBackgroundsAdapter);
        backgroundsListView.addItemDecoration(new BackgroundsItemDecoration(this));
        mBackgroundsAdapter.setSelectedPosition(selectedPosition);
    }

    private void initStoryPresenter() {
        BitmapStorySaver.FileSaveListener fileSaveListener;
        File storiesDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storiesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "VkTestApp/Stories");
            fileSaveListener = new UpdateGalleryNotifier(this);
        } else {
            File parent = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (parent != null) {
                storiesDir = new File(parent, "Stories");
            } else {
                storiesDir = new File(getFilesDir(), "VkTestApp/Stories");
            }
            fileSaveListener = null;
        }

        mStoryPresenter = new StoryPresenterImpl<>(new BitmapStorySaver(storiesDir, fileSaveListener), new Handler());
    }

    @Override
    public void onStickerPicked(@NonNull Bitmap sticker) {
        mStoryEditorView.addSticker(sticker);
    }

    @Override
    public void onBackgroundPicked(@NonNull Background background) {
        mStoryEditorView.setBackground(background);
    }

    @Override
    public void onAddBackgroundClick() {
        PermissionsUtils.checkAndRequestPermission(this,
                                                   PERMISSION_READ_STORAGE,
                                                   REQUEST_CODE_READ_PERMISSION,
                                                   this::startImagePicker);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_PERMISSION: {
                if (grantResults.length > 0 && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    startImagePicker();
                }
                break;
            }
            case REQUEST_CODE_WRITE_PERMISSION: {
                if (grantResults.length > 0 && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    saveStory();
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
    protected void onStop() {
        mInteractStickerListener.reset();

        mStoryPresenter.detachView();
        mStoryEditorView.removeInteractStickerListener(mInteractStickerListener);
        mStoryEditorView.removeBackgroundSetListener(mTextStyleController);
        super.onStop();
    }

    @Override
    public void showProgress() {

    }

    @Override
    public void onSaveComplete() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        Toast.makeText(this, R.string.story_save_complete, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveError() {
        Toast.makeText(this, R.string.story_save_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideProgress() {
    }

    // TODO: wtf, google photos?
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

        Background background = null;
        if (imagePath != null) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            Bitmap notResizedBitmap = BitmapFactory.decodeFile(imagePath, o);
            if (notResizedBitmap != null) {
                Bitmap actualBitmap;
                int width = notResizedBitmap.getWidth();
                int height = notResizedBitmap.getHeight();
                Point screenSize = ViewUtils.getScreenSize(this);
                if (width > screenSize.x || height > screenSize.y) {
                    float scale = Math.min(1f * screenSize.x / width, 1f * screenSize.y / height);
                    Matrix matrix = new Matrix();
                    matrix.postScale(scale, scale);
                    actualBitmap = Bitmap.createBitmap(notResizedBitmap,
                                                       0, 0,
                                                       width, height,
                                                       matrix,
                                                       false);
                    notResizedBitmap.recycle();
                } else {
                    actualBitmap = notResizedBitmap;
                }
                background = new SimpleBackground(new BitmapDrawable(getResources(), actualBitmap));
            }
        }
        if (background == null) {
            background = Background.EMPTY;
        }
        mStoryEditorView.setBackground(background);
        // onBackgroundPicked will be called
        mBackgroundsAdapter.setSelectedPosition(!background.isEmpty()
                                                        ? BackgroundsAdapter.UNKNOWN_POSITION
                                                        : 0);
    }

    private void startImagePicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_SELECT_PHOTO);
    }

    private void saveStory() {
        String name = UUID.randomUUID()
                .toString()
                .replaceAll("-", "")
                .substring(0, 10)
                .toUpperCase();
        Bitmap bitmap = mStoryEditorView.getBitmap();
        mStoryPresenter.save(new BitmapStory(name) {
            @Nullable
            @Override
            public Bitmap getBitmap() {
                return bitmap;
            }
        });
    }

    private static class InteractStickerListenerImpl implements InteractStickerListener {
        @NonNull
        private final View mSaveButton;
        @NonNull
        private final Set<StickerView> mActiveStickers = new HashSet<>();

        public InteractStickerListenerImpl(@NonNull View saveButton) {
            mSaveButton = saveButton;
        }

        @Override
        public void onStartInteract(@NonNull StickerView stickerView) {
            mActiveStickers.add(stickerView);
            mSaveButton.setEnabled(false);
        }

        @Override
        public void onStopInteract(@NonNull StickerView stickerView) {
            mActiveStickers.remove(stickerView);
            if (mActiveStickers.isEmpty()) {
                mSaveButton.setEnabled(true);
            }
        }

        // Because of "Cancelling event due to no window focus:" when sticker is interacting
        // and ACTION_CANCEL appears
        void reset() {
            mActiveStickers.clear();
            mSaveButton.setEnabled(true);
        }
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

    private static class UpdateGalleryNotifier implements BitmapStorySaver.FileSaveListener {
        @NonNull
        private final Context mAppContext;

        UpdateGalleryNotifier(@NonNull Context context) {
            mAppContext = context.getApplicationContext();
        }

        @Override
        public void onSave(@NonNull File file) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            mAppContext.sendBroadcast(intent);
        }
    }
}
