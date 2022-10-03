package com.repkap11.repframe;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

public class NumberPickerPreference extends DialogPreference {
    private int mValue;
    private boolean mValueSet;
    private CharSequence[] mEntriesCharSequence = null;
    private int[] mEntriesInt = null;

    public NumberPickerPreference(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference, defStyleAttr, defStyleRes);
        mEntriesCharSequence = ta.getTextArray(R.styleable.NumberPickerPreference_entries);
        mEntriesInt = new int[mEntriesCharSequence.length];
        for (int i = 0; i < mEntriesCharSequence.length; i++) {
            mEntriesInt[i] = Integer.parseInt(mEntriesCharSequence[i].toString());
        }

        Log.i("Paul", "init: entries:" + mEntriesCharSequence.length);

        ta.recycle();

        setDialogLayoutResource(R.layout.dialog_preference_number_picker);
    }

    public void setValueIndex(int index) {
        setValue(mEntriesInt[index]);
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        final boolean changed = value != getValue();

        if (changed || !mValueSet) {
            mValue = value;
            mValueSet = true;
            persistInt(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    @Nullable
    public int[] getEntries() {
        return mEntriesInt;
    }

    @NonNull
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        int realDefault = ((defaultValue != null) ? (Integer) defaultValue : 0);
        int quality = getPersistedInt(realDefault);
        setValue(quality);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = getValue();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValueIndex(myState.value);
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int value;

        public SavedState(Parcel source) {
            super(source);
            value = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
        }
    }
}