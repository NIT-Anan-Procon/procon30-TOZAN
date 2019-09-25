package user.example.com.tozandatacollectapp.sub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static android.content.Context.LOCATION_SERVICE;

public final class LocationAcquirer extends LocationCallback{

    public LocationAcquirer(Context context, OnLocationResultListener mListener){
        this.mListener = mListener;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        this.context = context;
        //initLocationManager(context);
    }

    private final String TAG = getClass().getSimpleName();
    private Context context;

    /**
     * Exif形式にGPS Locationを変換して返す。
     * longitudeRef => W or E
     * latitudeRef => N or S
     * latitude and longitude => num1/denom1,num2/denom2,num3/denom3
     * ex) 12/1,34/1,56789/1000
     *
     * @param location
     * @return ExifLocation
     */
    public static ExifLocation encodeGpsToExifFormat(Location location) {
        ExifLocation exifLocation = new ExifLocation();
        // 経度の変換(正->東, 負->西)
        // convertの出力サンプル => 73:9:57.03876
        String[] lonDMS = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS).split(":");
        StringBuilder lon = new StringBuilder();
        // 経度の正負でREFの値を設定（経度からは符号を取り除く）
        exifLocation.longitudeRef = lonDMS[0].contains("-") ? "W" : "E";

        lon.append(lonDMS[0].replace("-", ""));
        lon.append("/1,");
        lon.append(lonDMS[1]);
        lon.append("/1,");
        // 秒は小数の桁を数えて精度を求める
        int index = lonDMS[2].indexOf('.');
        if (index == -1) {
            lon.append(lonDMS[2]);
            lon.append("/1");
        } else {
            int digit = lonDMS[2].substring(index + 1).length();
            int second = (int) (Double.parseDouble(lonDMS[2]) * Math.pow(10, digit));
            lon.append(String.valueOf(second));
            lon.append("/1");
            for (int i = 0; i < digit; i++) {
                lon.append("0");
            }
        }
        exifLocation.longitude = lon.toString();

        // 緯度の変換(正->北, 負->南)
        // convertの出力サンプル => 73:9:57.03876
        String[] latDMS = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS).split(":");
        StringBuilder lat = new StringBuilder();
        // 経度の正負でREFの値を設定（経度からは符号を取り除く）
        exifLocation.latitudeRef = latDMS[0].contains("-") ? "S" : "N";

        lat.append(latDMS[0].replace("-", ""));
        lat.append("/1,");
        lat.append(latDMS[1]);
        lat.append("/1,");
        // 秒は小数の桁を数えて精度を求める
        index = latDMS[2].indexOf('.');
        if (index == -1) {
            lat.append(latDMS[2]);
            lat.append("/1");
        } else {
            int digit = latDMS[2].substring(index + 1).length();
            int second = (int) (Double.parseDouble(latDMS[2]) * Math.pow(10, digit));
            lat.append(String.valueOf(second));
            lat.append("/1");
            for (int i = 0; i < digit; i++) {
                lat.append("0");
            }
        }
        exifLocation.latitude = lat.toString();

        return exifLocation;
    }

    private static final int LOCATION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private OnLocationResultListener mListener;

    public interface OnLocationResultListener {
        void onLocationResult(LocationResult locationResult);
        void lastLocation(Location location);
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        mListener.onLocationResult(locationResult);
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {

        // 端末の位置情報サービスが無効になっている場合、設定画面を表示して有効化を促す
        if (!isGPSEnabled()) {
            showLocationSettingDialog();
            return;
        }

        LocationRequest request = new LocationRequest();
        request.setInterval(5 * 60 * 1000);
        request.setFastestInterval(10 * 1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(mListener != null)
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    mListener.lastLocation(task.getResult());
                }
            });

        fusedLocationProviderClient.requestLocationUpdates(request, this,null);
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(this);
    }

    private Boolean isGPSEnabled() {
        android.location.LocationManager locationManager = (android.location.LocationManager) context.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    private void showLocationSettingDialog() {
        /*new android.app.AlertDialog.Builder(context)
                .setMessage("設定画面で位置情報サービスを有効にしてください")
                .setPositiveButton("設定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //NOP
                    }
                })
                .create()
                .show();*/
        Toast.makeText(context, "設定画面で位置情報サービスを有効にしてください", Toast.LENGTH_LONG).show();
    }
}
