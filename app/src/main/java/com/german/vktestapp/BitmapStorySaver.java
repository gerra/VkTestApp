package com.german.vktestapp;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapStorySaver implements StorySaver<BitmapStory> {
    @NonNull
    private final File mStoriesFolder;

    public BitmapStorySaver(@NonNull File storiesFolder) {
        mStoriesFolder = storiesFolder;
    }

    @NonNull
    @Override
    public StorySaveResult save(@NonNull BitmapStory story) {
        if (!createDirectory(mStoriesFolder)) {
            return StorySaveResult.FAIL;
        }

        File file = new File(mStoriesFolder, story.getName() + ".png");
        try (OutputStream os = new FileOutputStream(file)) {
            Bitmap bitmap = story.getBitmap();
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

        return StorySaveResult.SUCCESS;
    }

    private boolean createDirectory(@NonNull File file) {
        return file.exists() || file.mkdirs();
    }
}
