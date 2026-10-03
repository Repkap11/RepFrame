package com.repkap11.repframe.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.repkap11.repframe.R;
import com.repkap11.repframe.settings.SettingsActivity;
import com.repkap11.repframe.settings.SettingsFragment;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainFragment extends Fragment {

    private static final String TAG = MainFragment.class.getSimpleName();
    private static final Set<String> VIDEO_EXTENSIONS = new HashSet<>(Arrays.asList(
            "mp4", "mkv", "webm", "3gp", "3gpp", "mov", "avi", "mpg", "mpeg", "m2ts", "ts", "wmv", "flv", "m4v"));
    private static final int VIDEO_FADE_DURATION_MS = 700;
    boolean mHasPopulatedImageView = false;
    private Handler mHandler;
    private ImageView mImageView;
    private PlayerView mPlayerView;
    private ExoPlayer mPlayer;
    private View mFader;
    private File mVideoFile;
    private File mPendingFile;
    private File mCurrentFile;
    private boolean mAtBlack;
    private FileObserver mFileObserver;
    @NonNull
    private File mRootFile;
    @NonNull
    private List<File> mFilesList;
    private int mCurrentFileIndex = -1;
    private int mCurrentChangeOffset = 1;
    private boolean mKeepShowingImages = true;
    private int mImageDelay_s;
    private String mErrorMessage = null;
    private TextView mLabelView;
    private final Runnable mShowImageRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(this);
            if (mFilesList.size() == 0) {
                return;
            }
            mCurrentFileIndex += mCurrentChangeOffset;
            if (mCurrentFileIndex >= mFilesList.size()) {
                mCurrentFileIndex = 0;
            }
            if (mCurrentFileIndex < 0) {
                mCurrentFileIndex = mFilesList.size() - 1;
            }
            if (!mHasPopulatedImageView || mCurrentChangeOffset != 0) {
                setImageByPath(mFilesList.get(mCurrentFileIndex));
                mHasPopulatedImageView = true;
                preloadNextImage();
            }
            if (mKeepShowingImages && !isVideoFile(mFilesList.get(mCurrentFileIndex))) {
                Log.i(TAG, "run: Showing after:" + mImageDelay_s);
                long delay_ms = mImageDelay_s * 1000L;
                mHandler.postDelayed(this, delay_ms);
            }
        }
    };
    private String mPendingShareSoShowImage = null;

    public static boolean isVideoFile(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0) {
            return false;
        }
        String extension = name.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return (mimeType != null && mimeType.startsWith("video/")) || VIDEO_EXTENSIONS.contains(extension);
    }

    public static long getCacheKey(File file) {
        long lastModified = file.lastModified();
        long current_time = System.currentTimeMillis();
        long key;
        if (current_time - lastModified < 1000) {//If the file was changed within 1 second, don't cache it.
            Log.i(TAG, "getCacheKey: Using time for cache!!");
            key = current_time;
            //If this file changed super recently, don't cache it.
        } else {
            key = lastModified;
        }
        return key;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        mHandler = new Handler(Looper.getMainLooper());
        super.onCreate(savedInstanceState);
        mRootFile = MainActivity.getRootImagesFile(requireContext());
        updateFileListOrFinish();
        mFileObserver = new FileObserver(mRootFile, FileObserver.CREATE | FileObserver.DELETE) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                Log.d(TAG, "onEvent() called with: event = [" + event + "], path = [" + path + "]");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean wasEmpty = mFilesList.size() == 0;
                        updateFileListOrFinish();
                        if (mPendingShareSoShowImage != null) {
                            showPendingImage();
                        } else if (wasEmpty) {
                            mCurrentChangeOffset = 1;
                            mKeepShowingImages = true;
                            mShowImageRunnable.run();
                        }
                    }
                });
            }
        };
    }

    private void showPendingImage() {
        if (mPendingShareSoShowImage == null) {
            Log.e(TAG, "showPendingImageIfNeeded: No pending image!!!");
            return;
        }
        for (int i = 0; i < mFilesList.size(); i++) {
            if (mFilesList.get(i).getName().equals(mPendingShareSoShowImage)) {
                Log.i(TAG, "onEvent: Found image:" + i);
                mCurrentFileIndex = i - 1;
                break;
            }
        }
        mCurrentChangeOffset = 1;
        mKeepShowingImages = false;
        //Post needed since this is not the UI thread.
        mHandler.post(mShowImageRunnable);
    }

    public void setPendingImage(String fileName) {
        Log.i(TAG, "setPendingImage: Waiting for fileName:" + fileName);
        mPendingShareSoShowImage = fileName;
    }

    private void updateFileListOrFinish() {
        File[] files = mRootFile.listFiles();
        if (files == null) {
            //Ahh, Some other IO error. Exit!!
            requireActivity().finish();
            return;
        }
        mFilesList = Arrays.asList(files);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mImageView = rootView.findViewById(R.id.fragment_main_image);
        mPlayerView = rootView.findViewById(R.id.fragment_main_player);
        mFader = rootView.findViewById(R.id.fragment_main_fader);
        mHasPopulatedImageView = false;
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
                    resumeVideo();
                } else {
                    //Pause
                    mHandler.removeCallbacks(mShowImageRunnable);
                    pauseVideo();
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
        mPendingFile = file;
        if (mAtBlack) {
            swapToPendingFile();
            return;
        }
        mFader.animate().cancel();
        mFader.animate().alpha(1f).setDuration(VIDEO_FADE_DURATION_MS / 2).start();
        mHandler.postDelayed(mSwapToBlackRunnable, VIDEO_FADE_DURATION_MS / 2);
    }

    private final Runnable mSwapToBlackRunnable = new Runnable() {
        @Override
        public void run() {
            mAtBlack = true;
            swapToPendingFile();
        }
    };

    private void swapToPendingFile() {
        if (mPendingFile == null || mPendingFile.equals(mCurrentFile)) {
            revealContent();
            return;
        }
        mCurrentFile = mPendingFile;
        if (isVideoFile(mCurrentFile)) {
            startVideo(mCurrentFile);
        } else {
            showPhoto(mCurrentFile);
        }
    }

    private void revealContent() {
        mAtBlack = false;
        mFader.animate().cancel();
        mFader.animate().alpha(0f).setDuration(VIDEO_FADE_DURATION_MS / 2).start();
    }

    private void showPhoto(@NonNull File file) {
        mVideoFile = null;
        mHandler.removeCallbacks(mStartPlaybackRunnable);
        if (mPlayerView != null) {
            mPlayerView.setVisibility(View.GONE);
        }
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.clearMediaItems();
        }
        if (mImageView == null) {
            revealContent();
            return;
        }
        // The signature makes the cache key follow file changes.
        Glide.with(this)
                .asBitmap()
                .load(file)
                .signature(new ObjectKey(getCacheKey(file)))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(BitmapTransitionOptions.withCrossFade(700))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        String path = model.toString();
                        String name = new File(path).getName();
                        mErrorMessage = "Failed to load: " + name;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateUi();
                            }
                        });
                        if (mAtBlack) {
                            revealContent();
                        }
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
                        if (mAtBlack) {
                            revealContent();
                        }
                        return false;
                    }
                })
                .into(mImageView);
    }

    private void preloadNextImage() {
        if (mFilesList.size() < 2 || mImageView == null) {
            return;
        }
        int nextIndex = mCurrentFileIndex + 1;
        if (nextIndex >= mFilesList.size()) {
            nextIndex = 0;
        }
        File nextFile = mFilesList.get(nextIndex);
        int width = mImageView.getWidth();
        int height = mImageView.getHeight();
        if (isVideoFile(nextFile) || width <= 0 || height <= 0) {
            return;
        }
        Glide.with(this)
                .asBitmap()
                .load(nextFile)
                .fitCenter()
                .signature(new ObjectKey(getCacheKey(nextFile)))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .preload(width, height);
    }

    private final Runnable mStartPlaybackRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayer != null && mVideoFile != null) {
                mPlayer.play();
            }
        }
    };

    private void startVideo(@NonNull File file) {
        mVideoFile = file;
        mHandler.removeCallbacks(mStartPlaybackRunnable);
        ensurePlayer();
        if (mPlayerView != null) {
            mPlayerView.setVisibility(View.VISIBLE);
        }
        mPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)));
        mPlayer.setPlayWhenReady(false);
        mPlayer.prepare();
    }

    private void ensurePlayer() {
        if (mPlayer == null) {
            mPlayer = new ExoPlayer.Builder(requireContext()).build();
            mPlayer.setVolume(0f);
            mPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_READY) {
                        if (mAtBlack) {
                            revealContent();
                        }
                        mHandler.removeCallbacks(mStartPlaybackRunnable);
                        mHandler.postDelayed(mStartPlaybackRunnable, VIDEO_FADE_DURATION_MS / 2);
                    } else if (playbackState == Player.STATE_ENDED && mKeepShowingImages) {
                        mCurrentChangeOffset = 1;
                        mHandler.post(mShowImageRunnable);
                    }
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    Log.w(TAG, "onPlayerError: Failed to play:" + mVideoFile, error);
                    mErrorMessage = "Failed to load: " + (mVideoFile == null ? "video" : mVideoFile.getName());
                    if (mPlayerView != null) {
                        mPlayerView.setVisibility(View.GONE);
                    }
                    if (mPlayer != null) {
                        mPlayer.stop();
                        mPlayer.clearMediaItems();
                    }
                    mVideoFile = null;
                    if (mAtBlack) {
                        revealContent();
                    }
                    updateUi();
                    if (mKeepShowingImages) {
                        long delay_ms = mImageDelay_s * 1000L;
                        mHandler.postDelayed(mShowImageRunnable, delay_ms);
                    }
                }
            });
            if (mPlayerView != null) {
                mPlayerView.setPlayer(mPlayer);
            }
        }
    }

    private void pauseVideo() {
        mHandler.removeCallbacks(mStartPlaybackRunnable);
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    private void resumeVideo() {
        if (mPlayer == null || mVideoFile == null) {
            return;
        }
        if (mPlayer.getPlaybackState() == Player.STATE_ENDED) {
            mCurrentChangeOffset = 1;
            mShowImageRunnable.run();
            return;
        }
        mHandler.removeCallbacks(mStartPlaybackRunnable);
        mHandler.postDelayed(mStartPlaybackRunnable, VIDEO_FADE_DURATION_MS / 2);
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
        if (mFilesList.size() == 0) {
            selectable = true;
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
        updateFileListOrFinish();
        if (mPendingShareSoShowImage != null) {
            showPendingImage();
        } else {
            mHandler.removeCallbacks(mShowImageRunnable);
            mKeepShowingImages = true;
            mCurrentChangeOffset = 0;
            mShowImageRunnable.run();
            mCurrentChangeOffset = 1;
            resumeVideo();
        }
        mFileObserver.startWatching();
        updateUi();
    }

    @Override
    public void onStop() {
        mFileObserver.stopWatching();
        mHandler.removeCallbacks(mShowImageRunnable);
        pauseVideo();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mImageView = null;
        mLabelView = null;
        mPlayerView = null;
        mFader = null;
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

