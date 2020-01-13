package com.imagepicker.features.cameraonly;

import android.os.Parcel;
import android.os.Parcelable;

import com.imagepicker.features.common.BaseConfig;

public class CameraOnlyConfig extends BaseConfig {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public CameraOnlyConfig() {
    }

    private CameraOnlyConfig(Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<CameraOnlyConfig> CREATOR = new Parcelable.Creator<CameraOnlyConfig>() {
        @Override
        public CameraOnlyConfig createFromParcel(Parcel source) {
            return new CameraOnlyConfig(source);
        }

        @Override
        public CameraOnlyConfig[] newArray(int size) {
            return new CameraOnlyConfig[size];
        }
    };
}
