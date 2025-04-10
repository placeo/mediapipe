// Copyright 2019 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.mediapipe.apps.custom.simple;

import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.SurfaceTexture;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.glutil.EglManager;
import java.lang.ref.WeakReference;
import com.jiangdg.ausbc.camera.CameraUVC;
import com.jiangdg.ausbc.callback.IPreviewDataCallBack;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbEndpoint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.app.PendingIntent;
import android.hardware.usb.UsbConstants;
import java.util.HashMap;

/** Main activity of MediaPipe basic app. */
public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  private static final String ACTION_USB_PERMISSION = "com.google.mediapipe.apps.custom.simple.USB_PERMISSION";

  // Flips the camera-preview frames vertically by default, before sending them into FrameProcessor
  // to be processed in a MediaPipe graph, and flips the processed frames back when they are
  // displayed. This maybe needed because OpenGL represents images assuming the image origin is at
  // the bottom-left corner, whereas MediaPipe in general assumes the image origin is at the
  // top-left corner.
  // NOTE: use "flipFramesVertically" in manifest metadata to override this behavior.
  private static final boolean FLIP_FRAMES_VERTICALLY = true;

  // Number of output frames allocated in ExternalTextureConverter.
  // NOTE: use "converterNumBuffers" in manifest metadata to override number of buffers. For
  // example, when there is a FlowLimiterCalculator in the graph, number of buffers should be at
  // least `max_in_flight + max_in_queue + 1` (where max_in_flight and max_in_queue are used in
  // FlowLimiterCalculator options). That's because we need buffers for all the frames that are in
  // flight/queue plus one for the next frame from the camera.
  private static final int NUM_BUFFERS = 2;

  static {
    // Load all native libraries needed by the app.
    System.loadLibrary("mediapipe_jni");
    System.loadLibrary("opencv_java4");
  }

  // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
  // frames onto a {@link Surface}.
  protected FrameProcessor processor;
  // Handles camera access via the {@link CameraX} Jetpack support library.
  protected CameraXPreviewHelper cameraHelper;

  // {@link SurfaceTexture} where the camera-preview frames can be accessed.
  private SurfaceTexture previewFrameTexture;
  // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
  private SurfaceView previewDisplayView;

  // Creates and manages an {@link EGLContext}.
  private EglManager eglManager;
  // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
  // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
  private ExternalTextureConverter converter;

  // ApplicationInfo for retrieving metadata defined in the manifest.
  private ApplicationInfo applicationInfo;

  // Progress dialog to show for the actions that must be executed on non-UI thread.
  private ProgressDialog progressDialog;

  private CameraUVC cameraUVC;
  private boolean isUsbCameraActive = false;

  private UsbManager usbManager;
  private UsbDevice usbDevice;
  private UsbDeviceConnection usbConnection;
  private UsbInterface usbInterface;
  private UsbEndpoint usbEndpoint;
  private Thread cameraThread;
  private volatile boolean stopCamera = false;

  private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (ACTION_USB_PERMISSION.equals(action)) {
        synchronized (this) {
          UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            if (device != null) {
              setupUsbCamera(device);
            }
          }
        }
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    try {
      applicationInfo =
          getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
    } catch (NameNotFoundException e) {
      Log.e(TAG, "Cannot find application info: " + e);
    }

    previewDisplayView = new SurfaceView(this);
    setupPreviewDisplayView();

    // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
    // binary graphs.
    AndroidAssetUtil.initializeNativeAssetManager(this);
    eglManager = new EglManager(null);
    processor =
        new FrameProcessor(
            this,
            eglManager.getNativeContext(),
            applicationInfo.metaData.getString("binaryGraphName"),
            applicationInfo.metaData.getString("inputVideoStreamName"),
            applicationInfo.metaData.getString("outputVideoStreamName"));
    processor
        .getVideoSurfaceOutput()
        .setFlipY(
            applicationInfo.metaData.getBoolean("flipFramesVertically", FLIP_FRAMES_VERTICALLY));

    PermissionHelper.checkAndRequestCameraPermissions(this);

    // USB 카메라 초기화
    usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    registerReceiver(usbReceiver, filter);

    // USB 장치 검색
    HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
    if (!deviceList.isEmpty()) {
      UsbDevice device = deviceList.values().iterator().next();
      PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
      usbManager.requestPermission(device, permissionIntent);
    }

    cameraUVC = new CameraUVC(this);
    cameraUVC.setPreviewCallback(new IPreviewDataCallBack() {
      @Override
      public void onPreviewData(byte[] data, int width, int height) {
        if (processor != null) {
          Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
          processor.onNewFrame(bitmap, System.currentTimeMillis());
        }
      }
    });
  }

  // Used to obtain the content view for this application. If you are extending this class, and
  // have a custom layout, override this method and return the custom layout.
  protected int getContentViewLayoutResId() {
    return R.layout.activity_main;
  }

  @Override
  protected void onResume() {
    super.onResume();
    converter =
        new ExternalTextureConverter(
            eglManager.getContext(),
            applicationInfo.metaData.getInt("converterNumBuffers", NUM_BUFFERS));
    converter.setFlipY(
        applicationInfo.metaData.getBoolean("flipFramesVertically", FLIP_FRAMES_VERTICALLY));
    converter.setConsumer(processor);
    
    if (usbDevice != null) {
      setupUsbCamera(usbDevice);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    final int id = item.getItemId();
    if (id == R.id.action_exit) {
      progressDialog = new ProgressDialog(this);
      progressDialog.setMessage("Closing graph, waiting until done & exiting...");
      progressDialog.setCancelable(false);

      new CloseProcessorAndExitTask(
              new WeakReference<>(progressDialog), new WeakReference<>(processor))
          .execute();
      return true;
    }
    return false;
  }

  @Override
  protected void onPause() {
    super.onPause();
    converter.close();
    
    stopUsbCamera();
    
    // Hide preview display until we re-open the camera again.
    previewDisplayView.setVisibility(View.GONE);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  protected void onCameraStarted(SurfaceTexture surfaceTexture) {
    previewFrameTexture = surfaceTexture;
    // Make the display view visible to start showing the preview. This triggers the
    // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
    previewDisplayView.setVisibility(View.VISIBLE);
  }

  protected Size cameraTargetResolution() {
    return null; // No preference and let the camera (helper) decide.
  }

  public void startCamera() {
    cameraHelper = new CameraXPreviewHelper();
    previewFrameTexture = converter.getSurfaceTexture();
    cameraHelper.setOnCameraStartedListener(
        surfaceTexture -> {
          onCameraStarted(surfaceTexture);
        });
    CameraHelper.CameraFacing cameraFacing =
        applicationInfo.metaData.getBoolean("cameraFacingFront", false)
            ? CameraHelper.CameraFacing.FRONT
            : CameraHelper.CameraFacing.BACK;
    cameraHelper.startCamera(
        this, cameraFacing, previewFrameTexture, cameraTargetResolution());
  }

  protected Size computeViewSize(int width, int height) {
    return new Size(width, height);
  }

  protected void onPreviewDisplaySurfaceChanged(
      SurfaceHolder holder, int format, int width, int height) {
    // (Re-)Compute the ideal size of the camera-preview display (the area that the
    // camera-preview frames get rendered onto, potentially with scaling and rotation)
    // based on the size of the SurfaceView that contains the display.
    Size viewSize = computeViewSize(width, height);
    Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
    boolean isCameraRotated = cameraHelper.isCameraRotated();

    // Configure the output width and height as the computed display size.
    converter.setDestinationSize(
        isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
        isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
  }

  private void setupPreviewDisplayView() {
    previewDisplayView.setVisibility(View.GONE);
    ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
    viewGroup.addView(previewDisplayView);

    previewDisplayView
        .getHolder()
        .addCallback(
            new SurfaceHolder.Callback() {
              @Override
              public void surfaceCreated(SurfaceHolder holder) {
                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
              }

              @Override
              public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                onPreviewDisplaySurfaceChanged(holder, format, width, height);
              }

              @Override
              public void surfaceDestroyed(SurfaceHolder holder) {
                processor.getVideoSurfaceOutput().setSurface(null);
              }
            });
  }

  private void setupUsbCamera(UsbDevice device) {
    if (device == null) {
        Log.e(TAG, "USB device is null");
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

        startUsbCamera();
    } catch (Exception e) {
        Log.e(TAG, "Error setting up USB camera: " + e.getMessage());
        stopUsbCamera();
    }
  }

  private void startUsbCamera() {
    if (isUsbCameraActive) {
        return;
    }

    isUsbCameraActive = true;
    stopCamera = false;
    cameraThread = new Thread(() -> {
        byte[] buffer = new byte[16384];
        while (!stopCamera && usbConnection != null && usbEndpoint != null) {
            int bytesRead = usbConnection.bulkTransfer(usbEndpoint, buffer, buffer.length, 5000);
            if (bytesRead > 0 && cameraUVC != null) {
                cameraUVC.openCamera(usbDevice);
            }
        }
    });
    cameraThread.start();
  }

  private void stopUsbCamera() {
    stopCamera = true;
    isUsbCameraActive = false;

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
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(usbReceiver);
  }

  private static class CloseProcessorAndExitTask extends AsyncTask<Void, Void, Boolean> {
    private final WeakReference<ProgressDialog> progressDialogRef;
    private final WeakReference<FrameProcessor> frameProcessorRef;

    public CloseProcessorAndExitTask(
        WeakReference<ProgressDialog> progressDialogRef,
        WeakReference<FrameProcessor> frameProcessorRef) {
      this.progressDialogRef = progressDialogRef;
      this.frameProcessorRef = frameProcessorRef;
    }

    @Override
    protected void onPreExecute() {
      ProgressDialog progressDialog = progressDialogRef.get();
      if (progressDialog != null) {
        progressDialog.show();
      }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
      FrameProcessor frameProcessor = frameProcessorRef.get();
      if (frameProcessor != null) {
        frameProcessor.close();
        return true;
      }
      return false;
    }

    @Override
    protected void onPostExecute(Boolean isProcessorClosed) {
      if (!isProcessorClosed) {
        throw new IllegalStateException("Processor was not closed.");
      }
      ProgressDialog progressDialog = progressDialogRef.get();
      if (progressDialog != null && progressDialog.isShowing()) {
        progressDialog.dismiss();
        progressDialog.getOwnerActivity().finish();
      }
    }
  }
}
