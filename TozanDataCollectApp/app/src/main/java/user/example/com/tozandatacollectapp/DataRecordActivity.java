package user.example.com.tozandatacollectapp;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import user.example.com.tozandatacollectapp.Dialog.ConfirmDialogFragment;
import user.example.com.tozandatacollectapp.sub.DBOpenHelper;
import user.example.com.tozandatacollectapp.sub.MountainData;
import user.example.com.tozandatacollectapp.sub.MyReceiver;
import user.example.com.tozandatacollectapp.sub.StorageAcquirer;

import static user.example.com.tozandatacollectapp.BrowseActivity.EXTRA_MOUNTAIN_DATA;
import static user.example.com.tozandatacollectapp.DataRecordService.BUTTON_STOP;
import static user.example.com.tozandatacollectapp.DataRecordService.DATA_BUTTON;
import static user.example.com.tozandatacollectapp.TitleActivity.EXTRA_DATAPATH;
import static user.example.com.tozandatacollectapp.TitleActivity.EXTRA_MOUNTAIN_ID;
import static user.example.com.tozandatacollectapp.sub.MyReceiver.ACTION_REC_STATE;
import static user.example.com.tozandatacollectapp.sub.MyReceiver.ACTION_SEND_IMG;

public class DataRecordActivity extends AppCompatActivity implements ConfirmDialogFragment.PositiveButtonClickListener {

    public static final int
            STATE_NOT_RECORDING = 0,
            STATE_DURING_STARTUP = 1,
            STATE_RECORDING = 2,
            STATE_DURING_STOPPING = 3;

    private ImageView display;
    private TextView infoTextView;
    private Button startRecord;

    private IntentFilter intentFilter;
    private MyReceiver receiver;
    private MyReceiver.ReceiveListener receiveListener;

    private MountainData mountainData;
    private String mountainID, dataStoragePath;

