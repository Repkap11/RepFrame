package com.repkap11.repframe.image_manager;

public interface ImagesAdapterCallback {
    boolean onImageClicked(boolean isCurrentlyChecked, String path, int index);

    boolean isImageChecked(String path);

    boolean onImageLongClicked(String path);

    boolean isAnyImageChecked();
}