package com.imagepicker.features;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.christian.christian_picker_image.R;
import com.imagepicker.features.camera.DefaultCameraModule;
import com.imagepicker.features.common.BaseConfig;
import com.imagepicker.features.common.BasePresenter;
import com.imagepicker.features.common.ImageLoaderListener;
import com.imagepicker.helper.ConfigUtils;
import com.imagepicker.model.Folder;
import com.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ImagePickerPresenter extends BasePresenter<ImagePickerView> {

    private ImageFileLoader imageLoader;
    private DefaultCameraModule cameraModule;
    private Handler main = new Handler(Looper.getMainLooper());

    ImagePickerPresenter(ImageFileLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    DefaultCameraModule getCameraModule() {
        if (cameraModule == null) {
            cameraModule = new DefaultCameraModule();
        }
        return cameraModule;
    }

    /* Set the camera module in onRestoreInstance */
    void setCameraModule(DefaultCameraModule cameraModule) {
        this.cameraModule = cameraModule;
    }

    void abortLoad() {
        imageLoader.abortLoadImages();
    }

    void loadImages(ImagePickerConfig config) {
        if (!isViewAttached()) return;

        boolean isFolder = config.isFolderMode();
        boolean includeVideo = config.isIncludeVideo();
        ArrayList<File> excludedImages = config.getExcludedImages();

        runOnUiIfAvailable(() -> getView().showLoading(true));

        imageLoader.loadDeviceImages(isFolder, includeVideo, excludedImages, new ImageLoaderListener() {
            @Override
            public void onImageLoaded(final List<Image> images, final List<Folder> folders) {
                runOnUiIfAvailable(() -> {
                    getView().showFetchCompleted(images, folders);

                    final boolean isEmpty = folders != null
                            ? folders.isEmpty()
                            : images.isEmpty();

                    if (isEmpty) {
                        getView().showEmpty();
                    } else {
                        getView().showLoading(false);
                    }
                });
            }

            @Override
            public void onFailed(final Throwable throwable) {
                runOnUiIfAvailable(() -> getView().showError(throwable));
            }
        });
    }

    void onDoneSelectImages(List<Image> selectedImages) {
        if (selectedImages != null && selectedImages.size() > 0) {

            /* Scan selected images which not existed */
            for (int i = 0; i < selectedImages.size(); i++) {
                Image image = selectedImages.get(i);
                File file = new File(image.getPath());
                if (!file.exists()) {
                    selectedImages.remove(i);
                    i--;
                }
            }
            getView().finishPickImages(selectedImages);
        }
    }

    void captureImage(Activity activity, BaseConfig config, int requestCode) {
        Context context = activity.getApplicationContext();
        Intent intent = getCameraModule().getCameraIntent(activity, config);
        if (intent == null) {
            Toast.makeText(context, context.getString(R.string.ef_error_create_image_file), Toast.LENGTH_LONG).show();
            return;
        }
        activity.startActivityForResult(intent, requestCode);
    }

    void finishCaptureImage(Context context, Intent data, final BaseConfig config) {
        getCameraModule().getImage(context, data, images -> {
            if (ConfigUtils.shouldReturn(config, true)) {
                getView().finishPickImages(images);
            } else {
                getView().showCapturedImage();
            }
        });
    }

    void abortCaptureImage() {
        getCameraModule().removeImage();
    }

    private void runOnUiIfAvailable(Runnable runnable) {
        main.post(() -> {
            if (isViewAttached()) {
                runnable.run();
            }
        });
    }
}
