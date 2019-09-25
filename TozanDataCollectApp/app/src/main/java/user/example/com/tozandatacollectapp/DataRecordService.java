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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import user.example.com.tozandatacollectapp.sub.DBOpenHelper;
import user.example.com.tozandatacollectapp.sub.ExifLocation;
import user.example.com.tozandatacollectapp.sub.LocationAcquirer;
import user.example.com.tozandatacollectapp.sub.MediaRecordManager;
import user.example.com.tozandatacollectapp.sub.MountainData;
import user.example.com.tozandatacollectapp.sub.StorageAcquirer;
import user.example.com.tozandatacollectapp.sub.TimeLapser;

import static user.example.com.tozandatacollectapp.BrowseActivity.EXTRA_MOUNTAIN_DATA;
import static user.example.com.tozandatacollectapp.DataRecordActivity.STATE_NOT_RECORDING;
import static user.example.com.tozandatacollectapp.DataRecordActivity.STATE_RECORDING;
import static user.example.com.tozandatacollectapp.TitleActivity.EXTRA_DATAPATH;
import static user.example.com.tozandatacollectapp.sub.MyReceiver.ACTION_REC_STATE;
import static user.example.com.tozandatacollectapp.sub.MyReceiver.ACTION_SEND_IMG;
import static user.example.com.tozandatacollectapp.sub.MyReceiver.DATA_IMG_PATH;
import static user.example.com.tozandatacollectapp.sub.MyReceiver.DATA_IMG_TIME;

public class DataRecordService extends Service {

    public void sendImage(String path, long time){
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra(
                DATA_IMG_PATH, path);
        broadcastIntent.putExtra(
                DATA_IMG_TIME, time);
        broadcastIntent.setAction(ACTION_SEND_IMG);
        getBaseContext().sendBroadcast(broadcastIntent);
    }

    public void sendState(int state){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_REC_STATE);
        broadcastIntent.putExtra(
                ACTION_REC_STATE, state);
        getBaseContext().sendBroadcast(broadcastIntent);
    }

    public class LocalBinder extends Binder {
        DataRecordService getService() {
            return DataRecordService.this;
        }
    }

    public static final String DATA_BUTTON = "button";
    public static final int BUTTON_STOP = 18728;

    public static final String TAG = DataRecordService.class.getSimpleName();
    boolean flag;
    private TimeLapser timelapser;
    private Handler h;
    private LocationAcquirer locationAcquirer;
    private ExifLocation exifLocation;
    private int azimuth, pitch, roll;
    private MediaRecordManager mediaRecordManager;
    private final IBinder mBinder = new LocalBinder();

    private MountainData mountainData;
    private String dataStoragePath;
    private StorageAcquirer sd = StorageAcquirer.getInstance();

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

    public void log(int n){
        Log.d(TAG, "onStartCommand:" + n);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground();


        if(h == null){
            h = new Handler();
        }

        if (intent != null) {
            //インテントがnullでない場合


            int button = intent.getIntExtra(DATA_BUTTON, -1);
            if (button != -1) {

                //ボタンが押されてインテントが投げられた場合
                if (button == BUTTON_STOP){
                    stopSelf();
                }
                return START_STICKY;
            }else{

                //インテントからデータを取得
                mountainData = intent.getParcelableExtra(EXTRA_MOUNTAIN_DATA);
                dataStoragePath = intent.getStringExtra(EXTRA_DATAPATH);

                //どちらかのデータが無い場合
                if(mountainData == null || dataStoragePath == null){

                    dataNotFound(h);
                    return START_STICKY;
                }
            }

        }else if(!flag){
            //初期化が済んでおらず、インテントがnullの場合

            SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(this);

            //山IDの取得
            String mountainId = data.getString(getString(R.string.key_mountain_id), null);
            if(mountainId == null){
                //山IDが無く、山データを取得できない場合
                dataNotFound(h);
                return START_STICKY;
            }else{
                //データベースから山データを取得
                DBOpenHelper helper = new DBOpenHelper(this);
                mountainData = helper.getMountainFromIdStr(mountainId);
            }

            //データストレージのパスを取得
            dataStoragePath = data.getString(getString(R.string.key_storage), null);
            if(dataStoragePath == null || new File(dataStoragePath).exists()){
                //データストレージが無い場合
                dataNotFound(h);
                return START_STICKY;
            }

        }else{

            //既に初期化されている場合
            return START_STICKY;
        }


        //初期化フラグを立てる
        flag = true;

        timelapser =
                new TimeLapser(getApplicationContext())
                        .setInterval(1000) //1秒ごとに撮影
                        .setShootingCallback(new TimeLapser.ShootingCallback() {
                            @Override
                            public void takenStillImage(final Bitmap stillImage, final long timeMillis, int a, int p, int r){
                                //端末の傾き、方向を取得
                                azimuth = a;
                                pitch = p;
                                roll = r;

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //メタデータを付けた画像と撮影時間を送信
                                        sendImage(saveWithMetaData(stillImage), timeMillis);
                                        //通知をアップデート
                                        startForeground(stillImage);
                                    }
                                }).start();
                            }
                        });

        timelapser.start();


        locationAcquirer = new LocationAcquirer(getApplicationContext(), new LocationAcquirer.OnLocationChangeListener() {
            @Override
            public void locationChanged(Location location) {
                if(exifLocation != null) return;
                if(location == null) return;
                exifLocation = LocationAcquirer.encodeGpsToExifFormat(location);
                Log.d("locationChanged", exifLocation.latitude + ", " + exifLocation.longitude);
            }
        });

        locationAcquirer.startLocationUpdates();


        mediaRecordManager = new MediaRecordManager(1000 * 60, dataStoragePath, mountainData);
        mediaRecordManager.start();

        sendState(STATE_RECORDING);


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
        startForeground(null);
    }

    private void startForeground(Bitmap bmp){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MyId")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(getString(R.string.data_recoding))
                .addAction(R.drawable.ic_close, getString(R.string.stop), makePendingIntent(BUTTON_STOP));

        if(bmp != null) builder.setLargeIcon(bmp);

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
        return PendingIntent.getService(this,id,new Intent(this, DataRecordService.class).putExtra(DATA_BUTTON,id),PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private String saveWithMetaData(Bitmap bmp){

        String path;
        try {
            File storageDir = new File(dataStoragePath);
            File dir = new File(
                    storageDir.getAbsolutePath(),
                    mountainData.getmId() + "/resources/n_view"
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

            SimpleDateFormat sdf = new SimpleDateFormat(this.getString(R.string.format_date));

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
            timelapser.release();
        }
        if(locationAcquirer != null){
            locationAcquirer.stopLocationUpdates();
        }
        if(mediaRecordManager != null){
            mediaRecordManager.stop();
        }
        sendState(STATE_NOT_RECORDING);
    }

    public void dataNotFound(Handler h){
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getString(R.string.service_intent_data_none), Toast.LENGTH_LONG).show();
            }
        });
        stopSelf();
    }
}
