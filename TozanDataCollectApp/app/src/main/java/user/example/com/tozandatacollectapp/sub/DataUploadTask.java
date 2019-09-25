package user.example.com.tozandatacollectapp.sub;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import user.example.com.tozandatacollectapp.R;

public class DataUploadTask extends AsyncTask<Integer, Void, Void> {

    public interface UploadStateCallback{
        void onBadConnection();
        void uploadFinished(boolean uploadSuccess, String message);
    }

    public static final int STATE_COMPLETE = 0, STATE_INTERRUPT = 1, STATE_BAD_CONNECTION = 2;

    private ConnectivityManager connectivityManager;
    private Handler handler;
    private String storagePath;
    private String uploadPageUrl;
    private Context context;

    private File cacheDir;

    private ZipManager.ProgressCallback zipProgressListener;
    private UploadStateCallback uploadStateCallback;

    public DataUploadTask(Context context, String storagePath){
        connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        handler = new Handler();
        this.storagePath = storagePath;
        this.uploadPageUrl = context.getString(R.string.page_upload);
        this.context = context;
        cacheDir = context.getCacheDir();
    }

    public DataUploadTask setZipProgressListener(ZipManager.ProgressCallback zipProgressListener) {
        this.zipProgressListener = zipProgressListener;
        return this;
    }

    public DataUploadTask setUploadStateCallback(UploadStateCallback uploadStateCallback) {
        this.uploadStateCallback = uploadStateCallback;
        return this;
    }

    @Override
    protected Void doInBackground(Integer... ids) {
        if(uploadStateCallback != null && handler != null) {

            for(int id : ids){
                File in = new File(storagePath, Integer.toString(id));
                File out = new File(cacheDir, id + ".zip");

                ZipManager.zip(in, out, new ZipManager.ProgressCallback() {
                    @Override
                    public void onCollectFileFinish(final int max) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                zipProgressListener.onCollectFileFinish(max);
                            }
                        });
                    }

                    @Override
                    public void onProgressChange(final int progress, final File file) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                zipProgressListener.onProgressChange(progress, file);
                            }
                        });
                    }

                    @Override
                    public void onZipFinish(final File out) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                zipProgressListener.onZipFinish(out);
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                zipProgressListener.onError(e);
                            }
                        });
                    }
                });

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "送信中", Toast.LENGTH_LONG).show();
                    }
                });

                try {
                    if(!hasConnection() && uploadStateCallback != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                uploadStateCallback.onBadConnection();
                            }
                        });
                    }else{

                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("up_file", out.getName(), RequestBody.create(MediaType.parse("application/x-compress"), out))
                                .build();

                        OkHttpClient client = new OkHttpClient.Builder()
                                .connectTimeout(10, TimeUnit.SECONDS)
                                .build();

                        Request request = new Request.Builder()
                                .url(uploadPageUrl)
                                .post(requestBody)
                                .build();

                        try {
                            final Response response = client.newCall(request).execute();
                            if (!response.isSuccessful()){
                                if(uploadStateCallback != null){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, response.code() + "", Toast.LENGTH_LONG).show();
                                            uploadStateCallback.uploadFinished(false, response.toString());
                                        }
                                    });
                                }
                                throw new IOException("Unexpected code " + response);
                            }else{
                                final String responseBody = response.body().string();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, response.code() + "", Toast.LENGTH_LONG).show();
                                        if(uploadStateCallback != null)
                                            uploadStateCallback.uploadFinished(true, response.toString());
                                        Toast.makeText(context, responseBody, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (final Exception e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(uploadStateCallback != null)
                                        uploadStateCallback.uploadFinished(false, e.toString());
                                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        return null;
    }


    private boolean hasConnection() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null) {
            return connectivityManager.getActiveNetworkInfo().isConnected();
        }
        return false;
    }

}
