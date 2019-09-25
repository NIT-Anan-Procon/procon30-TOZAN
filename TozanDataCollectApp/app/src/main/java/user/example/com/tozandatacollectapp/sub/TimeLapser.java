package user.example.com.tozandatacollectapp.sub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import java.io.IOException;

public class TimeLapser {

    public interface ShootingCallback{
        void takenStillImage(Bitmap stillImage, long timeMillis, int azimuth, int pitch, int roll);
    }

    private static final String TAG = "Timelapser";
    public static final int lSide = 1920;

    private DummyDetector dummyDetector;
    private CameraSource cameraSource;

    private int deviceOrientation;
    private int azimuth, pitch, roll;

    private int cameraOrientation;

    private int interval = 5000;//デフォルトを5秒とする

    private ShootingCallback shootingCallback;
    private DeviceOrientationAcquirer deviceOrientationAcquirer;

    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            cameraSource.takePicture(null, new CameraSource.PictureCallback() {
                @Override
                public void onPictureTaken(final byte[] bytes) {
                    if(shootingCallback != null){
                        final long timeMillis = System.currentTimeMillis();
                        final int a = azimuth, p = pitch, r = roll;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //サイズと向きを変えた画像を渡す
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                shootingCallback.takenStillImage(rotateBmp(resizeBitmap(bitmap)), timeMillis, a, p, r);
                            }
                        }).start();
                    }
                    //ループさせる
                    if(loop) handler.postDelayed(runnable, interval);
                }
            });
        }
    };
    private boolean loop;

    public TimeLapser(Context context){

        cameraOrientation = getCameraOrientation(context);

        //ダミーディテクターを初期化
        dummyDetector = new DummyDetector();
        dummyDetector.setProcessor(new DummyProcessor());
        //カメラソースを取得　ディテクターはダミーで、何も処理をしていない
        cameraSource = new CameraSource.Builder(context, dummyDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(1.0f)
                .setAutoFocusEnabled(true)
                .build();

        //端末の向きを取得
        deviceOrientationAcquirer = new DeviceOrientationAcquirer(context);
        deviceOrientationAcquirer.setOrientationChangeListener(new DeviceOrientationAcquirer.OrientationChangeListener() {
            @Override
            public void orientationChanged(int a, int p, int r) {
                azimuth = a;
                pitch = p;
                roll = r;
                deviceOrientation = deviceOrientationAcquirer.getImageOrientation();
            }
        });

        handler = new Handler();
    }

    public int getCameraOrientation(Context context){

        // カメラマネージャのインスタンスを取得
        CameraManager manager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                // 指定したカメラの情報を取得
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                if (characteristics.get(CameraCharacteristics.LENS_FACING)
                        == CameraCharacteristics.LENS_FACING_BACK){
                    Log.v("Camera2", "Facing : BACK");
                    //カメラの傾きを取得
                    return characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                }else if(characteristics.get(CameraCharacteristics.LENS_FACING)
                        == CameraCharacteristics.LENS_FACING_FRONT){
                    Log.v("Camera2", "Facing : FRONT");
                }
            }
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return 0;
    }

    //撮影インターバルを指定
    public TimeLapser setInterval(int msec){
        interval = msec;
        return this;
    }

    //写真が送られえてくるコールバック
    public TimeLapser setShootingCallback(ShootingCallback callback){
        shootingCallback = callback;
        return this;
    }

    @SuppressLint("MissingPermission")
    public void start(){
        if(cameraSource == null) return;

        try{
            cameraSource.stop();
        } catch (IllegalStateException e){
            e.printStackTrace();
        }

        try {
            cameraSource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //ループできるようにする
        loop = true;
        //撮影開始
        handler.postDelayed(runnable, interval);

        if(deviceOrientationAcquirer != null)
            deviceOrientationAcquirer.start();

    }

    public void stop(){

        //ループできないようにする
        loop = false;
        //撮影を止める
        handler.removeCallbacks(runnable);

        if(cameraSource != null)
            cameraSource.stop();
        if(deviceOrientationAcquirer != null)
            deviceOrientationAcquirer.stop();
    }

    public void release(){
        if(cameraSource != null)
            cameraSource.release();
    }

    //画像をリサイズ
    private Bitmap resizeBitmap(Bitmap beforeResizeBitmap){

        Size size = new Size(1440, 1920);
        // リサイズ比
        double resizeScale;
        // 横長画像の場合
        if (beforeResizeBitmap.getWidth() >= beforeResizeBitmap.getHeight()) {
            resizeScale = (double) size.getWidth() / beforeResizeBitmap.getWidth();
        }
        // 縦長画像の場合
        else {
            resizeScale = (double) size.getHeight() / beforeResizeBitmap.getHeight();
        }
        // リサイズ
        Bitmap afterResizeBitmap = Bitmap.createScaledBitmap(beforeResizeBitmap,
                (int) (beforeResizeBitmap.getWidth() * resizeScale),
                (int) (beforeResizeBitmap.getHeight() * resizeScale),
                true);

        return afterResizeBitmap;
    }

    //画像を回転
    private Bitmap rotateBmp(Bitmap src){

        int rotate = deviceOrientation + cameraOrientation;
        //回転角度のトータルが0なら回転しない
        if(rotate == 0) return src;

        //回転マトリックス作成
        Matrix mat = new Matrix();
        mat.postRotate(rotate);

        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), mat, true);
    }

    public class DummyProcessor implements Detector.Processor<Void> {
        @Override
        public void release() {
        }

        @Override
        public void receiveDetections(Detector.Detections<Void> detections) {
        }
    }

    public class DummyDetector extends Detector<Void> {

        @Override
        public SparseArray<Void> detect(Frame frame) {
            //何も処理しない
            return null;
        }
    }
}
