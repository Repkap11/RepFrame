package com.repkap11.repframe;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
    private static final String TAG = ImagesAdapter.class.getSimpleName();
    private final Handler mMainHandler;
    private final ImagesAdapterCallback mImageCheckCallback;
    private File[] mFiles = new File[]{};

    public ImagesAdapter(ImagesAdapterCallback callback) {
        mMainHandler = new Handler(Looper.getMainLooper());
        mImageCheckCallback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View child = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_image_manager_item, parent, false);
        return new ViewHolder(child, new ItemCheckedCallback() {
            @Override
            public void onItemChecked(int index, boolean isChecked) {
                mImageCheckCallback.onImageChecked(isChecked, mFiles[index].getPath());
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File imageFile = mFiles[position];
        holder.Name.setText(imageFile.getName());
        holder.Check.setChecked(mImageCheckCallback.isImageChecked(imageFile.getPath()));
        Glide.with(holder.Image.getContext())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(imageFile).into(holder.Image);
    }

    public void onFilesChanged(File[] files) {
        if (files == null) {
            Log.e(TAG, "onFilesChanged: Bad, null files");
            return;
        }
//        final int SCALE = 10;
//        mFiles = new File[files.length * SCALE];
//        for (int i = 0; i < SCALE; i++) {
//            System.arraycopy(files, 0, mFiles, files.length * i, files.length);
//        }
        mFiles = files;
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView Name;
        public ImageView Image;
        public CheckBox Check;
        public ItemCheckedCallback callback;

        public ViewHolder(@NonNull View itemView, ItemCheckedCallback callback) {
            super(itemView);
            this.callback = callback;
            this.Name = itemView.findViewById(R.id.fragment_image_manager_recycler_item_name);
            this.Image = itemView.findViewById(R.id.fragment_image_manager_recycler_item_image);
            this.Check = itemView.findViewById(R.id.fragment_image_manager_recycler_item_check);
            this.Check.setTag(this);
            this.Check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ViewHolder holder = (ViewHolder) buttonView.getTag();
                    holder.callback.onItemChecked(holder.getAdapterPosition(), isChecked);
                }
            });
        }
    }
}
