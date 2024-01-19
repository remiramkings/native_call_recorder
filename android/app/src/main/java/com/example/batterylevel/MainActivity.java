package com.example.batterylevel;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "samples.flutter.dev/battery";
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler(
                (call, result) -> {
                    // This method is invoked on the main thread.
                    if (call.method.equals("getBatteryLevel")) {
                        int batteryLevel = getBatteryLevel();

                        if (batteryLevel != -1) {
                        result.success(batteryLevel);
                        } else {
                        result.error("UNAVAILABLE", "Battery level not available.", null);
                        }
                    } else if(call.method.equals("recordCall")){
                        result.success(recordCall());
                    } else {
                        result.notImplemented();
                    }
                }
            );
    }

    private int getBatteryLevel() {
        int batteryLevel = -1;
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent intent = new ContextWrapper(getApplicationContext()).
                registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            batteryLevel = (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }

        return batteryLevel;
    }

    private String recordCall(){
        resolveMediaRecorderPermissions();
        CallRecorderService service = CallRecorderService
                .getInstance();
        if(!service.isRecordStarted()){
            service.startRecording(getFile());
        } else {
            service.stopRecording();
        }

        // Read MIUI files
        File miuiFileDir = new File("/storage/emulated/0/MIUI/sound_recorder/call_rec");
        if(miuiFileDir.exists()){
            System.out.println("MIUI record dir is exists");
            if(miuiFileDir.canRead()){
                System.out.println("MIUI record dir is readable");
                List<File> files = Arrays.asList(miuiFileDir.listFiles());
                System.out.println("Files listed: " + files.size());
                files.stream()
                        .forEach(file -> {
                            System.out.println("File: " + file.getName());
                        });
                System.out.println("File scan completed");
            }
        }

        return "Started call recording";
    }

    private File getFile(){
        String fileName = createFileName();
        return new File(
            setupRecordingPath(),
            fileName
        );
    }

    private String setupRecordingPath(){
        String musicPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                        .getAbsolutePath();
        File appRecordDir = new File(musicPath +
                File.separator +
                getApplicationContext().getPackageName());
        if(!appRecordDir.exists()){
            appRecordDir.mkdir();
        }

        return appRecordDir.getAbsolutePath();
    }

    private String createFileName(){
        LocalDateTime localDateTime = LocalDateTime.now();
        return "recorded-" + localDateTime.format(
                DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
                + ".amr";
    }

    private void resolveMediaRecorderPermissions(){
        if (ActivityCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ||ActivityCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
        }
    }


}
