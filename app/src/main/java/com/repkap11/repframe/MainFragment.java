package com.repkap11.repframe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

public class MainFragment extends Fragment {

    private static final String TAG = MainFragment.class.getSimpleName();
    private Handler mHandler;
    private ImageView mImageView;
    private FileObserver mFileObserver;
    private File mRootFile;
    @NonNull
    private File[] mFilesList;
    private int mCurrentFileIndex = 0;
    private int mCurrentChangeOffset = 1;
    private boolean mKeepShowingImages = true;
    private int mImageDelay_s;
    private String mErrorMessage = null;
    private TextView mLabelView;
    private final Runnable mShowImageRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(this);
            if (mFilesList.length == 0) {
                return;
            }
            mCurrentFileIndex += mCurrentChangeOffset;
            if (mCurrentFileIndex >= mFilesList.length) {
                mCurrentFileIndex = 0;
            }
            if (mCurrentFileIndex < 0) {
                mCurrentFileIndex = mFilesList.length - 1;
            }
            if (mCurrentChangeOffset != 0) {
                setImageByPath(mFilesList[mCurrentFileIndex]);
            }
            if (mKeepShowingImages) {
                Log.i(TAG, "run: Showing after:" + mImageDelay_s);
                long delay_ms = mImageDelay_s * 1000L;
                mHandler.postDelayed(this, delay_ms);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        mHandler = new Handler(Looper.getMainLooper());
        super.onCreate(savedInstanceState);
        mRootFile = new File(requireContext().getExternalFilesDir(null), "images");
        mRootFile.mkdir();//This helps the user fine the location where to put their files.
        if (!mRootFile.exists()) {
            //Ahh, our folder doesn't exist. Exit!!
            requireActivity().finish();
            return;
        }
        if (!mRootFile.isDirectory()) {
            //Ahh, our folder isn't a folder. Exit!!
            requireActivity().finish();
            return;
        }
        Log.i(TAG, "Add files to:" + mRootFile.getPath());

        mFilesList = mRootFile.listFiles();
        if (mFilesList == null) {
            //Ahh, Some other IO error. Exit!!
            requireActivity().finish();
            return;
        }
        mFileObserver = new FileObserver(mRootFile, FileObserver.CREATE | FileObserver.DELETE) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                Log.d(TAG, "onEvent() called with: event = [" + event + "], path = [" + path + "]");
                boolean wasEmpty = mFilesList.length == 0;
                mFilesList = mRootFile.listFiles();
                if (mFilesList == null) {
                    //Ahh, Some other IO error. Exit!!
                    requireActivity().finish();
                    return;
                }
                if (wasEmpty) {
                    mKeepShowingImages = true;
                    mCurrentChangeOffset = 1;
                    //Post needed since this is not the UI thread.
                    mHandler.post(mShowImageRunnable);
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mImageView = rootView.findViewById(R.id.fragment_main_image);
        mLabelView = rootView.findViewById(R.id.fragment_main_label);
        rootView.findViewById(R.id.fragment_main_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKeepShowingImages = false;
                mCurrentChangeOffset = 1;
                mShowImageRunnable.run();
                updateUi();
            }
        });
        rootView.findViewById(R.id.fragment_main_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKeepShowingImages = false;
                mCurrentChangeOffset = -1;
                mShowImageRunnable.run();
                updateUi();
            }
        });
        rootView.findViewById(R.id.fragment_main_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKeepShowingImages = !mKeepShowingImages;
                if (mKeepShowingImages) {
                    //Play
                    mCurrentChangeOffset = 0;
                    mShowImageRunnable.run();
                    mCurrentChangeOffset = 1;
                } else {
                    //Pause
                    mHandler.removeCallbacks(mShowImageRunnable);
                }
                updateUi();
            }
        });
        rootView.findViewById(R.id.fragment_main_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });
        updateUi();
        return rootView;
    }

    private void setImageByPath(File file) {
        if (file.exists()) {
            // Not caching is needed to get fade to work reliably.
            Glide.with(this)
                    .asBitmap()
                    .load(file)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transition(BitmapTransitionOptions.withCrossFade(500))
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
//                            Log.d(TAG, "onLoadFailed() called with: e = [" + e + "], model = [" + model + "], target = [" + target + "], isFirstResource = [" + isFirstResource + "]");
                            String path = model.toString();
                            String name = new File(path).getName();
                            mErrorMessage = "Failed to load: " + name;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateUi();
                                }
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            if (mErrorMessage != null) {
                                mErrorMessage = null;
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateUi();
                                    }
                                });
                            }
                            return false;
                        }
                    })
                    .into(mImageView);
//            Glide.with(this).load(file).into(mImageView);
        }
    }

    private void updateUi() {
        String labelValue = null;
        boolean selectable = false;
        if (!mKeepShowingImages) {
            labelValue = "Paused";
        }
        if (mErrorMessage != null) {
            labelValue = mErrorMessage;
        }
        if (mFilesList.length == 0) {
            selectable = true;
            labelValue = "No files found in: " + mRootFile;
        }
        mLabelView.setVisibility(labelValue == null ? View.INVISIBLE : View.VISIBLE);
        mLabelView.setText(labelValue);
        mLabelView.setTextIsSelectable(selectable);
    }

    @Override
    public void onStart() {
        super.onStart();
        mImageDelay_s = SettingsFragment.getImageDelaySeconds(requireContext());
        Log.i(TAG, "onStart: Using delay:" + mImageDelay_s);
        mFileObserver.startWatching();
        mHandler.removeCallbacks(mShowImageRunnable);
        mKeepShowingImages = true;
        mCurrentChangeOffset = 0;
        mShowImageRunnable.run();
        mCurrentChangeOffset = 1;
        updateUi();
    }

    @Override
    public void onStop() {
        mFileObserver.stopWatching();
        mHandler.removeCallbacks(mShowImageRunnable);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mImageView = null;
        mLabelView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

