package com.repkap11.repframe.image_manager;

public interface ItemCheckedCallback {
    boolean onItemChecked(int index, boolean isChecked);

    boolean onItemLongClicked(int index);
}