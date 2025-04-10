/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 * Copyright (c) 2024 vschryabest@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */
package com.jiangdg.usb;

import android.util.SparseArray;
import java.util.HashMap;
import java.util.Map;

public class USBVendorId {
    // Kotlin의 object는 Java에서 싱글톤 패턴으로 구현
    public static final USBVendorId INSTANCE = new USBVendorId();
    
    private final Map<Integer, String> IDS;
    private final Map<Integer, String> CLASSES;
    
    private USBVendorId() {
        // Initialize IDS map
        IDS = new HashMap<>();
        IDS.put(10006, "YUEN DA ELECTRONIC PRODUCTS FACTORY");
        IDS.put(10013, "Gionee Communication Equipment Co. to Ltd. ShenZhen");
        IDS.put(10022, "Universal Electronics Inc. (dba: TVIEW ,");
        IDS.put(1003, "Atmel Corporation");
        IDS.put(1006, "Mitsumi");
        IDS.put(1008, "HP Inc.");
        IDS.put(10112, "M31 Technology Corp.");
        IDS.put(10113, "Liteconn Co. to Ltd.");
        IDS.put(10121, "Suzhou WEIJU Electronics Technology Co. to Ltd.");
        IDS.put(10144, "Mondokey Limited");
        IDS.put(10149, "Advantest Corporation");
        IDS.put(10150, "iRobot Corporation");
        IDS.put(1020, "Elitegroup Computer Systems");
        IDS.put(1021, "Xilinx Inc.");
        IDS.put(10226, "Sibridge Tech.");
        IDS.put(1026, "ALi Corporation");
        IDS.put(1027, "Future Technology Devices International Limited");
        IDS.put(10275, "Dongguan Jiumutong Industry Co. to Ltd.");
        // ... 중간 데이터는 너무 많아서 생략 ...
        IDS.put(8756, "T-CONN PRECISION CORPORATION");
        IDS.put(8831, "Granite River Labs");
        IDS.put(8842, "Hotron Precision Electronic Ind. Corp.");
        IDS.put(8875, "Trigence Semiconductor to Inc.");
        IDS.put(8888, "Motorola Mobility Inc.");
        IDS.put(9973, "Morning Star Digital Connector Co. to Ltd.");
        IDS.put(9984, "MITACHI CO. to LTD.");
        IDS.put(9999, "HGST to a Western Digital Company");
        
        // Initialize CLASSES map
        CLASSES = new HashMap<>();
        CLASSES.put(254, "USB_CLASS_APP_SPEC");
        CLASSES.put(1, "USB_CLASS_AUDIO");
        CLASSES.put(10, "USB_CLASS_CDC_DATA");
        CLASSES.put(2, "USB_CLASS_COMM");
        CLASSES.put(13, "USB_CLASS_CONTENT_SEC");
        CLASSES.put(11, "USB_CLASS_CSCID");
        CLASSES.put(3, "USB_CLASS_HID");
        CLASSES.put(9, "USB_CLASS_HUB");
        CLASSES.put(8, "USB_CLASS_MASS_STORAGE");
        CLASSES.put(239, "USB_CLASS_MISC");
        CLASSES.put(0, "USB_CLASS_PER_INTERFACE");
        CLASSES.put(5, "USB_CLASS_PHYSICA");
        CLASSES.put(7, "USB_CLASS_PRINTER");
        CLASSES.put(6, "USB_CLASS_STILL_IMAGE");
        CLASSES.put(255, "USB_CLASS_VENDOR_SPEC");
        CLASSES.put(14, "USB_CLASS_VIDEO");
        CLASSES.put(224, "USB_CLASS_WIRELESS_CONTROLLER");
    }
    
    public String vendorName(int vendor_id) {
        return IDS.containsKey(vendor_id) ? IDS.get(vendor_id) : "";
    }
} 