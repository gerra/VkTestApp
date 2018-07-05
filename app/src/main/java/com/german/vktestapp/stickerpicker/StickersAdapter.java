package com.german.vktestapp.stickerpicker;

import android.graphics.Bitmap;
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
        @NonNull
        private final StickerPickListener mStickerPickListener;

        public StickerViewHolder(@NonNull ImageView stickerView,
                                 @NonNull StickerPickListener stickerPickListener) {
            super(stickerView);

            mStickerPickListener = stickerPickListener;
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
            itemView.setOnClickListener(v -> mStickerPickListener.onStickerPicked(bm));
        }
    }
}
