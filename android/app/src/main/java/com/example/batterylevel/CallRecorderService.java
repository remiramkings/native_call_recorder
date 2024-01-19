package com.example.batterylevel;

import android.media.MediaRecorder;

import java.io.File;

public class CallRecorderService {
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_CALL;
    private static final int SAMPLING_RATE = 8000;
    private static final int ENCODING_BIT_RATE = 12200;
    private static final int OUTPUT_FORMAT = MediaRecorder.OutputFormat.THREE_GPP;
    private static final int ENCODER = MediaRecorder.AudioEncoder.AMR_NB;

    private MediaRecorder recorder;

    private boolean isRecordStarted = false;

    private static CallRecorderService instance;

    public CallRecorderService() {
    }

    public static CallRecorderService getInstance(){
        if(instance == null) {
            instance = new CallRecorderService();
        }

        return instance;
    }

    public boolean isRecordStarted() {
        return isRecordStarted;
    }

    private MediaRecorder.OnInfoListener getInfoListener() {
        return new MediaRecorder.OnInfoListener() {
            public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
                System.out.println("OnInfoListener " + arg1 + "," + arg2);
            }
        };
    }

    public void startRecording(File file) {
        this.recorder = new MediaRecorder();
        try {
            recorder.reset();
            recorder.setAudioSource(AUDIO_SOURCE);
            recorder.setAudioSamplingRate(SAMPLING_RATE);
            recorder.setAudioEncodingBitRate(ENCODING_BIT_RATE);
            recorder.setOutputFormat(OUTPUT_FORMAT);
            recorder.setAudioEncoder(ENCODER);
            recorder.setOutputFile(file);

            recorder.setOnInfoListener(getInfoListener());

            recorder.prepare();
            // Sometimes prepare takes some time to complete
            Thread.sleep(2000);
            recorder.start();
            System.out.println("Recording started: " + file.getAbsolutePath());
            isRecordStarted = true;

        } catch (Exception e) {
            e.printStackTrace();
            isRecordStarted = false;
            System.out.println("Recorder Error: " + e.getMessage());
        }
    }

    public void stopRecording(){
        if(isRecordStarted){
            recorder.stop();
            recorder.release();
        }
    }
}
