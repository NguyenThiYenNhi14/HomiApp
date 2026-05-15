package com.yn.homi.model;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.yn.homi.R;

public class PhotoPickerBottomSheet extends DialogFragment {

    public interface OnPhotoOptionListener {
        void onOpenCamera();
        void onSelectGallery();
    }

    private OnPhotoOptionListener listener;

    public void setListener(OnPhotoOptionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.bottom_sheet_photo_picker, null);

        TextView tvCamera  = view.findViewById(R.id.tvOpenCamera);
        TextView tvGallery = view.findViewById(R.id.tvSelectGallery);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        // Bo góc dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.drawable.dialog_holo_light_frame);
        }

        tvCamera.setOnClickListener(v -> {
            if (listener != null) listener.onOpenCamera();
            dismiss();
        });

        tvGallery.setOnClickListener(v -> {
            if (listener != null) listener.onSelectGallery();
            dismiss();
        });

        return dialog;
    }
}