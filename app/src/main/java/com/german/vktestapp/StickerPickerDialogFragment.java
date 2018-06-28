package com.german.vktestapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

public class StickerPickerDialogFragment extends BottomSheetDialogFragment
        implements StickerPickerView {
    private StickerPickerPresenter mStickerPickerPresenter;
    private StickersAdapter mStickersAdapter;
    private StickerPickListener mStickerPickListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof StickerPickListener) {
            mStickerPickerPresenter = (StickerPickerPresenter) context;
        } else {
            throw new ClassCastException("Fragment should be attached to StickerPickerPresenter");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new BottomSheetDialog(getContext(), R.style.StickerPickerDialog);

        View stickerPickerView = View.inflate(getContext(), R.layout.sticker_picker, null);
        RecyclerView stickersListView = stickerPickerView.findViewById(R.id.stickers_list);
        stickersListView.setLayoutManager(new GridLayoutManager(getContext(), 4));

        mStickersAdapter = new StickersAdapter(new StickerPickListenerWrapper(mStickerPickListener));
        stickersListView.setAdapter(mStickersAdapter);

        return dialog;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStickerPickerPresenter = new StickerPickerPresenterImpl();
        mStickerPickerPresenter.attachView(this);

        mStickerPickerPresenter.loadStickers(getContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

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
}
