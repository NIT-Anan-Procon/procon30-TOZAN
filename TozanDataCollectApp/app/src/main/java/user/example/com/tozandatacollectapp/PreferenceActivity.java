package user.example.com.tozandatacollectapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import user.example.com.tozandatacollectapp.Dialog.StoragePrefDialogFragment;

public class PreferenceActivity extends AppCompatActivity implements StoragePrefDialogFragment.OnConfirmListener {

    private PreferenceFragment prefFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        //画面に設定フラグメントを表示
        prefFragment = new PreferenceFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.replacement, prefFragment).commit();

        //ツールバーを初期化
        initToolbar();
    }

    public void initToolbar(){
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //戻るボタンを表示
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
                //戻るボタンで終了
                finish();
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }

        return result;
    }

    //ストレージ設定ダイアログの結果が決まった
    @Override
    public void onConfirm(String value) {
        prefFragment.onStoragePrefChanged(value);
    }
}
