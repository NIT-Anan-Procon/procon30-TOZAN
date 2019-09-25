package user.example.com.tozandatacollectapp.sub;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import user.example.com.tozandatacollectapp.R;

public class MediaRecordManager {

    private final String TAG = getClass().getSimpleName();

    private MediaRecorder rec;
    private String appName;
    private Thread thread;
    private TozanDataInfo tozanDataInfo;

    private boolean isRecording = false;
    private int sourceDuration;
    private long time;

    public MediaRecordManager(Context context, int sourceDuration, TozanDataInfo tozanDataInfo){
        appName = context.getString(R.string.app_name);
        if(sourceDuration < 0) throw new IllegalArgumentException("指定する出力ファイルの再生時間が負の値です。(sourceDuration = " + sourceDuration + ")");
        this.sourceDuration = sourceDuration;
        this.tozanDataInfo = tozanDataInfo;
    }

    private void startRecord() {

        File storageDir = new File(tozanDataInfo.getDataPath());
        if(!storageDir.exists()){
            storageDir = new File(tozanDataInfo.getIntStorage());
        }

        File dir = new File(
                storageDir.getAbsolutePath(),
                tozanDataInfo.getDataName() + "/resources/sounds"
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
