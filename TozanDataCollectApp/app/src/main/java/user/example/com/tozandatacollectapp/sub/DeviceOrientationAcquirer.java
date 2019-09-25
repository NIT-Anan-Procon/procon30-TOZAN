package user.example.com.tozandatacollectapp.sub;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DeviceOrientationAcquirer {

    public interface OrientationChangeListener{
        void orientationChanged(int azimuth, int pitch, int roll);
    }

    protected final static double RAD2DEG = 180 / Math.PI;

    private static final int MATRIX_SIZE = 16;

    /* 回転行列 */
    private float[] inR = new float[MATRIX_SIZE];
    private float[] outR = new float[MATRIX_SIZE];
    private float[] I = new float[MATRIX_SIZE];

    private float[] orientationValues = new float[3];
    private float[] magneticValues = new float[3];
    private float[] accelerometerValues = new float[3];

    private int azimuth, pitch, roll;
    private OrientationChangeListener listener;

    private SensorManager sensorManager;

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            switch (event.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magneticValues = event.values.clone();
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    accelerometerValues = event.values.clone();
                    break;
            }

            if (magneticValues != null && accelerometerValues != null) {

                SensorManager.getRotationMatrix(inR, I, accelerometerValues, magneticValues);

                SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
                SensorManager.getOrientation(outR, orientationValues);

                azimuth = (int) (orientationValues[0] * RAD2DEG);
                pitch = (int) (orientationValues[1] * RAD2DEG);
                roll = (int) (orientationValues[2] * RAD2DEG);
                if(listener != null){
                    listener.orientationChanged(azimuth, pitch, roll);
                }
            }
        }
    };



    public DeviceOrientationAcquirer(Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void start(){
        sensorManager.registerListener(
                sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);

        sensorManager.registerListener(
                sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI);

    }

    public void stop(){
        sensorManager.unregisterListener(sensorEventListener);
    }

    public int getImageOrientation(){
        int idt = roll;
        if(Math.abs(idt) <= 45) {
            return 0;
        }else if(Math.abs(idt) >= 135) {
            return 180;
        }else{
            return idt > 0 ? 90 : 270;
        }
    }

    public void setOrientationChangeListener(OrientationChangeListener listener){
        this.listener = listener;
    }
}
