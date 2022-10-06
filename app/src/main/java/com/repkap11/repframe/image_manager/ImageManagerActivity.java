package com.repkap11.repframe.image_manager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.repkap11.repframe.R;

public class ImageManagerActivity extends AppCompatActivity {
    private static final String TAG = ImageManagerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_manager);
    }

    @Override
    public void onBackPressed() {
        ImageManagerFragment frag = (ImageManagerFragment) getSupportFragmentManager().findFragmentById(R.id.activity_image_manager_frag);
        boolean handledBack = false;
        if (frag != null) {
            handledBack = frag.onBackForFrag();
        }
        if (!handledBack) {
            super.onBackPressed();
        }
    }
}
