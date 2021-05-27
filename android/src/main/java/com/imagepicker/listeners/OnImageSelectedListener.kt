package com.imagepicker.listeners

import com.imagepicker.model.Image

interface OnImageSelectedListener {
    fun onSelectionUpdate(selectedImage: List<Image?>?)
}