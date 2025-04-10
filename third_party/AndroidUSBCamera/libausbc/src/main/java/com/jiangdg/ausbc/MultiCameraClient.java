package com.jiangdg.ausbc;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.jiangdg.ausbc.callback.ICameraStateCallBack;
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack;
import com.jiangdg.ausbc.callback.IPreviewDataCallBack;
import com.jiangdg.ausbc.camera.CameraUVC;

import java.util.HashMap;
import java.util.Map;

public class MultiCameraClient {
    private static final String TAG = "MultiCameraClient";

    private final Context context;
    private final Map<String, CameraUVC> cameraMap;
    private IDeviceConnectCallBack deviceCallback;
    private ICameraStateCallBack stateCallback;
    private IPreviewDataCallBack previewCallback;

    public MultiCameraClient(Context context) {
        this.context = context;
        this.cameraMap = new HashMap<>();
    }

    public void setDeviceCallback(IDeviceConnectCallBack callback) {
        this.deviceCallback = callback;
    }

    public void setStateCallback(ICameraStateCallBack callback) {
        this.stateCallback = callback;
    }

    public void setPreviewCallback(IPreviewDataCallBack callback) {
        this.previewCallback = callback;
    }

    public void registerCamera(UsbDevice device) {
        if (device == null) {
            Log.e(TAG, "Cannot register null device");
            return;
        }

        String deviceKey = device.getDeviceName();
        if (!cameraMap.containsKey(deviceKey)) {
            CameraUVC camera = new CameraUVC(context);
            camera.setStateCallback(new ICameraStateCallBack() {
                @Override
                public void onCameraOpened() {
                    if (stateCallback != null) {
                        stateCallback.onCameraOpened();
                    }
                }

                @Override
                public void onCameraClosed() {
                    if (stateCallback != null) {
                        stateCallback.onCameraClosed();
                    }
                }

                @Override
                public void onCameraError(Exception e) {
                    if (stateCallback != null) {
                        stateCallback.onCameraError(e);
                    }
                }
            });

            camera.setPreviewCallback(previewCallback);
            cameraMap.put(deviceKey, camera);

            if (deviceCallback != null) {
                deviceCallback.onAttachDev(device);
            }
        }
    }

    public void unregisterCamera(UsbDevice device) {
        if (device == null) {
            return;
        }

        String deviceKey = device.getDeviceName();
        CameraUVC camera = cameraMap.remove(deviceKey);
        if (camera != null) {
            camera.closeCamera();
            if (deviceCallback != null) {
                deviceCallback.onDetachDec(device);
            }
        }
    }

    public void openCamera(UsbDevice device) {
        if (device == null) {
            return;
        }

        String deviceKey = device.getDeviceName();
        CameraUVC camera = cameraMap.get(deviceKey);
        if (camera != null) {
            camera.openCamera(device);
            if (deviceCallback != null) {
                deviceCallback.onConnectDev(device, true);
            }
        }
    }

    public void closeCamera(UsbDevice device) {
        if (device == null) {
            return;
        }

        String deviceKey = device.getDeviceName();
        CameraUVC camera = cameraMap.get(deviceKey);
        if (camera != null) {
            camera.closeCamera();
            if (deviceCallback != null) {
                deviceCallback.onDisConnectDec(device);
            }
        }
    }

    public void closeAllCameras() {
        for (Map.Entry<String, CameraUVC> entry : cameraMap.entrySet()) {
            CameraUVC camera = entry.getValue();
            if (camera != null) {
                camera.closeCamera();
            }
        }
        cameraMap.clear();
    }
} 