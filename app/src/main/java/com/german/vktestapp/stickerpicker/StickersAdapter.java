package com.german.vktestapp.stickerpicker;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

public class StickersAdapter extends RecyclerView.Adapter<StickersAdapter.StickerViewHolder> {
    @NonNull
    private final StickerPickListener mStickerPickListener;

    @Nullable
    private List<StickerProvider> mStickerProviders;

    public StickersAdapter(@NonNull StickerPickListener stickerPickListener) {
        mStickerPickListener = stickerPickListener;
    }

    public void setStickersProviders(@NonNull List<StickerProvider> stickersProvider) {
        mStickerProviders = stickersProvider;
    }

    @Override
    public StickerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView view = new ImageView(parent.getContext());
        view.setAdjustViewBounds(true);
        return new StickerViewHolder(view, mStickerPickListener);
    }

    @Override
    public void onBindViewHolder(StickerViewHolder holder, int position) {
        if (mStickerProviders == null || position >= mStickerProviders.size()) {
            return;
        }

        holder.setSticker(mStickerProviders.get(position));
    }

    @Override
    public int getItemCount() {
        return mStickerProviders != null
                ? mStickerProviders.size()
                : 0;
    }

    static class StickerViewHolder extends RecyclerView.ViewHolder {
        public StickerViewHolder(@NonNull ImageView stickerView,
                                 @NonNull StickerPickListener stickerPickListener) {
            super(stickerView);
            stickerView.setOnClickListener(view -> {
                if (!(view instanceof ImageView)) {
                    return;
                }
                ImageView imageView = (ImageView) view;
                Drawable drawable = imageView.getDrawable();
                if (!(drawable instanceof BitmapDrawable)) {
                    return;
                }
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap != null) {
                    stickerPickListener.onStickerPicked(bitmap);
                }
            });

        }

        void setSticker(@NonNull StickerProvider stickerProvider) {
            Bitmap bm;
            try {
                bm = stickerProvider.getSticker();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            ((ImageView) itemView).setImageBitmap(bm);
        }
    }
}
