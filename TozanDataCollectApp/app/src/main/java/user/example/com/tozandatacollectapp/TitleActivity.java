package user.example.com.tozandatacollectapp;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import user.example.com.tozandatacollectapp.Dialog.DataUpdateDialogFragment;
import user.example.com.tozandatacollectapp.Dialog.ErrorDialogFragment;
import user.example.com.tozandatacollectapp.sub.CustomUncaughtExceptionHandler;
import user.example.com.tozandatacollectapp.sub.StorageAcquirer;

public class TitleActivity extends AppCompatActivity{

    public static final String EXTRA_MOUNTAIN_ID = "tozanDataName", EXTRA_DATAPATH = "tozanDataPath", EXTRA_ACQUIRING = "tozanAcquiring";
    public static final String EX_STACK_TRACE = "exStackTrace";
    final int REQUEST_CODE = 1000;
    private boolean permissionChecked;


    //ストレージ設定用
    private SharedPreferences data;
    private StorageAcquirer storageAcquirer;
    private String storagePath;

    //データ取得中か
    private boolean isAcquiring;

    private Button post, setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_title);

        //ストレージ読み込み
        StorageAcquirer.init(this);
        storageAcquirer = StorageAcquirer.getInstance();

        //プリファレンス初期化
        data = PreferenceManager.getDefaultSharedPreferences(this);

        //発生したエラーを処理するやつ
        setExceptionHandler(data);

        //プリファレンスに保存されているストレージ設定を読み込む
        storagePath = data.getString(getString(R.string.key_storage), null);

        //未設定の時、内部ストレージに設定
        if(storagePath == null){
            storagePath = storageAcquirer.getInternalStorageList().get(0);
            data.edit().putString(getString(R.string.key_storage), storagePath).apply();
        }

        post = findViewById(R.id.postData);
        setting = findViewById(R.id.setting);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //ストレージ設定を再取得
                storagePath = data.getString(getString(R.string.key_storage), null);

                //データ取得中かつデータ取得サービスが起動されているか
                if(isAcquiring && isServiceRunning()){

                    //保存されていたデータ取得中の山IDを取得
                    String id = data.getString(getString(R.string.key_name), null);

                    //データ取得画面を起動
                    startActivity(
                            new Intent(
                                    TitleActivity.this,
                                    DataAcquisitionActivity.class
                            ).putExtra(
                                    EXTRA_MOUNTAIN_ID,
                                    id
                            ).putExtra(
                                    EXTRA_DATAPATH,
                                    storagePath
                            ).putExtra(
                                    EXTRA_ACQUIRING,
                                    isAcquiring
                            )
                    );
                }else{
                    //データ管理画面を取得
                    startActivity(new Intent(TitleActivity.this, BrowseActivity.class).putExtra(EXTRA_DATAPATH, storagePath));
                }

            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //設定画面を取得
                startActivity(new Intent(TitleActivity.this, PreferenceActivity.class));
            }
        });

        //山データの情報アップデートする
        DataUpdateDialogFragment.newInstance().show(getSupportFragmentManager(), "fragment_dialog");

        //ツールバー初期化
        initToolbar();

        //パーミッションを取得
        ActivityCompat.requestPermissions(TitleActivity.this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.RECORD_AUDIO},
                REQUEST_CODE);

    }

    //サービスが動いているか
    public boolean isServiceRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DataAcquisitionService.class.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //ツールバーを初期化
    public void initToolbar(){
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    //エラー周り
    private void setExceptionHandler(SharedPreferences preferences){
        CustomUncaughtExceptionHandler customUncaughtExceptionHandler = new CustomUncaughtExceptionHandler(
                getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(customUncaughtExceptionHandler);

        String exStackTrace = preferences.getString(EX_STACK_TRACE, null);

        if (!TextUtils.isEmpty(exStackTrace)) {
            // スタックトレースが存在する場合は、
            // エラー情報を送信するかしないかのダイアログを表示
            ErrorDialogFragment.newInstance(exStackTrace).show(
                    getSupportFragmentManager(), "error_dialog");
            // スタックトレースを消去
            preferences.edit().remove(EX_STACK_TRACE).apply();
        }
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
        new AlertDialog.Builder(TitleActivity.this)
                .setMessage(this.getString(R.string.permission_check))
                .setPositiveButton(this.getString(R.string.permit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(TitleActivity.this, new String[]{
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
    protected void onResume() {
        super.onResume();
        isAcquiring = data.getBoolean(getString(R.string.key_acquiring), false);
    }

}
