package com.xiaolin.homework;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class RecordUtil {

    private MediaRecorder recorder;

    private boolean isRecordStarted;

    RecordUtil() {
        recorder = new MediaRecorder();
    }

    private boolean prepareRecorder(String fileName) {
        try {
            File dir = new File(Environment.getExternalStorageDirectory().getPath(), "xxxx-records");
            if (!dir.exists()) {
                dir.mkdir();
            }
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(File.createTempFile(fileName, ".3gp", dir).getPath());
            recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    Log.e("Error", "录制失败" + what);
                }
            });

            recorder.prepare();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
    }

    public void startRecord(String phone) {
        if (isRecordStarted) {
            recorder.stop();
            releaseMediaRecorder();
            isRecordStarted = false;
        }
        String filename;
        if (phone == null) {
            filename = System.currentTimeMillis() + "";
        } else {
            filename = phone + "_" + System.currentTimeMillis();
        }
        if (prepareRecorder(filename)) {
            recorder.start();
            isRecordStarted = true;
        } else {
            releaseMediaRecorder();
            isRecordStarted = false;
        }
    }

    public void stopRecord() {
        try {
            if (recorder != null && isRecordStarted) {
                releaseMediaRecorder();
                isRecordStarted = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
        }
    }

    private void releaseMediaRecorder() {
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }
}
