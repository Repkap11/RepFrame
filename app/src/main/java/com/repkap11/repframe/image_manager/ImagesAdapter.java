package com.repkap11.repframe.image_manager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.repkap11.repframe.R;
import com.repkap11.repframe.main.MainFragment;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
    private static final String TAG = ImagesAdapter.class.getSimpleName();
    private final Handler mMainHandler;
    private final ImagesAdapterCallback mImageCheckCallback;
    private List<File> mFiles = Collections.emptyList();

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
            public boolean onItemClicked(int index, boolean isCurrentlyChecked) {
                return mImageCheckCallback.onImageClicked(isCurrentlyChecked, mFiles.get(index).getPath());
            }

            @Override
            public boolean onItemLongClicked(int index) {
                return mImageCheckCallback.onImageLongClicked(mFiles.get(index).getPath());
            }

            @Override
            public boolean isAnyImageChecked() {
                return mImageCheckCallback.isAnyImageChecked();
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File imageFile = mFiles.get(position);
        holder.Name.setText(imageFile.getName());
        holder.Click.setTag(holder);
        holder.Check.setChecked(mImageCheckCallback.isImageChecked(imageFile.getPath()));
        holder.Check.setVisibility(mImageCheckCallback.isAnyImageChecked() ? View.VISIBLE : View.INVISIBLE);
        Glide.with(holder.Image.getContext())
                .asBitmap()
                .signature(new ObjectKey(MainFragment.getCacheKey(imageFile)))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(imageFile).into(holder.Image);
    }

    public void onFilesChanged(List<File> files) {
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
        return mFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView Name;
        public ImageView Image;
        public CheckBox Check;
        public View Click;
        public ItemCheckedCallback callback;

        public ViewHolder(@NonNull View itemView, ItemCheckedCallback callback) {
            super(itemView);
            this.callback = callback;
            this.Name = itemView.findViewById(R.id.fragment_image_manager_recycler_item_name);
            this.Image = itemView.findViewById(R.id.fragment_image_manager_recycler_item_image);
            this.Check = itemView.findViewById(R.id.fragment_image_manager_recycler_item_check);
            this.Click = itemView.findViewById(R.id.fragment_image_manager_recycler_item_click_region);
            this.Click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder holder = (ViewHolder) v.getTag();
                    if (holder == null) {
                        return;
                    }
                    boolean checked = holder.callback.onItemClicked(holder.getAdapterPosition(), holder.Check.isChecked());
                    holder.Check.setChecked(checked);
                }
            });
            this.Click.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ViewHolder holder = (ViewHolder) v.getTag();
                    if (holder == null) {
                        return false;
                    }
                    boolean checked = holder.callback.onItemLongClicked(holder.getAdapterPosition());
                    if (checked) {
                        holder.Check.setChecked(checked);
                    }
                    return true;
                }
            });
        }
    }
}
