package com.repkap11.repframe.image_manager;

public interface ItemCheckedCallback {
    boolean onItemClicked(int index, boolean isCurrentlyChecked);

    boolean onItemLongClicked(int index);

    boolean isAnyImageChecked();
}