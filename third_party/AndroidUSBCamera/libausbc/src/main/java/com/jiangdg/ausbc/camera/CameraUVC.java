package com.jiangdg.ausbc.camera;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbConstants;
import android.util.Log;

import com.jiangdg.ausbc.callback.ICameraStateCallBack;
import com.jiangdg.ausbc.callback.IPreviewDataCallBack;

public class CameraUVC {
    private static final String TAG = "CameraUVC";
    private static final int BUFFER_SIZE = 16384;

    private final Context context;
    private final UsbManager usbManager;
    private UsbDevice usbDevice;
    private UsbDeviceConnection usbConnection;
    private UsbInterface usbInterface;
    private UsbEndpoint usbEndpoint;
    private Thread cameraThread;
    private volatile boolean isRunning;

    private ICameraStateCallBack stateCallback;
    private IPreviewDataCallBack previewCallback;

    public CameraUVC(Context context) {
        this.context = context;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    public void setStateCallback(ICameraStateCallBack callback) {
        this.stateCallback = callback;
    }

    public void setPreviewCallback(IPreviewDataCallBack callback) {
        this.previewCallback = callback;
    }

    public void openCamera(UsbDevice device) {
        if (device == null) {
            if (stateCallback != null) {
                stateCallback.onCameraError(new Exception("USB device is null"));
            }
            return;
        }

        try {
            usbDevice = device;
            usbConnection = usbManager.openDevice(usbDevice);
            if (usbConnection == null) {
                throw new Exception("Could not open USB device");
            }

            usbInterface = usbDevice.getInterface(0);
            if (!usbConnection.claimInterface(usbInterface, true)) {
                throw new Exception("Could not claim USB interface");
            }

            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(i);
                if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                    endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    usbEndpoint = endpoint;
                    break;
                }
            }

            if (usbEndpoint == null) {
                throw new Exception("Could not find USB endpoint");
            }

            startPreview();
            if (stateCallback != null) {
                stateCallback.onCameraOpened();
            }
        } catch (Exception e) {
            if (stateCallback != null) {
                stateCallback.onCameraError(e);
            }
            closeCamera();
        }
    }

    private void startPreview() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        cameraThread = new Thread(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (isRunning) {
                int bytesRead = usbConnection.bulkTransfer(usbEndpoint, buffer, buffer.length, 5000);
                if (bytesRead > 0 && previewCallback != null) {
                    previewCallback.onPreviewData(buffer, 0, 0); // Width and height need to be determined from the data
                }
            }
        });
        cameraThread.start();
    }

    public void closeCamera() {
        isRunning = false;
        if (cameraThread != null) {
            try {
                cameraThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping camera thread: " + e);
            }
            cameraThread = null;
        }

        if (usbConnection != null) {
            if (usbInterface != null) {
                usbConnection.releaseInterface(usbInterface);
                usbInterface = null;
            }
            usbConnection.close();
            usbConnection = null;
        }

        usbDevice = null;
        usbEndpoint = null;

        if (stateCallback != null) {
            stateCallback.onCameraClosed();
        }
    }
} 