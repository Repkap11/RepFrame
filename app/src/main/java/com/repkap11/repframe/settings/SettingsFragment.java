package com.repkap11.repframe.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.repkap11.repframe.BuildConfig;
import com.repkap11.repframe.R;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String PREFERENCES_REPFRAME = "PREFERENCES_REPFRAME";
    private static final String PREF_IMAGE_DELAY_S = "preference_image_delay_s";
    private static final String PREF_APP_VERSION = "preference_app_version";
    private SharedPreferences mSharedPrefs;


    public static int getImageDelaySeconds(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_REPFRAME, Context.MODE_PRIVATE);
        int defaultValue = context.getResources().getInteger(R.integer.image_delay_s_default);
        return prefs.getInt(PREF_IMAGE_DELAY_S, defaultValue);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = requireContext().getSharedPreferences(PREFERENCES_REPFRAME, Context.MODE_PRIVATE);
    }

    @SuppressWarnings("TypeParameterUnusedInFormals")
    @NonNull
    public <T extends Preference> T requirePreference(@NonNull CharSequence key) {
        return Objects.requireNonNull(findPreference(key));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(PREFERENCES_REPFRAME);
        setPreferencesFromResource(R.xml.preferences_settings, rootKey);
        SeekBarPreference imageDelayPref = requirePreference(PREF_IMAGE_DELAY_S);
        imageDelayPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final int valueInt = Integer.valueOf(String.valueOf(newValue));
                String valueStr = getResources().getQuantityString(R.plurals.quantity_seconds, valueInt, valueInt);
                preference.setSummary(valueStr);
                return true;
            }
        });
        imageDelayPref.getOnPreferenceChangeListener().onPreferenceChange(imageDelayPref, imageDelayPref.getValue());

        Preference appVersionPreference = requirePreference("preference_app_version");
        appVersionPreference.setSummary(BuildConfig.VERSION_NAME);
    }
}
