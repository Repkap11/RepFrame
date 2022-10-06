package com.repkap11.repframe.image_manager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.repkap11.repframe.R;
import com.repkap11.repframe.main.MainFragment;

import java.io.File;

public class ImagePreviewDialogFragment extends DialogFragment {

    public static final String ARG_IMAGE_PATH = "ARG_IMAGE_PATH";
    private static final String TAG = ImagePreviewDialogFragment.class.getSimpleName();
    private ImageView mImageView;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.Theme_RepFrame_Dialog);
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_image_preview, null, false);
        String path = getArguments().getString(ARG_IMAGE_PATH);
        String name = new File(path).getName();

        TextView textView = rootView.findViewById(R.id.dialog_fragment_image_preview_name);
        textView.setText(name);
        mImageView = rootView.findViewById(R.id.dialog_fragment_image_preview_image);
        if (path == null) {
            Log.e(TAG, "onCreateDialog: You must pas a path");
            dismiss();
            return alert.show();
        }
        File file = new File(path);
        Glide.with(this)
                .asBitmap()
                .load(file)
                .signature(new ObjectKey(MainFragment.getCacheKey(file)))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .addListener(new RequestListener<Bitmap>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
//                        Log.d(TAG, "onLoadFailed() called with: e = [" + e + "], model = [" + model + "], target = [" + target + "], isFirstResource = [" + isFirstResource + "]");
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                        Log.d(TAG, "onResourceReady() called with: resource = [" + resource + "], model = [" + model + "], target = [" + target + "], dataSource = [" + dataSource + "], isFirstResource = [" + isFirstResource + "]");
//                        String path = model.toString();
//                        int width = resource.getWidth();
//                        int height = resource.getHeight();
//                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mImageView.getLayoutParams();
//                        params.dimensionRatio = width + ":" + height;
//                        mImageView.setLayoutParams(params);
//                        return false;
//                    }
//                })
                .into(mImageView);
        alert.setView(rootView);
        Dialog dialog = alert.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

}
