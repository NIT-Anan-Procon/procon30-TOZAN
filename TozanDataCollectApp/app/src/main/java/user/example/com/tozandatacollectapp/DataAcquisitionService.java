package user.example.com.tozandatacollectapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.media.ExifInterface;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import user.example.com.tozandatacollectapp.sub.DeviceOrientationAcquirer;
import user.example.com.tozandatacollectapp.sub.ExifLocation;
import user.example.com.tozandatacollectapp.sub.LocationAcquirer;
import user.example.com.tozandatacollectapp.sub.MediaRecordManager;
import user.example.com.tozandatacollectapp.sub.StorageAcquirer;
import user.example.com.tozandatacollectapp.sub.TimeLapser3;
import user.example.com.tozandatacollectapp.sub.TozanDataInfo;

import static user.example.com.tozandatacollectapp.BrowseActivity.EXTRA_DATAPATH;
import static user.example.com.tozandatacollectapp.BrowseActivity.EXTRA_MOUNTAIN_ID;

public class DataAcquisitionService extends Service {

    public void sendImage(String path, long time){
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("mode", 0);
        broadcastIntent.putExtra(
                "path", path);
        broadcastIntent.putExtra(
                "time", time);
        broadcastIntent.setAction(DataAcquisitionActivity.ACTION);
        getBaseContext().sendBroadcast(broadcastIntent);
    }

    public void sendLocation(Location location){
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("mode", 1);
        broadcastIntent.putExtra(
                "lat", location.getLatitude());
        broadcastIntent.putExtra(
                "lon", location.getLongitude());
        broadcastIntent.setAction(DataAcquisitionActivity.ACTION);
        getBaseContext().sendBroadcast(broadcastIntent);
    }

    public class LocalBinder extends Binder {
        DataAcquisitionService getService() {
            return DataAcquisitionService.this;
        }
    }


    String TAG = getClass().getSimpleName();
    boolean flag;
    TimeLapser3 timelapser;
    Handler h;
    LocationAcquirer locationAcquirer;
    private ExifLocation exifLocation;
    private int azimuth, pitch, roll;
    MediaRecordManager mediaRecordManager;
    private final IBinder mBinder = new LocalBinder();

    private String dataName, dataPath;
    private TozanDataInfo tozanDataInfo;
    private StorageAcquirer sd = StorageAcquirer.getInstance();
    private DeviceOrientationAcquirer deviceOrientationAcquirer;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground();

        if(h == null){
            h = new Handler();
        }

