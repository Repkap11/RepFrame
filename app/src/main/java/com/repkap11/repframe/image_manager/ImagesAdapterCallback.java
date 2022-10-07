package com.repkap11.repframe.image_manager;

public interface ImagesAdapterCallback {
    boolean onImageChecked(boolean isChecked, String path);

    boolean isImageChecked(String path);

    boolean onImageLongClicked(String path);
}