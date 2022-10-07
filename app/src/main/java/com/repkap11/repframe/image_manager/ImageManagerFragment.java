package com.repkap11.repframe.image_manager;

import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.repkap11.repframe.R;
import com.repkap11.repframe.main.MainActivity;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ImageManagerFragment extends Fragment {

    private static final String TAG = ImageManagerFragment.class.getSimpleName();
    private File mRootFile;
    private FileObserver mFileObserver;
    private List<File> mFilesList;
    private ImagesAdapter mAdapter;
    private HashSet<String> mCheckedFiles;
    private View mDeleteButton;
    private Handler mMainHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        mMainHandler = new Handler(Looper.getMainLooper());
        mRootFile = MainActivity.getRootImagesFile(requireContext());
        mCheckedFiles = new HashSet<String>();
        mAdapter = new ImagesAdapter(new ImagesAdapterCallback() {
            @Override
            public boolean onImageChecked(boolean isChecked, String path) {
                Log.d(TAG, "onImageChecked() called with: isChecked = [" + isChecked + "], path = [" + path + "]");
                if (isChecked) {
                    mCheckedFiles.add(path);
                } else {
                    mCheckedFiles.remove(path);
                }
                updateDeleteButtonState();
                return isChecked;
            }

            @Override
            public boolean isImageChecked(String path) {
                return mCheckedFiles.contains(path);
            }

            @Override
            public boolean onImageLongClicked(String path) {
                ImagePreviewDialogFragment dialogFragment = new ImagePreviewDialogFragment();
                Bundle args = new Bundle();
                args.putString(ImagePreviewDialogFragment.ARG_IMAGE_PATH, path);
                dialogFragment.setArguments(args);
                dialogFragment.show(getParentFragmentManager(), TAG + "ImagePreviewDialogFragment" + path);
                return false;
            }
        });
        updateFileListOrFinish();
        mFileObserver = new FileObserver(mRootFile, FileObserver.CREATE | FileObserver.DELETE) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                Log.d(TAG, "onEvent() called with: event = [" + event + "], path = [" + path + "]");
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateFileListOrFinish();
                    }
                });
            }
        };

    }

    private void updateDeleteButtonState() {
        if (mDeleteButton == null) {
            return;
        }
        mDeleteButton.setEnabled(!mCheckedFiles.isEmpty());

    }

    private void updateFileListOrFinish() {
        mFilesList = Arrays.asList(mRootFile.listFiles());
        if (mFilesList == null) {
            //Ahh, Some other IO error. Exit!!
            requireActivity().finish();
            return;
        }
        Iterator<String> it = mCheckedFiles.iterator();
        boolean removedAny = false;
        while (it.hasNext()) {
            String path = it.next();
            if (!mFilesList.contains(path)) {
                it.remove();
                removedAny = true;
            }
        }
        if (removedAny) {
            updateDeleteButtonState();
        }
        mAdapter.onFilesChanged(mFilesList);
    }

    public boolean onBackForFrag() {
        if (mCheckedFiles.isEmpty()) {
            return false;
        }
        mCheckedFiles.clear();
        updateDeleteButtonState();
        mAdapter.notifyDataSetChanged();
        return true;
    }


    @Override
    public void onStart() {
        super.onStart();
        updateFileListOrFinish();
        mFileObserver.startWatching();
    }

    @Override
    public void onStop() {
        mFileObserver.stopWatching();
        super.onStop();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_manager, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.fragment_image_manager_recycler);
        recyclerView.setAdapter(mAdapter);
        mDeleteButton = rootView.findViewById(R.id.fragment_image_manager_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String path : mCheckedFiles) {
                    Log.i(TAG, "onClick: Deleting:" + path);
                    File file = new File(path);
                    if (file.exists() && file.canWrite()) {
                        file.delete();
                    }
                }
                mCheckedFiles.clear();
                updateDeleteButtonState();
                mAdapter.notifyDataSetChanged();//Might not be needed, since the file IO should refresh it.
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateDeleteButtonState();
    }

    @Override
    public void onDestroyView() {
        mDeleteButton = null;
        super.onDestroyView();
    }
}
