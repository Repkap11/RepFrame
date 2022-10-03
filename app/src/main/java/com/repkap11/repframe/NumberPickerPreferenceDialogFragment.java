package com.repkap11.repframe;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.util.Arrays;

public class NumberPickerPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private static final String TAG = NumberPickerPreferenceDialogFragment.class.getSimpleName();
    private NumberPicker mNumberPicker;
    private int mValue;

    @NonNull
    public static NumberPickerPreferenceDialogFragment newInstance(@NonNull String key) {
        final NumberPickerPreferenceDialogFragment fragment = new NumberPickerPreferenceDialogFragment();
        final Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    private static String[] intToStringArray(int[] entries) {
        String[] result = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            result[i] = Integer.toString(entries[i]);
        }
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mValue = getNumberPickerPreference().getValue();
        } else {
            mValue = savedInstanceState.getInt(TAG);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TAG, mValue);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        mNumberPicker = view.findViewById(R.id.dialog_preference_number_picker_picker);

        int[] entries = getNumberPickerPreference().getEntries();
        if (entries == null) {
            throw new RuntimeException("You must have entries");
        }
        Log.i("Paul", "onBindDialogView2: entries.length:" + entries.length + " value:" + mValue);
        int index = Arrays.binarySearch(entries, mValue);
        if (index < 0) {
            index = 0;
        }
        mNumberPicker.setDisplayedValues(intToStringArray(entries));

        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(entries.length - 1);
        mNumberPicker.setValue(index);

        mNumberPicker.setWrapSelectorWheel(false);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            mNumberPicker.clearFocus();

            final int value = mNumberPicker.getValue();
            NumberPickerPreference pref = getNumberPickerPreference();
            if (pref.callChangeListener(value)) {
                pref.setValueIndex(value);
            }
        }
    }

    private NumberPickerPreference getNumberPickerPreference() {
        return (NumberPickerPreference) getPreference();
    }
}