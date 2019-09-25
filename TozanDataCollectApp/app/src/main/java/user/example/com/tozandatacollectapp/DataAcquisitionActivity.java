package user.example.com.tozandatacollectapp;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;

import user.example.com.tozandatacollectapp.Dialog.ConfirmDialogFragment;
import user.example.com.tozandatacollectapp.sub.StorageAcquirer;

import static user.example.com.tozandatacollectapp.BrowseActivity.EXTRA_DATAPATH;
import static user.example.com.tozandatacollectapp.BrowseActivity.EXTRA_MOUNTAIN_ID;
import static user.example.com.tozandatacollectapp.TitleActivity.EXTRA_DATAPATH;
import static user.example.com.tozandatacollectapp.TitleActivity.EXTRA_MOUNTAIN_ID;

public class DataAcquisitionActivity extends AppCompatActivity implements ConfirmDialogFragment.PositiveButtonClickListener {

    ImageView display;
    TextView infoTextView;
    Button startRecord;

    IntentFilter intentFilter;
    MyBroadcastReceiver receiver;

    private String mountainID, dataPath;

    public static final String ACTION = "SEND_PATH";
    final int REQUEST_CODE = 1000;
    private boolean permissionChecked;
    private boolean isAcquiring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_acquisition);


        Intent extras = getIntent();
        if(extras != null){

            //プリファレンス初期化
            SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(this);
            StorageAcquirer.init(this);
            StorageAcquirer sd = StorageAcquirer.getInstance();

            mountainID = extras.getStringExtra(EXTRA_MOUNTAIN_ID);
            if(mountainID == null){
                mountainID = data.getString(getString(R.string.key_name), "" + System.currentTimeMillis());
            }
            dataPath = extras.getStringExtra(EXTRA_DATAPATH);
            if(dataPath == null){
                dataPath = data.getString(getString(R.string.key_storage), null);
                if(dataPath == null || !new File(dataPath).exists()){
                    dataPath = sd.getInternalStorageList().get(0);
                }
            }
            isAcquiring = extras.getBooleanExtra(getString(R.string.key_acquiring), false);
            if(!isAcquiring){
                isAcquiring = data.getBoolean(getString(R.string.key_acquiring), false);
            }
        }else{
            //データが無い時、終了する
            finish();
            return;
        }

        final Intent intent = new Intent(DataAcquisitionActivity.this, DataAcquisitionService.class);
        intent.putExtra(EXTRA_MOUNTAIN_ID, mountainID);
        intent.putExtra(EXTRA_DATAPATH, dataPath);

        display = findViewById(R.id.display);
        infoTextView = findViewById(R.id.time);
        startRecord = findViewById(R.id.start);
        if(isAcquiring){
            changeState(isServiceRunning());
        }

        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isAcquiring){
                    data.edit().putBoolean(getString(R.string.key_acquiring), false).apply();
                    startService(new Intent(intent).putExtra("button", 1919810));
                }else {
                    if (permissionChecked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent);
                        } else {
                            startService(intent);
                        }
                        data.edit().putBoolean(getString(R.string.key_acquiring), true).apply();
                    } else {
                        createDialog();
                        return;
                    }
                }
                changeState(!isAcquiring);
            }
        });

        ActivityCompat.requestPermissions(DataAcquisitionActivity.this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.RECORD_AUDIO},
                REQUEST_CODE);

        initToolbar();
    }

    public boolean isServiceRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DataAcquisitionService.class.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void changeState(boolean isAcquiring){
        this.isAcquiring = isAcquiring;
        startRecord.setBackgroundTintList(getColorStateList(isAcquiring? R.color.on : R.color.off));
        startRecord.setText(getString(isAcquiring? R.string.recording : R.string.start_record));
    }

    public void initToolbar(){
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean result = true;

        switch (id){
            case android.R.id.home:
                confirmFinish();
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }

        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                permissionChecked = true;
                for(int result : grantResults){
                    permissionChecked &= result == PackageManager.PERMISSION_GRANTED;
                }
                if (!permissionChecked) {
                    createDialog();
                }
            }
        }
    }

    public void createDialog(){
        new AlertDialog.Builder(DataAcquisitionActivity.this)
                .setMessage(this.getString(R.string.permission_check))
                .setPositiveButton(this.getString(R.string.permit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(DataAcquisitionActivity.this, new String[]{
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.INTERNET,
                                        Manifest.permission.ACCESS_NETWORK_STATE,
                                        Manifest.permission.RECORD_AUDIO},
                                REQUEST_CODE);
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onPositiveButtonClick() {
        finish();
    }


    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int mode = intent.getIntExtra("mode", -1);

            if(mode == 0) {
                String path = intent.getStringExtra("path");
                long time = intent.getLongExtra("time", -1);

                if (path == null || path.isEmpty()) return;
                File file = new File(path);
                if (file.exists()) {
                    Bitmap bmp = BitmapFactory.decodeFile(path);
                    if (bmp != null) {
                        display.setImageBitmap(bmp);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        infoTextView.setText(sdf.format(time));
                    }

                }
            }else if(mode == 1){
                double lon = intent.getDoubleExtra("lon", -1);
                double lat = intent.getDoubleExtra("lat", -1);
                //textView2.setText(String.format("lon=%f, lat=%f", lon, lat));
            }
            Log.d("BroadcastReceiver", "onreceive");
        }
    }

    @Override
    protected void onStart() {

        receiver = new MyBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(receiver, intentFilter);
        /*Intent intent = new Intent(getApplicationContext(), SampleService.class);
        bindService(intent, mSampleServiceConnection, Context.BIND_AUTO_CREATE);*/

        super.onStart();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void confirmFinish(){
        ConfirmDialogFragment.newInstance(getString(R.string.add_tozan_data))
                .show(getSupportFragmentManager(), "confirm_finish");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            confirmFinish();
            return true;
        }else {
            return super.dispatchKeyEvent(event);
        }
    }
}
