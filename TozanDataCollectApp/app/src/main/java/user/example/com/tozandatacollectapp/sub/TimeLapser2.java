package user.example.com.tozandatacollectapp.sub;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import user.example.com.tozandatacollectapp.CameraRemote.TakePictureTask;

public class TimeLapser2 {
    private final String TAG = getClass().getSimpleName();

    public interface ShootingCallback{
        void takenStillImage(Bitmap stillImage);
    }

    TozanDataInfo tozanDataInfo;
    int interval = 5000;
    private TimeLapser2.ShootingCallback shootingCallback;
    boolean loop;
    Handler h;
    Context context;
    TakePictureTask takePicture;
    Runnable runnable;

    public TimeLapser2 (Context context, TozanDataInfo tozanDataInfo){
        this.tozanDataInfo = tozanDataInfo;
        this.context = context;
        h = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                try{

                    initTask();
                    takePicture.execute();
                    Log.d(TAG, "takePicture");

                }catch (Exception e){

                }
            }
        };
    }

    public TimeLapser2 setInterval(int msec){
        interval = msec;
        return this;
    }

    public TimeLapser2 setShootingCallback(TimeLapser2.ShootingCallback callback){
        this.shootingCallback = callback;
        return this;
    }

    public void initTask(){
        takePicture = new TakePictureTask(context, tozanDataInfo, new ShootingCallback() {
            @Override
            public void takenStillImage(Bitmap stillImage) {
                if(shootingCallback != null)
                    shootingCallback.takenStillImage(stillImage);
                if(loop) h.postDelayed(runnable, interval);
            }
        });
    }


    public void start(){
        loop = true;
        h.post(runnable);
    }

    public void stop(){
        h.removeCallbacks(runnable);
        loop = false;
    }
}