    public static final String ACTION = "SEND_PATH";
    final int REQUEST_CODE = 1000;
    private boolean permissionChecked;
    private int acquiringState = STATE_NOT_RECORDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_record);

        //プリファレンス初期化
        final SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(this);

        final Intent extras = getIntent();
        if(!getExtras(extras, data)) {
            //データが無い場合、終了する
            finish();
            return;
        }

        //データ記録サービス起動用インテントの作成
        final Intent intent = new Intent(DataRecordActivity.this, DataRecordService.class);
        intent.putExtra(EXTRA_MOUNTAIN_DATA, mountainData);
        intent.putExtra(EXTRA_DATAPATH, dataStoragePath);

        display = findViewById(R.id.display);
        infoTextView = findViewById(R.id.time);
        startRecord = findViewById(R.id.start);

        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(acquiringState == STATE_RECORDING){
                    //データ記録中の場合

                    //ボタンの表示を更新
                    changeState(STATE_DURING_STOPPING);
                    //データ記録サービスを停止
                    ContextCompat.startForegroundService(DataRecordActivity.this, intent);
                    startService(new Intent(intent).putExtra(DATA_BUTTON, BUTTON_STOP));
                    return;
                }else if(acquiringState == STATE_NOT_RECORDING){
                    //データ記録中でない場合
                    if (permissionChecked) {
                        //権限が許可されているなら、データ記録サービスを起動
                        ContextCompat.startForegroundService(DataRecordActivity.this, intent);
                        //ボタンの表示を更新
                        changeState(STATE_DURING_STARTUP);
                    } else {
                        //許可されていなければ、許可を求める
                        createDialog();
                        return;
                    }
                }else{
                    return;
                }

                //ボタンの表示を更新
                changeState(acquiringState);
            }
        });

        //権限の許可を求める
        ActivityCompat.requestPermissions(DataRecordActivity.this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.RECORD_AUDIO},
                REQUEST_CODE);

        //ツールバー初期化
        initToolbar();

        receiveListener = new MyReceiver.ReceiveListener() {
            @Override
            public void onAcquiringStateReceived(int state) {
                //ボタンの表示を更新
                changeState(state);
                //データを保存
                data.edit().putBoolean(getString(R.string.key_acquiring), state == STATE_RECORDING).apply();
            }

            @Override
            public void onImageDateReceived(final String path, final String dateStr) {
                final Handler h = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bmp = BitmapFactory.decodeFile(path);
                        if (bmp != null) {
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    display.setImageBitmap(bmp);
                                    infoTextView.setText(dateStr);
                                }
                            });
                        }
                    }
                }).start();
            }
        };
    }

    //インテントからデータを取得
    private boolean getExtras(Intent extras, SharedPreferences data) {

        if(extras == null) return false;

        //山IDを取得
        mountainID = extras.getStringExtra(EXTRA_MOUNTAIN_ID);
        if(mountainID == null){
            //nullの場合、設定からデータを取得
            mountainID = data.getString(getString(R.string.key_mountain_id), null);
            //それでもnullなら失敗
            if(mountainID == null) return false;
        }

        //山データを取得
        mountainData = extras.getParcelableExtra(EXTRA_MOUNTAIN_DATA);
        if(mountainData == null){
            //nullならデータベースから取得
            DBOpenHelper helper = new DBOpenHelper(this);
            mountainData = helper.getMountainFromIdStr(mountainID);
            //それでもnullなら失敗
            if(mountainData == null) return false;
        }

        //データ記録中かどうかを取得
        boolean isAcquiring = extras.getBooleanExtra(getString(R.string.key_acquiring), false);
        if(isAcquiring){
            isAcquiring = isServiceRunning();
            acquiringState = isAcquiring ? STATE_RECORDING : STATE_NOT_RECORDING;
            //データ記録中なら、ボタンの表示を記録中のものに変える
            if(isAcquiring) changeState(STATE_RECORDING);
        }

        //データがあるストレージのパスを取得
        dataStoragePath = extras.getStringExtra(EXTRA_DATAPATH);
        if(dataStoragePath == null){
            dataStoragePath = data.getString(getString(R.string.key_storage), null);
            if(dataStoragePath == null || !new File(dataStoragePath).exists()){
                //nullならファイルストレージ一覧から取得
                StorageAcquirer.init(this);
                StorageAcquirer sd = StorageAcquirer.getInstance();
                dataStoragePath = sd.getInternalStorageList().get(0);
            }
        }

        return true;
    }

    //データ記録サービスが起動中か
    public boolean isServiceRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DataRecordService.class.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //記録ボタンの表示を更新
    public void changeState(int acquiringState){
        this.acquiringState = acquiringState;

        //状態に応じた色とテキストに変える
        int colorResId = R.color.not_recording;
        int textResId = R.string.not_recording;

        if(acquiringState == STATE_DURING_STARTUP){
            colorResId = R.color.startup;
            textResId = R.string.startup;
        }else if(acquiringState == STATE_RECORDING){
            colorResId = R.color.recording;
            textResId = R.string.recording;
        }else if(acquiringState == STATE_DURING_STOPPING){
            colorResId = R.color.stopping;
            textResId = R.string.stopping;
        }

        //背景色の変更
        startRecord.setBackgroundTintList(getColorStateList(colorResId));
        //テキストの変更
        startRecord.setText(getString(textResId));
    }

    //ツールバーの初期化
    public void initToolbar(){
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //戻るボタンを使えるようにする
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
                //終了していいか確認
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
                    //権限が許可されていないなら、許可を求める
                    Toast.makeText(getApplicationContext(), "権限が無いため記録ができません\n権限を許可してください", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //権限の許可を求めるダイアログを表示する
    public void createDialog(){
        new AlertDialog.Builder(DataRecordActivity.this)
                .setMessage(this.getString(R.string.permission_check))
                .setPositiveButton(this.getString(R.string.permit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(DataRecordActivity.this, new String[]{
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
    protected void onStart() {

        //レシーバーを登録
        receiver = new MyReceiver(this, receiveListener);
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SEND_IMG);
        intentFilter.addAction(ACTION_REC_STATE);
        registerReceiver(receiver, intentFilter);

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeState(isServiceRunning() ? STATE_RECORDING : STATE_NOT_RECORDING);
    }

    @Override
    protected void onDestroy() {
        //レシーバーの登録を解除
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    //画面を終了していいか確認
    public void confirmFinish(){
        if(acquiringState == STATE_RECORDING || acquiringState == STATE_DURING_STARTUP) {
            ConfirmDialogFragment.newInstance(getString(R.string.confirm_close_data_acquisition), getString(R.string.finish))
                    .show(getSupportFragmentManager(), "confirm_close");
        }else{
            finish();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                //戻るボタンが押されたとき
                confirmFinish();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    //終了確認ダイアログの終了ボタンが押された
    @Override
    public void onPositiveButtonClick() {

        finish();
    }

}