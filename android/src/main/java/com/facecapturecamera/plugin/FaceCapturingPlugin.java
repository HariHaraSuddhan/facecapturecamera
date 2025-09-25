package com.facecapturecamera.plugin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "FaceCapturing")
public class FaceCapturingPlugin extends Plugin {

    private static final int REQUEST_CODE = 9001;
    private FaceCapturing implementation = new FaceCapturing();
    public static FaceCapturingPlugin instance;

    @PluginMethod
    public void startCamera(PluginCall call) {
        Activity activity = getActivity();
        Intent intent = new Intent(activity, CameraXActivity.class);
        String camera = call.getString("camera", "front");
        intent.putExtra("camera", camera);
        startActivityForResult(call, intent, REQUEST_CODE);
    }

    @PluginMethod
    public void stopCamera(PluginCall call) {
        // Not implemented yet - optional
        call.resolve();
    }

    @PluginMethod
    public void capture(PluginCall call) {
        Intent intent = new Intent("requestCapture");
        getContext().sendBroadcast(intent);
        call.resolve();
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && data != null) {
            String base64 = data.getStringExtra("imageBase64");
            JSObject js = new JSObject();
            js.put("imageBase64", base64);
            notifyListeners("captureComplete", js);
        }
    }

    @Override
    public void load() {
        instance = this;
    }

    public void notifyFaceDetected() {
        notifyListeners("faceDetected", null);
    }

    public void notifyFaceLost() {
        notifyListeners("faceLost", null);
    }

    public void sendCaptureComplete(String base64) {
        JSObject js = new JSObject();
        js.put("imageBase64", base64);
        notifyListeners("captureComplete", js);
    }
}
