package user.example.com.tozandatacollectapp.CameraRemote;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import user.example.com.tozandatacollectapp.sub.TimeLapser2;
import user.example.com.tozandatacollectapp.sub.TozanDataInfo;

public class TakePictureTask extends AsyncTask<Void, Void, Void> {

    Context context;
    TozanDataInfo tozanDataInfo;
    Device device;
    private TimeLapser2.ShootingCallback shootingCallback;

    public TakePictureTask(Context context, TozanDataInfo tozanDataInfo, TimeLapser2.ShootingCallback shootingCallback){
        this.context = context;
        this.tozanDataInfo = tozanDataInfo;
        device = new Device("http://10.0.0.1:10000/sony/camera");
        this.shootingCallback = shootingCallback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        long ldt = System.currentTimeMillis();
        while (!device.isIDLE()){
            while (ldt < System.currentTimeMillis() - 500);
            ldt = System.currentTimeMillis();
        }
        Device.getImg(device.takePicture(), context, new Device.OnLoadFinishCallBack() {
            @Override
            public void onLoadFinish(Bitmap bmp) {
                if(shootingCallback != null)
                    shootingCallback.takenStillImage(bmp);
            }
        });

        return null;
    }


}
