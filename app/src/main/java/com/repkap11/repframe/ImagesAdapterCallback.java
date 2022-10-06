package com.repkap11.repframe;

public interface ImagesAdapterCallback {
    void onImageChecked(boolean isChecked, String path);

    boolean isImageChecked(String path);
}