package user.example.com.tozandatacollectapp.sub;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;

import user.example.com.tozandatacollectapp.Recyclerview.PositionHolder;

public class LoadPictureTask extends AsyncTask<File, Void, Void> {
    private ImageView imageView;
    private PositionHolder holder;
    private int originalPosition;

    private Bitmap bmp;
    public LoadPictureTask(ImageView imageView, PositionHolder holder, int originalPosition){
        this.imageView = imageView;
        this.holder = holder;
        this.originalPosition = originalPosition;
    }
    @Override
    protected Void doInBackground(File... files) {
        if(holder.position != originalPosition) return null;
        bmp = BitmapUtil.createBitmap(files[0], 128);
        imageView.post(new Runnable() {
            @Override
            public void run() {
                if(holder.position == originalPosition)
                    imageView.setImageBitmap(bmp);
                else bmp = null;
            }
        });
        return null;
    }
}
