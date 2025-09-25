package com.facecapturecamera.plugin;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.facecapturecamera.plugin.WhiteOverlayWithHole;
import com.getcapacitor.JSObject;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraXActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[] { Manifest.permission.CAMERA };

    private PreviewView previewView;
    private boolean facePresent = false;
    private Executor executor;

    private BroadcastReceiver captureReceiver;
    private FaceGuideView faceGuide;
    private ImageButton captureBtn;
    private TextView instructionText;
    private ImageButton flipBtn;
    private String currentCamera = "front"; // default
    private ProgressBar progressBar;
    private long lastFaceDetectedTime = 0;
    private static final long FACE_LOST_DEBOUNCE_MS = 1500; // adjust sensitivity here
    private Rect lastFaceBounds = null;
    private Camera camera;

    @Override
    protected void onResume() {
        super.onResume();
        captureReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if ("requestCapture".equals(intent.getAction())) {
                        captureImage();
                    }
                }
            };
        registerReceiver(captureReceiver, new IntentFilter("requestCapture"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(captureReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String cameraFromIntent = getIntent().getStringExtra("camera");
        if (cameraFromIntent != null) {
            currentCamera = cameraFromIntent;
        }

        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        previewView = new PreviewView(this);
        previewView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        rootLayout.addView(previewView);

        WhiteOverlayWithHole whiteOverlay = new WhiteOverlayWithHole(this);
        whiteOverlay.setLayoutParams(
            new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        );
        rootLayout.addView(whiteOverlay);

        faceGuide = new FaceGuideView(this);
        faceGuide.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.addView(faceGuide);

        instructionText = new TextView(this);
        instructionText.setText("Place your face within circle");
        instructionText.setTextColor(Color.BLACK);
        instructionText.setTextSize(16);
        instructionText.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.bottomMargin = 350; // Adjust this to position above the button
        textParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        instructionText.setLayoutParams(textParams);

        rootLayout.addView(instructionText);

        // method for progressbar
        progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE); // initially hidden

        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        progressParams.gravity = Gravity.CENTER; // center of screen

        progressBar.setLayoutParams(progressParams);
        rootLayout.addView(progressBar);

        // Close button (top-left)
        ImageButton closeBtn = new ImageButton(this);
        closeBtn.setImageResource(R.drawable.ic_close); // add your close icon drawable here
        closeBtn.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        closeParams.gravity = Gravity.TOP | Gravity.START;
        closeParams.setMargins(30, 30, 0, 0); // margin from top-left
        closeBtn.setLayoutParams(closeParams);
        closeBtn.setOnClickListener(v -> finish());
        rootLayout.addView(closeBtn);

        // Flip camera button (top-right)
        flipBtn = new ImageButton(this);
        flipBtn.setImageResource(R.drawable.ic_flip_camera);
        flipBtn.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams flipParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        flipParams.gravity = Gravity.TOP | Gravity.END;
        flipParams.setMargins(0, 30, 30, 0);
        flipBtn.setLayoutParams(flipParams);

        flipBtn.setOnClickListener(
            v -> {
                if (currentCamera.equals("front")) {
                    currentCamera = "back";
                } else {
                    currentCamera = "front";
                }
                startCamera(); // restart camera with new camera selector
            }
        );

        rootLayout.addView(flipBtn);

        captureBtn = new ImageButton(this);
        captureBtn.setImageResource(R.drawable.ic_camera);
        captureBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.round_button_background));
        captureBtn.setPadding(10, 10, 10, 10);
        captureBtn.setEnabled(false);
        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnParams.bottomMargin = 60;
        btnParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
        captureBtn.setLayoutParams(btnParams);
        captureBtn.setOnClickListener(
            v -> {
                progressBar.setVisibility(View.VISIBLE); // show loading spinner
                captureImage(); // your existing capture logic
            }
        );
        rootLayout.addView(captureBtn);
        setContentView(rootLayout);
        getWindow().setStatusBarColor(Color.WHITE);
        executor = ContextCompat.getMainExecutor(this);
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startCamera() { // proper working function
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(
            () -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    CameraSelector cameraSelector = "back".equals(currentCamera)
                        ? CameraSelector.DEFAULT_BACK_CAMERA
                        : CameraSelector.DEFAULT_FRONT_CAMERA;

                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());

                    // Set preview scale type to reduce zoomed/cropped look (especially front cam)
                    previewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);

                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                    FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();

                    FaceDetector detector = FaceDetection.getClient(options);

                    imageAnalysis.setAnalyzer(
                        executor,
                        imageProxy -> {
                            if (imageProxy == null || imageProxy.getImage() == null) {
                                imageProxy.close();
                                return;
                            }

                            InputImage image = InputImage.fromMediaImage(
                                imageProxy.getImage(),
                                imageProxy.getImageInfo().getRotationDegrees()
                            );

                            detector
                                .process(image)
                                .addOnSuccessListener(
                                    faces -> {
                                        Log.d("FaceCapture", "Faces found: " + faces.size());
                                        if (!faces.isEmpty()) {
                                            if (!facePresent) {
                                                //             if (faces.size() == 1) {
                                                Face face = faces.get(0); // Assuming one face
                                                Rect faceBounds = face.getBoundingBox();
                                                lastFaceBounds = faceBounds; // Save to a class-level variable
                                                facePresent = true;
                                                runOnUiThread(
                                                    () -> {
                                                        captureBtn.setEnabled(true);
                                                        captureBtn.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                                                        faceGuide.setBorderColor(Color.GREEN);
                                                        instructionText.setText("Capture now");
                                                    }
                                                );
                                                if (FaceCapturingPlugin.instance != null) {
                                                    FaceCapturingPlugin.instance.notifyFaceDetected();
                                                    Log.d("FaceCapture", "Face Detected Event Fired");
                                                }
                                            } else {
                                                if (facePresent || faces.size() > 1) {
                                                    facePresent = false;
                                                    runOnUiThread(
                                                        () -> {
                                                            captureBtn.setEnabled(false);
                                                            captureBtn.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                                                            faceGuide.setBorderColor(Color.RED);

                                                            if (faces.size() > 1) {
                                                                instructionText.setText("Only one face allowed");
                                                            } else {
                                                                instructionText.setText("Place your face within circle");
                                                            }
                                                        }
                                                    );
                                                }
                                            }
                                            lastFaceDetectedTime = System.currentTimeMillis(); // update timestamp
                                        } else {
                                            if (facePresent && System.currentTimeMillis() - lastFaceDetectedTime > FACE_LOST_DEBOUNCE_MS) {
                                                facePresent = false;
                                                runOnUiThread(
                                                    () -> {
                                                        captureBtn.setEnabled(false);
                                                        captureBtn.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                                                        faceGuide.setBorderColor(Color.RED);
                                                        instructionText.setText("Place your face within circle");
                                                    }
                                                );
                                                if (FaceCapturingPlugin.instance != null) {
                                                    FaceCapturingPlugin.instance.notifyFaceLost();
                                                    Log.d("FaceCapture", "Face Lost Event Fired");
                                                }
                                            }
                                        }
                                    }
                                )
                                .addOnFailureListener(e -> Log.e("FaceCapture", "Detection failed", e))
                                .addOnCompleteListener(task -> imageProxy.close());
                        }
                    );

                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

                    // Log zoom state changes
                    camera
                        .getCameraInfo()
                        .getZoomState()
                        .observe(
                            this,
                            zoomState -> {
                                float minZoom = zoomState.getMinZoomRatio();
                                float maxZoom = zoomState.getMaxZoomRatio();
                                float currentZoom = zoomState.getZoomRatio();
                                Log.d("FaceCapture", "Zoom state - min: " + minZoom + ", max: " + maxZoom + ", current: " + currentZoom);
                            }
                        );

                    // For front camera, set zoom to minimum (delayed to ensure applied)
                    if ("front".equals(currentCamera)) {
                        new Handler(Looper.getMainLooper())
                            .postDelayed(
                                () -> {
                                    camera.getCameraControl().setZoomRatio(1.0f);
                                    Log.d("FaceCapture", "Set front camera zoom to 1.0f after delay");
                                },
                                200
                            );
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            },
            executor
        );
    }

    private void captureImage() {
        runOnUiThread(
            () -> {
                progressBar.setVisibility(View.VISIBLE);
                captureBtn.setEnabled(false);
            }
        );

        Bitmap bitmap = previewView.getBitmap();
        if (bitmap != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            byte[] bytes = outputStream.toByteArray();
            String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);

            if (FaceCapturingPlugin.instance != null) {
                FaceCapturingPlugin.instance.sendCaptureComplete(base64);
            }

            // After sending data, close this activity (camera plugin)
            runOnUiThread(
                () -> {
                    finish();
                }
            );
        } else {
            // In case bitmap is null, hide loader and re-enable button
            runOnUiThread(
                () -> {
                    progressBar.setVisibility(View.GONE);
                    captureBtn.setEnabled(true);
                }
            );
        }
    }
}
