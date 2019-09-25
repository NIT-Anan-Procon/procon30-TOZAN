package user.example.com.tozandatacollectapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import user.example.com.tozandatacollectapp.Dialog.ListDialogFragment;
import user.example.com.tozandatacollectapp.Dialog.PostDataDialogFragment;
import user.example.com.tozandatacollectapp.Recyclerview.MountainBrowseAdapter;
import user.example.com.tozandatacollectapp.Recyclerview.SimpleMountainBrowseAdapter;
import user.example.com.tozandatacollectapp.sub.DBOpenHelper;
import user.example.com.tozandatacollectapp.sub.FileDeleter;
import user.example.com.tozandatacollectapp.sub.MountainData;

import static user.example.com.tozandatacollectapp.TitleActivity.EXTRA_ACQUIRING;
import static user.example.com.tozandatacollectapp.TitleActivity.EXTRA_DATAPATH;

public class BrowseActivity extends AppCompatActivity implements ListDialogFragment.ItemClickListener {

    public static final String EXTRA_MOUNTAIN_DATA = "mountainData";
    private RecyclerView hasData, noData;
    private List<String> hasList = new ArrayList<>(), noList = new ArrayList<>();

    private TreeMap<String, Integer> prefList = new TreeMap<>();
    private List<String> prefNameList = new ArrayList<>();
    private SimpleMountainBrowseAdapter simpleMountainBrowseAdapter;
    private MountainBrowseAdapter mountainBrowseAdapter;

    private Spinner spinner;
    private ArrayAdapter<String> spinnerAdapter;

    private SharedPreferences data;

    private String storagePath;
    //データ記録中か
    private boolean isAcquiring;

    private DBOpenHelper helper;

