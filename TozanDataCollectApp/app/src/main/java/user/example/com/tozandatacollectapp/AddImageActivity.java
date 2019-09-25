package user.example.com.tozandatacollectapp;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import user.example.com.tozandatacollectapp.Recyclerview.ImageAdapter;
import user.example.com.tozandatacollectapp.sub.ImageCopyTask;
import user.example.com.tozandatacollectapp.sub.StorageAcquirer;

public class AddImageActivity extends AppCompatActivity {

    public static final String EX_MID = "mId";
    public static final int RC_GALLERY = 1284;
    private Button addImage;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private StorageAcquirer storageAcquirer;
    private SharedPreferences data;
    private String storagePath;
    private int mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mId = getIntent().getIntExtra(EX_MID, -1);
        if(mId == -1){
            finish();
            return;
        }

        setContentView(R.layout.activity_add_image);
        initToolbar();

        addImage = findViewById(R.id.button_add);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ギャラリー呼び出し
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getApplicationContext().getString(R.string.choose_image)), RC_GALLERY);
            }
        });

        recyclerView = findViewById(R.id.imageList);
        imageAdapter = new ImageAdapter();
        recyclerView.setAdapter(imageAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final Handler h = new Handler();
        final Toast toast = new Toast(getApplicationContext());
        imageAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final File file) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final ImageView imageView = new ImageView(getApplicationContext());
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        toast.setView(imageView);
                        toast.setDuration(Toast.LENGTH_LONG);
                        final Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bmp);
                                toast.show();
                            }
                        });
                    }
                }).start();
            }
        });

        StorageAcquirer.init(this);
        storageAcquirer = StorageAcquirer.getInstance();
        data = PreferenceManager.getDefaultSharedPreferences(this);

        storagePath = data.getString(getString(R.string.key_storage), storageAcquirer.getInternalStorageList().get(0));

        new Thread(new Runnable() {
            @Override
            public void run() {
                final File nView = new File(storagePath + "/" + mId + "/resources/s_view");
                for(final String s : nView.list()){
                    /*try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            imageAdapter.addItem(new File(nView, s));
                        }
                    });
                }
            }
        }).start();

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

    public void copyFiles(Uri... uris){
        new ImageCopyTask(getApplicationContext(), storagePath, mId)
                .setProgressCallback(new ImageCopyTask.ProgressCallback() {
                    @Override
                    public void onCopyStart(int max) {

                    }

                    @Override
                    public void onProgressChange(int progress) {

                    }

                    @Override
                    public void onCopyFinish(File out) {
                        imageAdapter.addItem(out);
                    }

                    @Override
                    public void onTaskFinish() {

                    }
                }).execute(uris);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_GALLERY && resultCode == RESULT_OK) {
            if(data.getData() != null){
                // 単一選択
                Uri uri = data.getData();
                copyFiles(uri);
            } else {
                // 複数選択(EXTRA_ALLOW_MULTIPLE)
                ClipData clipData = data.getClipData();
                Uri[] uris = new Uri[clipData.getItemCount()];
                for(int i = 0; i < clipData.getItemCount(); i++){
                    ClipData.Item item = clipData.getItemAt(i);
                    uris[i] = item.getUri();
                }

                copyFiles(uris);
            }
        }
    }
}
