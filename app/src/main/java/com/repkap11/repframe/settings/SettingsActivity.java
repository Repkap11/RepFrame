package com.repkap11.repframe.settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.repkap11.repframe.R;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
