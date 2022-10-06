package com.repkap11.repframe;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private File mRootFile;

    @NonNull
    public static File getRootImagesFile(Context context) {
        return new File(context.getExternalFilesDir(null), "images");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideSystemBars();

        mRootFile = getRootImagesFile(this);
        mRootFile.mkdirs();//This helps the user fine the location where to put their files.
        if (!mRootFile.exists()) {
            //Ahh, our folder doesn't exist. Exit!!
            finish();
            return;
        }
        if (!mRootFile.isDirectory()) {
            //Ahh, our folder isn't a folder. Exit!!
            finish();
            return;
        }
        Log.i(TAG, "Add files to:" + mRootFile.getPath());

        Intent startingIntent = getIntent();
        processIntents(startingIntent);
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntents(intent);
    }

    private void processIntents(Intent intent) {
        Log.i(TAG, "processIntents: Processing Intent");
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (Intent.ACTION_SEND.equals(action)) {
            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            handleImageUriFromShare(imageUri);
        }
        if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            ArrayList<Parcelable> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            for (Parcelable parcelable : imageUris) {
                Uri imageUri = (Uri) parcelable;
                handleImageUriFromShare(imageUri);
            }
        }
    }

    public File getFileForURI(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String type = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
        File outputPath = null;
        try {
            outputPath = File.createTempFile("SharedImage_", "." + type, mRootFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputPath;
    }

    private void handleImageUriFromShare(Uri imageUri) {
        if (imageUri == null) {
            Log.w(TAG, "handleActionSendIntent: No image");
            return;
        }
        ContentResolver contentResolver = getContentResolver();
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = contentResolver.openInputStream(imageUri);
            File outputFile = getFileForURI(imageUri);
            String fileName = outputFile.getName();
            MainFragment frag = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.activity_main_frag);
            if (frag != null) {
                frag.setPendingImage(fileName);
            }

            Log.i(TAG, "handleActionSendIntent: Saving to file:" + fileName);
            fileOutputStream = new FileOutputStream(outputFile);
            byte[] buf = new byte[1024];
            inputStream.read(buf);
            do {
                fileOutputStream.write(buf);
            } while (inputStream.read(buf) != -1);
//            Toast.makeText(this, "Added image:" + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (fileOutputStream != null) fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
