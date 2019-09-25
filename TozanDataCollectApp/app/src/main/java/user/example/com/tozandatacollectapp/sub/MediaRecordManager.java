package user.example.com.tozandatacollectapp.sub;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class MediaRecordManager {

    private final String TAG = getClass().getSimpleName();

    private MediaRecorder rec;
    private Thread thread;

    private MountainData mountainData;
    private String dataStoragePath;

    private boolean isRecording = false;
    private int sourceDuration;
    private long time;

    public MediaRecordManager(int sourceDuration, String dataStoragePath, MountainData mountainData){
        if(sourceDuration < 0) throw new IllegalArgumentException("指定する出力ファイルの再生時間が負の値です。(sourceDuration = " + sourceDuration + ")");
        this.sourceDuration = sourceDuration;
        this.mountainData = mountainData;
        this.dataStoragePath = dataStoragePath;
    }

    private void startRecord() {

        File storageDir = new File(dataStoragePath);

        File dir = new File(
                storageDir.getAbsolutePath(),
                mountainData.getmId() + "/resources/sounds"
        );

        if(!dir.exists())
            dir.mkdirs();

        time = System.currentTimeMillis();

        final File wavFile = new File(dir, time + ".wav");
        if (wavFile.exists()) {
            wavFile.delete();
        }
                rec = new MediaRecorder();
                rec.setAudioSource(MediaRecorder.AudioSource.MIC);
                rec.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                rec.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                rec.setAudioSamplingRate(48000);
                rec.setAudioEncodingBitRate(384000);
                rec.setOutputFile(wavFile.getAbsolutePath());

                try {
                    rec.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                rec.start();

                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "thread start");
                        try {
                            while(isRecording){
                                if(time < System.currentTimeMillis() - sourceDuration) break;
                                Thread.sleep(500);
                            }
                            if(isRecording)
                                Log.d(TAG, "restart record");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        } finally {
                            if(isRecording) {
                                stopRecord();
                                startRecord();
                            }

                        }

                    }
                });
                thread.start();
    }

    private void stopRecord() {
                try {
                    rec.stop();
                    rec.reset();
                    rec.release();
                    Log.d(TAG, "stop recording");
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }

    public void start(){
        if(!isRecording){
            isRecording = true;
            startRecord();
            Log.d(TAG, "start recording");
        }else{
            Log.d(TAG, "already recording");
        }
    }

    public void stop(){
        if(isRecording) {
            isRecording = false;
            try {
                stopRecord();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Log.d(TAG, "recording already stopped");
        }
    }
}
