package com.german.vktestapp.backgrounds;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.german.vktestapp.AddBackgroundClickListener;
import com.german.vktestapp.BackgroundPickListener;
import com.german.vktestapp.R;
import com.german.vktestapp.view.RoundCornersDrawable;

import java.util.List;

public class BackgroundsAdapter extends RecyclerView.Adapter<BackgroundsAdapter.BackgroundViewHolder> {
    public static final int UNKNOWN_POSITION = -1;

    private static final int TYPE_THUMB = 1;
    private static final int TYPE_ADD = 2;

    @NonNull
    private final List<BackgroundProvider> mBackgroundProviders;
    @NonNull
    private final BackgroundPickListener mBackgroundPickListener;
    @NonNull
    private final AddBackgroundClickListener mAddBackgroundClickListener;

    private int mSelectedPosition = UNKNOWN_POSITION;

    public BackgroundsAdapter(@NonNull List<BackgroundProvider> backgroundProviders,
                              @NonNull BackgroundPickListener backgroundPickListener,
                              @NonNull AddBackgroundClickListener addBackgroundClickListener) {
        mBackgroundProviders = backgroundProviders;
        mBackgroundPickListener = backgroundPickListener;
        mAddBackgroundClickListener = addBackgroundClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mBackgroundProviders.size()) {
            return TYPE_ADD;
        } else {
            return TYPE_THUMB;
        }
    }

    @NonNull
    @Override
    public BackgroundViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        int size = parent.getResources()
                .getDimensionPixelSize(R.dimen.background_preview_size);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        return viewType == TYPE_THUMB
                ? new ThumbViewHolder(imageView)
                : new AddViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(BackgroundViewHolder holder, int position) {
        if (holder instanceof ThumbViewHolder) {
            Background background = mBackgroundProviders.get(position)
                    .getBackground();
            BackgroundPickListener backgroundPickListener = bg -> setSelectedPosition(position, bg);
            ((ThumbViewHolder) holder).bind(background,
                                            backgroundPickListener,
                                            mSelectedPosition == position);
        } else if (holder instanceof AddViewHolder) {
            ((AddViewHolder) holder).bind(mAddBackgroundClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return mBackgroundProviders.size() + 1;
    }

    public void setSelectedPosition(int selectedPosition) {
        if (selectedPosition != mSelectedPosition) {
            Background background = selectedPosition != UNKNOWN_POSITION
                    ? mBackgroundProviders.get(selectedPosition)
                        .getBackground()
                    : null;

            setSelectedPosition(selectedPosition, background);

        }
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    private void setSelectedPosition(int selectedPosition, @Nullable Background background) {
        if (selectedPosition != mSelectedPosition) {
            int oldSelected = mSelectedPosition;
            mSelectedPosition = selectedPosition;
            if (oldSelected != UNKNOWN_POSITION) {
                notifyItemChanged(oldSelected);
            }
            if (mSelectedPosition != UNKNOWN_POSITION) {
                notifyItemChanged(mSelectedPosition);
            }
            if (background != null) {
                mBackgroundPickListener.onBackgroundPicked(background);
            }
        }
    }

    abstract static class BackgroundViewHolder extends RecyclerView.ViewHolder {
        public BackgroundViewHolder(ImageView itemView) {
            super(itemView);
        }

        protected void bind(@NonNull Drawable drawable) {
            float cornerRadius = itemView.getResources()
                    .getDimension(R.dimen.background_preview_corner_radius);
            Drawable roundedDrawable = new RoundCornersDrawable(drawable, cornerRadius);
            ((ImageView) itemView).setImageDrawable(roundedDrawable);
        }
    }

    private static class ThumbViewHolder extends BackgroundViewHolder {
        public ThumbViewHolder(@NonNull ImageView itemView) {
            super(itemView);
        }

        public void bind(@NonNull Background background,
                         @NonNull BackgroundPickListener backgroundPickListener,
                         boolean isActive) {
            Drawable thumbDrawable = background.getThumb(itemView.getContext());

            Drawable actualDrawable;
            if (isActive) {
                Drawable[] drawables = new Drawable[2];
                drawables[0] = thumbDrawable;
                drawables[1] = itemView.getContext().getResources()
                        .getDrawable(R.drawable.active_background_border_2);
                actualDrawable = new LayerDrawable(drawables);
            } else {
                actualDrawable = thumbDrawable;
            }

            super.bind(actualDrawable);

            itemView.setOnClickListener(v -> backgroundPickListener.onBackgroundPicked(background));
        }
    }

    private static class AddViewHolder extends BackgroundViewHolder {
        public AddViewHolder(@NonNull ImageView itemView) {
            super(itemView);
        }

        void bind(@NonNull AddBackgroundClickListener addBackgroundClickListener) {
            Drawable addDrawable = itemView.getResources()
                    .getDrawable(R.drawable.add_backround_drawable);
            super.bind(addDrawable);

            itemView.setOnClickListener(v -> addBackgroundClickListener.onAddBackgroundClick());
        }
    }
}
