package user.example.com.tozandatacollectapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import user.example.com.tozandatacollectapp.Dialog.StoragePrefDialogFragment;

public class PreferenceActivity extends AppCompatActivity implements StoragePrefDialogFragment.OnConfirmListener {

    private PreferenceFragment prefFragment;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        prefFragment = new PreferenceFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.replacement, prefFragment).commit();
        initToolbar();
    }

    public void initToolbar(){
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean result = true;

        switch (id){
            case android.R.id.home:
                finish();
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }

        return result;
    }

    @Override
    public void onConfirm(String value) {
        prefFragment.onStoragePrefChanged(value);
    }
}
