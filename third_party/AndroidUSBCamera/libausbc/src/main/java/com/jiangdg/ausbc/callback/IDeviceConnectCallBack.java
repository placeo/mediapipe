package com.jiangdg.ausbc.callback;

import android.hardware.usb.UsbDevice;

public interface IDeviceConnectCallBack {
    void onAttachDev(UsbDevice device);
    void onDetachDec(UsbDevice device);
    void onConnectDev(UsbDevice device, boolean isConnected);
    void onDisConnectDec(UsbDevice device);
} 