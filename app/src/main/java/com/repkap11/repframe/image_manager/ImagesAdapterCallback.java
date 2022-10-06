package com.repkap11.repframe.image_manager;

public interface ImagesAdapterCallback {
    void onImageChecked(boolean isChecked, String path);

    boolean isImageChecked(String path);

    void onImageLongClicked(String path);
}