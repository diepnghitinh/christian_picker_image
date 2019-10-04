package com.imagepicker.listeners;

import com.imagepicker.model.Image;

import java.util.List;

public interface OnImageSelectedListener {
    void onSelectionUpdate(List<Image> selectedImage);
}
