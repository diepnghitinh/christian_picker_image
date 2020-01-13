package com.imagepicker.features.recyclers;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.christian.christian_picker_image.R;
import com.imagepicker.adapter.FolderPickerAdapter;
import com.imagepicker.adapter.ImagePickerAdapter;
import com.imagepicker.features.ImagePickerConfig;
import com.imagepicker.features.ReturnMode;
import com.imagepicker.features.imageloader.ImageLoader;
import com.imagepicker.helper.ConfigUtils;
import com.imagepicker.helper.ImagePickerUtils;
import com.imagepicker.listeners.OnFolderClickListener;
import com.imagepicker.listeners.OnImageClickListener;
import com.imagepicker.listeners.OnImageSelectedListener;
import com.imagepicker.model.Folder;
import com.imagepicker.model.Image;
import com.imagepicker.view.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

import static com.imagepicker.features.IpCons.MAX_LIMIT;
import static com.imagepicker.features.IpCons.MODE_MULTIPLE;
import static com.imagepicker.features.IpCons.MODE_SINGLE;

public class RecyclerViewManager {

    private final Context context;
    private final RecyclerView recyclerView;
    private final ImagePickerConfig config;

    private GridLayoutManager layoutManager;
    private GridSpacingItemDecoration itemOffsetDecoration;

    private ImagePickerAdapter imageAdapter;
    private FolderPickerAdapter folderAdapter;

    private Parcelable foldersState;

    private int imageColumns;
    private int folderColumns;

    public RecyclerViewManager(RecyclerView recyclerView, ImagePickerConfig config, int orientation) {
        this.recyclerView = recyclerView;
        this.config = config;
        this.context = recyclerView.getContext();
        changeOrientation(orientation);
    }

    /**
     * Set item size, column size base on the screen orientation
     */
    public void changeOrientation(int orientation) {
        imageColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5;
        folderColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4;

        boolean shouldShowFolder = config.isFolderMode() && isDisplayingFolderView();
        int columns = shouldShowFolder ? folderColumns : imageColumns;
        layoutManager = new GridLayoutManager(context, columns);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        setItemDecoration(columns);
    }

    public void setupAdapters(OnImageClickListener onImageClickListener, OnFolderClickListener onFolderClickListener) {
        ArrayList<Image> selectedImages = null;
        if (config.getMode() == MODE_MULTIPLE && !config.getSelectedImages().isEmpty()) {
            selectedImages = config.getSelectedImages();
        }

        /* Init folder and image adapter */
        final ImageLoader imageLoader = config.getImageLoader();
        imageAdapter = new ImagePickerAdapter(context, imageLoader, selectedImages, onImageClickListener);
        folderAdapter = new FolderPickerAdapter(context, imageLoader, bucket -> {
            foldersState = recyclerView.getLayoutManager().onSaveInstanceState();
            onFolderClickListener.onFolderClick(bucket);
        });
    }

    private void setItemDecoration(int columns) {
        if (itemOffsetDecoration != null) {
            recyclerView.removeItemDecoration(itemOffsetDecoration);
        }
        itemOffsetDecoration = new GridSpacingItemDecoration(
                columns,
                context.getResources().getDimensionPixelSize(R.dimen.ef_item_padding),
                false
        );
        recyclerView.addItemDecoration(itemOffsetDecoration);

        layoutManager.setSpanCount(columns);
    }

    public void handleBack(OnBackAction action) {
        if (config.isFolderMode() && !isDisplayingFolderView()) {
            setFolderAdapter(null);
            action.onBackToFolder();
            return;
        }
        action.onFinishImagePicker();
    }

    private boolean isDisplayingFolderView() {
        return recyclerView.getAdapter() == null || recyclerView.getAdapter() instanceof FolderPickerAdapter;
    }

    public String getTitle() {
        if (isDisplayingFolderView()) {
            return ConfigUtils.getFolderTitle(context, config);
        }

        if (config.getMode() == MODE_SINGLE) {
            return ConfigUtils.getImageTitle(context, config);
        }

        final int imageSize = imageAdapter.getSelectedImages().size();
        final boolean useDefaultTitle = !ImagePickerUtils.isStringEmpty(config.getImageTitle()) && imageSize == 0;

        if (useDefaultTitle) {
            return ConfigUtils.getImageTitle(context, config);
        }
        return config.getLimit() == MAX_LIMIT
                ? String.format(context.getString(R.string.ef_selected), imageSize)
                : String.format(context.getString(R.string.ef_selected_with_limit), imageSize, config.getLimit());
    }

    public void setImageAdapter(List<Image> images) {
        imageAdapter.setData(images);
        setItemDecoration(imageColumns);
        recyclerView.setAdapter(imageAdapter);
    }

    public void setFolderAdapter(List<Folder> folders) {
        folderAdapter.setData(folders);
        setItemDecoration(folderColumns);
        recyclerView.setAdapter(folderAdapter);

        if (foldersState != null) {
            layoutManager.setSpanCount(folderColumns);
            recyclerView.getLayoutManager().onRestoreInstanceState(foldersState);
        }
    }

    /* --------------------------------------------------- */
    /* > Images */
    /* --------------------------------------------------- */

    private void checkAdapterIsInitialized() {
        if (imageAdapter == null) {
            throw new IllegalStateException("Must call setupAdapters first!");
        }
    }

    public List<Image> getSelectedImages() {
        checkAdapterIsInitialized();
        return imageAdapter.getSelectedImages();
    }

    public void setImageSelectedListener(OnImageSelectedListener listener) {
        checkAdapterIsInitialized();
        imageAdapter.setImageSelectedListener(listener);
    }

    public boolean selectImage(boolean isSelected) {
        if (config.getMode() == MODE_MULTIPLE) {
            if (imageAdapter.getSelectedImages().size() >= config.getLimit() && !isSelected) {
                Toast.makeText(context, R.string.ef_msg_limit_images, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (config.getMode() == MODE_SINGLE) {
            if (imageAdapter.getSelectedImages().size() > 0) {
                imageAdapter.removeAllSelectedSingleClick();
            }
        }
        return true;
    }

    public boolean isShowDoneButton() {
        return !isDisplayingFolderView()
                && !imageAdapter.getSelectedImages().isEmpty()
                && (config.getReturnMode() != ReturnMode.ALL && config.getReturnMode() != ReturnMode.GALLERY_ONLY);
    }

}