    private String[] hasListItems, noListItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_data);

        Intent intent = getIntent();
        if(intent == null){
            finish();
            return;
        }

        //ストレージパス取得
        storagePath = intent.getStringExtra(EXTRA_DATAPATH);
        //データが記録中かを取得
        isAcquiring = intent.getBooleanExtra(EXTRA_ACQUIRING, false);

        //プリファレンス初期化
        data = PreferenceManager.getDefaultSharedPreferences(this);
        //データベース読み込み
        helper = new DBOpenHelper(getApplicationContext());

        //各UI初期化
        initRecyclerView();
        initToolbar();
    }

    public void initToolbar(){
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //戻るボタンを出す
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
                //ツールバーの戻るボタンを押すと戻るようにする
                finish();
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }

        return result;
    }

    public void initRecyclerView(){

        hasListItems = new String[]{getString(R.string.add_tozan_data), getString(R.string.add_image), getString(R.string.post_data), getString(R.string.delete_data)};
        noListItems = new String[]{getString(R.string.add_tozan_data), getString(R.string.add_image)};

        hasData = findViewById(R.id.hasData);
        noData = findViewById(R.id.noData);
        hasData.setNestedScrollingEnabled(false);

        hasData.setItemAnimator(new DefaultItemAnimator());
        noData.setItemAnimator(new DefaultItemAnimator());

        //アダプター初期化
        mountainBrowseAdapter = new MountainBrowseAdapter(storagePath);
        simpleMountainBrowseAdapter = new SimpleMountainBrowseAdapter();

        mountainBrowseAdapter.setOnItemClickListener(new MountainBrowseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int selection, final MountainData mountainData) {
                //メニュー表示
                ListDialogFragment.newInstance(mountainData, hasListItems).show(getSupportFragmentManager(), "dialog_list");
            }
        });
        mountainBrowseAdapter.setOnItemCountChangeListener(new MountainBrowseAdapter.OnItemCountChangeListener() {
            @Override
            public void onItemCountChanged(int count) {
                //データがあるか無いかで表示を切り替え
                findViewById(R.id.textView_no_data).setVisibility(count == 0? View.VISIBLE : View.GONE);
            }
        });
        simpleMountainBrowseAdapter.setOnItemClickListener(new SimpleMountainBrowseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int selection, final MountainData mountainData) {
                //メニュー表示
                ListDialogFragment.newInstance(mountainData, noListItems).show(getSupportFragmentManager(), "dialog_list");
            }
        });
        simpleMountainBrowseAdapter.setOnItemCountChangeListener(new SimpleMountainBrowseAdapter.OnItemCountChangeListener() {
            @Override
            public void onItemCountChanged(int count) {
                //データがあるか無いかで表示を切り替え
                findViewById(R.id.textView4).setVisibility(count == 0? View.VISIBLE : View.GONE);
            }
        });

        //アダプターをつける
        hasData.setAdapter(mountainBrowseAdapter);
        noData.setAdapter(simpleMountainBrowseAdapter);

        spinner = findViewById(R.id.prefSpinner);

        //アダプターをつける
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, prefNameList);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, final long l) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final List<MountainData> items = new ArrayList<>();
                        List<Integer> ids;
                        if(l == 0){
                            //全て表示
                            ids = new ArrayList<>(helper.getMountains().keySet());
                        }else{
                            //選択された都道府県の山を表示
                            ids = new ArrayList<>(helper.getPrefMountain(prefList.get(prefNameList.get((int) l))).keySet());
                        }
                        //データがある山を除外
                        for(String s : hasList){
                            ids.remove((Object)(Integer.parseInt(s)));
                        }

                        //データを設定、表示
                        items.addAll(helper.getMountainsFromId(ids));
                        spinner.post(new Runnable() {
                            @Override
                            public void run() {
                                simpleMountainBrowseAdapter.setItemList(items);
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //非同期で山データ読み込み
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //山IDをすべて読み込み
                List<Integer> allId = new ArrayList<>(helper.getMountains().keySet());

                //山データの場所
                File fileDir = new File(storagePath);

                for(String s : fileDir.list()){

                    //ディレクトリ名がID(数字)か
                    if(isNum(s)){
                        //山IDか
                        if(allId.contains(Integer.parseInt(s))){
                            File nView = new File(fileDir, s + "/resources/n_view");
                            //データのディレクトリがあるか
                            if(nView.exists()){
                                //あるリストに追加
                                hasList.add(s);
                                //全体のリストから除去
                                allId.remove((Object)Integer.parseInt(s));
                            }else{
                                //ないリストに追加
                                noList.add(s);
                            }
                        }
                    }
                }

                //全体リストの残ったデータをないリストに追加
                for(Integer i : allId){
                    noList.add(Integer.toString(i));
                }

                //あるリストとないリストの山データを取得
                final List<MountainData> hasDataList = helper.getMountainsFromIdStr(hasList);
                final List<MountainData> noDataList = helper.getMountainsFromIdStr(noList);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //データを表示
                        simpleMountainBrowseAdapter.setItemList(noDataList);
                        mountainBrowseAdapter.setItemList(hasDataList);
                    }
                });

                //県名リストの先頭に"全て"を追加
                prefNameList.add("全て");
                for (TreeMap.Entry<Integer, String> entry : helper.getPrefs().entrySet()){
                    //県データリストに追加
                    prefList.put(entry.getValue(), entry.getKey());
                    //県名リストに追加
                    prefNameList.add(entry.getValue());
                }

                //データリストが変わったことをアダプターに通知
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        spinnerAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

        //データ取得中の場合、記録画面を起動
        if(isAcquiring){
            String mountainId = data.getString(getString(R.string.key_mountain_id), null);
            if(mountainId != null){
                MountainData mountainData = helper.getMountainFromIdStr(mountainId);
                if(mountainData != null)
                    startDataAcquisition(mountainData);
            }
        }

    }

    //絶景写真の追加
    public void addImage(MountainData mountainData){
        //絶景写真の追加画面を起動
        startActivity(new Intent(BrowseActivity.this, AddImageActivity.class).putExtra(AddImageActivity.EX_MID, mountainData.getmId()));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //データ取得を開始
    public void startDataAcquisition(MountainData mountainData){

        //取得する山データのIDを保存
        data.edit().putString(getString(R.string.key_mountain_id), "" + mountainData.getmId()).apply();

        //データ記録画面を起動
        startActivity(
                new Intent(BrowseActivity.this,
                        DataRecordActivity.class)
                        .putExtra(EXTRA_MOUNTAIN_DATA, mountainData)
                        .putExtra(EXTRA_DATAPATH, storagePath)
        );
    }

    //データディレクトリ作成
    public void mkdirs(MountainData mountainData){

        Log.d(getClass().getSimpleName(), "mkdirs");


        int mId = mountainData.getmId();
        File storageDir = new File(storagePath);

        String[] dirNames = {"ads", "s_view", "n_view", "sounds"};
        for(String dirName : dirNames){
            File dir = new File(storageDir.getAbsolutePath(), mId + "/resources/" + dirName);

            if(!dir.exists())
                dir.mkdirs();
        }

        //ないリストとあるリストを変更
        simpleMountainBrowseAdapter.deleteItem(mountainData);
        mountainBrowseAdapter.addItem(mountainData);

    }

    //データを削除
    public void delete(final MountainData mountainData){

        Log.d(getClass().getSimpleName(), "mkdirs");

        //あるリストとないリストを変更
        mountainBrowseAdapter.deleteItem(mountainData);
        simpleMountainBrowseAdapter.addItem(mountainData);

        //データ本体と一時隔離用のファイル
        final File mountain = new File(storagePath, Integer.toString(mountainData.getmId()));
        String deleteName = "d" + mountainData.getmId();
        final File deleteMountain = new File(storagePath, deleteName);

        //ファイルの名前を隔離用のものに変える
        mountain.renameTo(deleteMountain);

        Snackbar.make(getWindow().getDecorView(), mountainData.getmName() + "を削除しました", 5000)
                .setAction("元に戻す", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //名前と各リストを元に戻す
                        deleteMountain.renameTo(mountain);
                        simpleMountainBrowseAdapter.deleteItem(mountainData);
                        mountainBrowseAdapter.addItem(mountainData);
                    }
                }).addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);

                //元に戻すボタンを押す以外の動作でバーが消えた場合
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //隔離ファイルを削除
                            FileDeleter.deleteFile(deleteMountain);
                        }
                    }).start();
                }
            }
        }).show();
    }

    //数字かどうかの判断
    private boolean isNum(String str){
        try{
            Integer.parseInt(str);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
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

    //リストダイアログのアイテムが押された
    @Override
    public void onClick(final MountainData mountainData, String[] itemList, int which) {
        //0と1は各ダイアログ共通
        if(which <= 1) {
            //ないリストからの選択の場合、山データディレクトリを作成、表示リストを変更
            if(itemList == noListItems)
                mkdirs(mountainData);

            if(which == 0) {
                //登山記録を追加
                startDataAcquisition(mountainData);
            }else {
                //絶景写真を追加
                addImage(mountainData);
            }
        }else if(which == 2) {
            //データをサーバに送信
            PostDataDialogFragment.newInstance(storagePath, mountainData.getmId()).show(getSupportFragmentManager(), "fragment_postData");
        }else if(which == 3) {
            //データを削除
            delete(mountainData);
        }
    }
}
