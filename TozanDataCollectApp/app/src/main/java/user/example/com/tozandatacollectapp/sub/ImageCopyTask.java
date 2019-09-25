package user.example.com.tozandatacollectapp.sub;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageCopyTask extends AsyncTask<Uri, Void, Void> {

    public interface ProgressCallback{
        void onCopyStart(int max);
        void onProgressChange(int progress);
        void onCopyFinish(File out);
        void onTaskFinish();
    }

    private ProgressCallback progressCallback;
    private File nView;
    private Handler handler;
    private ContentResolver contentResolver;

    public ImageCopyTask(Context context, String storagePath, int mId){
        handler = new Handler();
        nView = new File(storagePath + "/" + mId + "/resources/s_view");
        contentResolver = context.getContentResolver();
    }


    public ImageCopyTask setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
        return this;
    }

    @Override
    protected Void doInBackground(final Uri... uris) {

        if(uris == null) return null;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if(progressCallback != null)
                    progressCallback.onCopyStart(uris.length);
            }
        });

        for (int i = 0; i < uris.length; i++) {
            Uri uri = uris[i];

            final int progress = i;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(progressCallback != null)
                        progressCallback.onProgressChange(progress);
                }
            });

            Bitmap img = null;
            InputStream in = null;
            try {
                in = contentResolver.openInputStream(uri);
                img = BitmapFactory.decodeStream(in);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }

            final File out = new File(nView, System.currentTimeMillis() + ".jpg");
            FileOutputStream outStream = null;
            try {
                outStream = new FileOutputStream(out);
                img.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(progressCallback != null)
                            progressCallback.onCopyFinish(out);
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outStream = null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(progressCallback != null)
                    progressCallback.onTaskFinish();
            }
        });
    }
}
