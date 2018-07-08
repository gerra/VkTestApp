package com.german.vktestapp;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapStorySaver implements StorySaver<BitmapStory> {
    @NonNull
    private final File mStoriesFolder;
    @Nullable
    private final FileSaveListener mFileSaveListener;

    public BitmapStorySaver(@NonNull File storiesFolder, @Nullable FileSaveListener fileSaveListener) {
        mStoriesFolder = storiesFolder;
        mFileSaveListener = fileSaveListener;
    }

    @NonNull
    @Override
    public StorySaveResult save(@NonNull BitmapStory story) {
        Bitmap bitmap = story.getBitmap();

        try {
            if (!createDirectory(mStoriesFolder)) {
                return StorySaveResult.FAIL;
            }

            File file = new File(mStoriesFolder, story.getName() + ".png");
            try (OutputStream os = new FileOutputStream(file)) {
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.flush();
                } else {
                    file.delete();
                }
            } catch (IOException e) {
                file.delete();
                return StorySaveResult.FAIL;
            }

            if (mFileSaveListener != null) {
                mFileSaveListener.onSave(file);
            }

            return StorySaveResult.SUCCESS;
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    private boolean createDirectory(@NonNull File file) {
        return file.exists() || file.mkdirs();
    }

    // Used for ACTION_MEDIA_SCANNER_SCAN_FILE e.g.
    public interface FileSaveListener {
        void onSave(@NonNull File file);
    }
}
