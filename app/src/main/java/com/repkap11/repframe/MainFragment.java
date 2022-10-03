package com.repkap11.repframe;

import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;

public class MainFragment extends Fragment {

    private static final String TAG = MainFragment.class.getSimpleName();
    private static final long IMAGE_DELAY_MS = 1000;
    private Handler mHandler;
    private ImageView mImageView;
    private FileObserver mFileObserver;
    private File mRootFile;
    private File[] mFilesList;
    private int mCurrentFileIndex = 0;
    private final Runnable mNextImageRunnable = new Runnable() {
        @Override
        public void run() {
            mCurrentFileIndex += 1;
            if (mCurrentFileIndex >= mFilesList.length) {
                mCurrentFileIndex = 0;
            }
            setImageByPath(mFilesList[mCurrentFileIndex]);
            mHandler.postDelayed(this, IMAGE_DELAY_MS);
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
        mFileObserver = new FileObserver(mRootFile, FileObserver.CREATE | FileObserver.DELETE) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                Log.d(TAG, "onEvent() called with: event = [" + event + "], path = [" + path + "]");
                mFilesList = mRootFile.listFiles();
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mImageView = rootView.findViewById(R.id.fragment_main_image);
        setImageByPath(new File(mRootFile, "test.png"));
        return rootView;
    }

    private void setImageByPath(File file) {
        if (file.exists()) {
            Glide.with(this)
                    .load(file)
                    .into(mImageView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mFileObserver.startWatching();
        mHandler.post(mNextImageRunnable);
    }

    @Override
    public void onStop() {
        mFileObserver.stopWatching();
        mHandler.removeCallbacks(mNextImageRunnable);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mImageView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

