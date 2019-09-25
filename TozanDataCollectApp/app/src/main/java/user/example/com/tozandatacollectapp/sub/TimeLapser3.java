package user.example.com.tozandatacollectapp.sub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimeLapser3 {

    public interface ShootingCallback{
        void takenStillImage(Bitmap stillImage);
    }

    private static final String TAG = "Timelapser";
    public static final int lSide = 1920;

    private DummyDetector dummyDetector;
    private CameraSource cameraSource;
    private TozanDataInfo tozanDataInfo;

    private long time = 0;
    private int rotation = 0;
    private int deviceOrientation;

    private int interval = 5000;//デフォルトを5秒とする

    private ShootingCallback shootingCallback;
    private DeviceOrientationAcquirer deviceOrientationAcquirer;

    Handler handler;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            cameraSource.takePicture(null, new CameraSource.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if(shootingCallback != null)
                        shootingCallback.takenStillImage(rotateBmp(resizeBitmap(bitmap)));
                    if(loop) handler.postDelayed(runnable, interval);
                }
            });
        }
    };
    boolean loop;


    public TimeLapser3(Context context, TozanDataInfo tozanDataInfo){

        this.tozanDataInfo = tozanDataInfo;

        dummyDetector = new DummyDetector();
        dummyDetector.setProcessor(new DummyProcessor());

        Size previewSize = getPreviewSize(context);

        cameraSource = new CameraSource.Builder(context, dummyDetector)
                .setRequestedPreviewSize(previewSize.getWidth(), previewSize.getHeight())
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(1.0f)
                .setAutoFocusEnabled(true)
                .build();

        deviceOrientationAcquirer = new DeviceOrientationAcquirer(context);
        deviceOrientationAcquirer.setOrientationChangeListener(new DeviceOrientationAcquirer.OrientationChangeListener() {
            @Override
            public void orientationChanged(int azimuth, int pitch, int roll) {
                deviceOrientation = deviceOrientationAcquirer.getImageOrientation();
            }
        });

        handler = new Handler();

    }

    public Size getPreviewSize(Context context){
        Size mPreviewSize = null;
        // カメラマネージャのインスタンスを取得
        CameraManager manager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                // 指定したカメラの情報を取得
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) 
                    == CameraCharacteristics.LENS_FACING_BACK) {
                    Log.v("Camera2", "Facing : BACK");
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    List<Size> sizeList = new ArrayList<>();
                    for(Size s : map.getOutputSizes(SurfaceTexture.class)) {
                        if (Math.max(s.getWidth(), s.getHeight()) <= lSide && s.getWidth() != s.getHeight())
                            sizeList.add(s);
                    }
                    Collections.sort(sizeList, new Comparator<Size>() {
                        @Override
                        public int compare(Size size, Size t1) {
                            int idt = -Integer.compare(Math.max(size.getWidth(), size.getHeight()), Math.max(t1.getWidth(), t1.getHeight()));
                            if(idt != 0) return idt;
                            return -Integer.compare(Math.min(size.getWidth(), size.getHeight()), Math.min(t1.getWidth(), t1.getHeight()));
                        }
                    });
                    mPreviewSize = sizeList.get(0);
                }
                else if (characteristics.get(CameraCharacteristics.LENS_FACING)
                    == CameraCharacteristics.LENS_FACING_FRONT) {
                    Log.v("Camera2", "Facing : FRONT"); 
                }
            }
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mPreviewSize;
    }

    public TimeLapser3 setInterval(int msec){
        interval = msec;
        return this;
    }

    public TimeLapser3 setShootingCallback(ShootingCallback callback){
        shootingCallback = callback;
        return this;
    }

    public TimeLapser3 setRotation(int degrees){
        this.rotation = (Math.round(degrees / 90f) % 4) * 90;
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

        loop = true;
        handler.postDelayed(runnable, interval);

        if(deviceOrientationAcquirer != null)
            deviceOrientationAcquirer.start();

    }

    public void stop(){
        loop = false;
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

    private Bitmap rotateBmp(Bitmap src){

        if(rotation + deviceOrientation == 0) return src;
        // 回転マトリックス作成（90度回転）
        Matrix mat = new Matrix();
        mat.postRotate(rotation + deviceOrientation);

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
            return null;
        }
    }
}
