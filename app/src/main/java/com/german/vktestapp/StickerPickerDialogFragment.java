package com.german.vktestapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class StickerPickerDialogFragment extends BottomSheetDialogFragment
        implements StickerPickerView {
    private static final int COLUMNS_IN_STICKERS_LIST_COUNT = 4;

    private StickerPickerPresenter mStickerPickerPresenter;
    private StickersAdapter mStickersAdapter;
    private StickerPickListener mStickerPickListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof StickerPickListener) {
            mStickerPickListener = (StickerPickListener) context;
        } else {
            throw new ClassCastException("Fragment should be attached to StickerPickerPresenter");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View stickerPickerView = View.inflate(getContext(), R.layout.sticker_picker, null);

        mStickersAdapter = new StickersAdapter(new StickerPickListenerWrapper(mStickerPickListener));

        RecyclerView stickersListView = stickerPickerView.findViewById(R.id.stickers_list);
        stickersListView.setAdapter(mStickersAdapter);
        stickersListView.setLayoutManager(new GridLayoutManager(getContext(), COLUMNS_IN_STICKERS_LIST_COUNT));
        int horizontalSpace = getResources().getDimensionPixelSize(R.dimen.horizontal_space_stickers_list);
        int verticalSpace = getResources().getDimensionPixelSize(R.dimen.vertical_space_stickers_list);
        StickersSpaceDecorator stickersSpaceDecorator = new StickersSpaceDecorator(COLUMNS_IN_STICKERS_LIST_COUNT, horizontalSpace, verticalSpace);
        stickersListView.addItemDecoration(stickersSpaceDecorator);

        Toolbar toolbar = stickerPickerView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.stickers_title);

        return stickerPickerView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mStickerPickerPresenter = new StickerPickerPresenterImpl();
        mStickerPickerPresenter.attachView(this);
        mStickerPickerPresenter.loadStickers(getContext());
    }

    @Override
    public void onStop() {
        super.onStop();

        mStickerPickerPresenter.detachView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mStickerPickListener = null;
    }

    @Override
    public void showStickers(@NonNull List<StickerProvider> stickerProviderList) {
        mStickersAdapter.setStickersProviders(stickerProviderList);
        mStickersAdapter.notifyDataSetChanged();
    }

    private class StickerPickListenerWrapper implements StickerPickListener {
        @Nullable
        private final StickerPickListener mOriginal;

        StickerPickListenerWrapper(@Nullable StickerPickListener original) {
            mOriginal = original;
        }

        @Override
        public void onStickerPicked(@NonNull Bitmap sticker) {
            if (mOriginal != null) {
                mOriginal.onStickerPicked(sticker);
            }

            dismiss();
        }
    }

    private static class StickersSpaceDecorator extends RecyclerView.ItemDecoration {
        private final int mColumnsCount;
        private final int mHorizontalSpace;
        private final int mVerticalSpace;

        public StickersSpaceDecorator(int columnsCount, int horizontalSpace, int verticalSpace) {
            mColumnsCount = columnsCount;
            mHorizontalSpace = horizontalSpace;
            mVerticalSpace = verticalSpace;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % mColumnsCount;

            if (column < mColumnsCount - 1) {
                outRect.right += mHorizontalSpace;
            }

            if (position >= mColumnsCount) {
                outRect.top += mVerticalSpace;
            }
        }
    }
}
