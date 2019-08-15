package com.pivoto.facedetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;


@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements Callback,
        FaceDetectionListener, PreviewCallback {

    public static final String TAG = "Face:Activity";

    private final static String[] NECESSARY_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };

    private final static int PERMISSION_REQUEST_CODE = 1;

    private int mCameraId;

    @Nullable
    private Camera mCamera;

    private int mCameraFacing;

    private int mOrientation;

    private SurfaceHolder mSurfaceHolder;

    private FaceDebugView mFaceDebugView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCameraFacing = CameraInfo.CAMERA_FACING_FRONT;
        mOrientation = 90;

        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == mCameraFacing) {
                mCameraId = i;
                break;
            }
        }

        setContentView(R.layout.activity_main);

        final SurfaceView surfaceView = findViewById(R.id.preview_surface_view);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        mFaceDebugView = findViewById(R.id.face_debug_view);
        mFaceDebugView.setOrientation(mOrientation);
        mFaceDebugView.setCameraFacing(mCameraFacing);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {

        boolean hasAllPermissions = true;

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false;
                break;
            }
        }

        if (hasAllPermissions) {
            openCamera();
            startPreview();
        } else {
            Toast.makeText(this, "Missing permissions", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCameraPermission();
    }

    private void openCameraPermission() {
        boolean hasAllPermissions = true;
        for (String permission : NECESSARY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false;
                break;
            }
        }

        if (hasAllPermissions) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this, NECESSARY_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    private void openCamera() {
        if (mCamera == null) {
            mCamera = Camera.open(mCameraId);
            mCamera.setFaceDetectionListener(this);
            mCamera.setDisplayOrientation(mOrientation);
        } else {
            Log.e(TAG, "Error opening camera. It seems it is already open");
        }
    }

    private void startPreview() {
        if(mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                mCamera.startFaceDetection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopPreview() {
        if(mCamera != null) {
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
        }
    }

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        startPreview();
    }

    @Override
    public void surfaceChanged(final SurfaceHolder surfaceHolder, final int format, final int width,
            final int height) {

        if (mSurfaceHolder.getSurface() == null || mCamera == null) {
            return;
        }

        stopPreview();

        mSurfaceHolder = surfaceHolder;

        startPreview();
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onFaceDetection(final Face[] faces, final Camera camera) {
        mFaceDebugView.setFacePosition(faces);
    }

    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        // Nothing to do
    }
}
