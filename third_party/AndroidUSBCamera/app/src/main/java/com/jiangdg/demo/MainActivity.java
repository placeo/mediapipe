package com.jiangdg.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import com.jiangdg.ausbc.utils.Utils;
import com.jiangdg.demo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private PowerManager.WakeLock mWakeLock;
    private ActivityMainBinding viewBinding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        int usbDeviceId = getIntent().getIntExtra(KEY_USB_DEVICE, -1);
        replaceDemoFragment(DemoFragment.newInstance(usbDeviceId));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWakeLock = Utils.INSTANCE.wakeLock(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWakeLock != null) {
            Utils.INSTANCE.wakeUnLock(mWakeLock);
        }
    }

    private void replaceDemoFragment(Fragment fragment) {
        int hasCameraPermission = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);
        int hasStoragePermission = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (hasCameraPermission != PermissionChecker.PERMISSION_GRANTED || hasStoragePermission != PermissionChecker.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, R.string.permission_tip, Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                REQUEST_CAMERA
            );
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA:
                int hasCameraPermission = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);
                if (hasCameraPermission == PermissionChecker.PERMISSION_DENIED) {
                    Toast.makeText(this, R.string.permission_tip, Toast.LENGTH_LONG).show();
                    return;
                }
                replaceDemoFragment(new DemoFragment());
                break;

            case REQUEST_STORAGE:
                int hasStoragePermission = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasStoragePermission == PermissionChecker.PERMISSION_DENIED) {
                    Toast.makeText(this, R.string.permission_tip, Toast.LENGTH_LONG).show();
                    return;
                }
                // TODO: Handle storage permission granted
                break;

            default:
                break;
        }
    }

    public static Intent newInstance(Context context, int usbDeviceId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(KEY_USB_DEVICE, usbDeviceId);
        return intent;
    }

    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_STORAGE = 1;
    public static final String KEY_USB_DEVICE = "usbDeviceId";
}