        if (intent != null) {
            int button = intent.getIntExtra("button", -1);
            if (button != -1) {
                if (button == 1919810){
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "停止します", Toast.LENGTH_LONG).show();
                        }
                    });
                    stopSelf();
                }
                return START_STICKY;
            }else{
                SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(this);

                dataName = intent.getStringExtra(EXTRA_MOUNTAIN_ID);
                if(dataName == null){
                    dataName = data.getString(getString(R.string.key_name), null);
                }
                dataPath = intent.getStringExtra(EXTRA_DATAPATH);
                if(dataPath == null){
                    dataPath = data.getString(getString(R.string.key_storage), null);
                    if(dataPath == null || !new File(dataPath).exists()){
                        dataPath = sd.getInternalStorageList().get(0);
                    }
                }

            }
        }else if (flag){
            return START_STICKY;
        }else{
            SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(this);

            dataName = data.getString(getString(R.string.key_name), null);
            dataPath = sd.getInternalStorageList().get(0);
        }
        tozanDataInfo = new TozanDataInfo(dataName, dataPath, sd.getInternalStorageList().get(0));

        flag = true;

        timelapser =
                new TimeLapser3(getApplicationContext(), tozanDataInfo)
                        .setInterval(1000)
                        .setShootingCallback(new TimeLapser3.ShootingCallback() {
                            @Override
                            public void takenStillImage(final Bitmap stillImage) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendImage(saveWithMetaData(stillImage), System.currentTimeMillis());
                                        startForeground(stillImage);
                                    }
                                }).start();
                            }
                        }).setRotation(90);

        timelapser.start();

        locationAcquirer = new LocationAcquirer(getApplicationContext(), new LocationAcquirer.OnLocationResultListener() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                sendLocation(location);
                exifLocation = LocationAcquirer.encodeGpsToExifFormat(location);
                Log.d("locationResult", exifLocation.latitude + ", " + exifLocation.longitude);
            }

            @Override
            public void lastLocation(Location location) {
                if(exifLocation != null) return;
                sendLocation(location);
                exifLocation = LocationAcquirer.encodeGpsToExifFormat(location);
                Log.d("lastLocation", exifLocation.latitude + ", " + exifLocation.longitude);
            }
        });

        locationAcquirer.startLocationUpdates();

        mediaRecordManager = new MediaRecordManager(getApplicationContext(), 1000 * 60, tozanDataInfo);
        mediaRecordManager.start();

        deviceOrientationAcquirer = new DeviceOrientationAcquirer(getApplicationContext());
        deviceOrientationAcquirer.setOrientationChangeListener(new DeviceOrientationAcquirer.OrientationChangeListener() {
            @Override
            public void orientationChanged(int a, int p, int r) {
                azimuth = a;
                pitch = p;
                roll = r;
            }
        });

        deviceOrientationAcquirer.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stop();
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        stop();
        Log.d(TAG, "onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory:level=" + level);
        super.onTrimMemory(level);
    }

    private void startForeground(){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MyId")
                .setContentTitle("データ取得中")
                .setContentText("")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .addAction(R.drawable.ic_close, "STOP", makePendingIntent(1919810));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(createNotificationChannel());
        }

        Notification notification = builder.build();

        startForeground(1, notification);
    }

    private void startForeground(Bitmap bmp){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MyId")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("データ取得中")
                .setLargeIcon(bmp)
                .addAction(R.drawable.ic_close, "停止", makePendingIntent(1919810));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(createNotificationChannel());
        }

        Notification notification = builder.build();

        startForeground(1, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(){
        String channelId = "TozanDataAcquirerService";
        String channelName = "TozanDataAcquirer";
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager service = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    private PendingIntent makePendingIntent(int id){
        return PendingIntent.getService(this,id,new Intent(this,DataAcquisitionService.class).putExtra("button",id),PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private String saveWithMetaData(Bitmap bmp){

        String path;
        try {
            File storageDir = new File(tozanDataInfo.getDataPath());
            if(!storageDir.exists()){
                storageDir = new File(tozanDataInfo.getIntStorage());
            }

            File dir = new File(
                    storageDir.getAbsolutePath(),
                    tozanDataInfo.getDataName() + "/resources/n_view"
            );

            if(!dir.exists())
                dir.mkdirs();

            File jpegFile = new File(dir, System.currentTimeMillis() + ".jpg");
            path = jpegFile.getPath();

            FileOutputStream outStream = new FileOutputStream(jpegFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.close();

            // Exifを追加する対象の画像ファイルを指定
            ExifInterface ex = new ExifInterface(jpegFile.getAbsolutePath());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

            ex.setAttribute(ExifInterface.TAG_DATETIME, sdf.format(System.currentTimeMillis()));

            if(exifLocation != null) {
                // exifを必要分セットする
                ex.setAttribute(ExifInterface.TAG_GPS_LATITUDE, exifLocation.latitude);
                ex.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, exifLocation.latitudeRef);
                ex.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, exifLocation.longitude);
                ex.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, exifLocation.longitudeRef);
                ex.setAttribute(ExifInterface.TAG_GPS_LATITUDE, exifLocation.latitude);
            }

            ex.setAttribute("UserComment", "{\"Azimuth\":"+ azimuth +",\"Pitch\":" + pitch + ",\"Roll\":" + roll + "}");

            // 保存する
            ex.saveAttributes();

            return path;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void stop(){
        if(timelapser != null) {
            timelapser.stop();
            //timelapser.release();
        }
        if(locationAcquirer != null){
            locationAcquirer.stopLocationUpdates();
        }
        if(mediaRecordManager != null){
            mediaRecordManager.stop();
        }
    }

    /*public void setSurface(SurfaceView surfaceView){

    }*/



}
