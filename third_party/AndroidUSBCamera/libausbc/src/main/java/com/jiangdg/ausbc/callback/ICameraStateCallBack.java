package com.jiangdg.ausbc.callback;

public interface ICameraStateCallBack {
    void onCameraOpened();
    void onCameraClosed();
    void onCameraError(Exception e);
} 