package com.repkap11.repframe.image_manager;

public interface ItemCheckedCallback {
    void onItemChecked(int index, boolean isChecked);

    void onItemLongClicked(int index);
}