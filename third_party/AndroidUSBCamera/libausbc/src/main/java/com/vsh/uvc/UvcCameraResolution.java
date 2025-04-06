/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2024 vshcryabets@gmail.com
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
 *  Files in the libjpeg-turbo, libusb, libuvc folder
 *  may have a different license, see the respective files.
 */
package com.vsh.uvc;

import java.util.List;
import java.util.Objects;

public class UvcCameraResolution {
    private final int id;
    private final int subtype;
    private final int frameIndex;
    private final int width;
    private final int height;
    private final List<Integer> frameIntervals; // in 100ns units

    public UvcCameraResolution(int id, int subtype, int frameIndex, int width, int height, List<Integer> frameIntervals) {
        this.id = id;
        this.subtype = subtype;
        this.frameIndex = frameIndex;
        this.width = width;
        this.height = height;
        this.frameIntervals = frameIntervals;
    }

    public int getId() {
        return id;
    }

    public int getSubtype() {
        return subtype;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Integer> getFrameIntervals() {
        return frameIntervals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UvcCameraResolution that = (UvcCameraResolution) o;
        return id == that.id &&
                subtype == that.subtype &&
                frameIndex == that.frameIndex &&
                width == that.width &&
                height == that.height &&
                Objects.equals(frameIntervals, that.frameIntervals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subtype, frameIndex, width, height, frameIntervals);
    }

    @Override
    public String toString() {
        return "UvcCameraResolution{" +
                "id=" + id +
                ", subtype=" + subtype +
                ", frameIndex=" + frameIndex +
                ", width=" + width +
                ", height=" + height +
                ", frameIntervals=" + frameIntervals +
                '}';
    }
} 