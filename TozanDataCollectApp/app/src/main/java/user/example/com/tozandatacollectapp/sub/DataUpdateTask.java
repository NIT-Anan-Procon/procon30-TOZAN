package user.example.com.tozandatacollectapp.sub;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import user.example.com.tozandatacollectapp.R;

public class DataUpdateTask extends AsyncTask<Void, Void, Void> {

    public interface ProgressCallback{
        void taskFinished(boolean updateSuccess);
        void badConnection();
    }

    private ProgressCallback progressCallback;
    private ConnectivityManager connectivityManager;
    private Handler handler;

    private String categories, prefectures, mountains;
    private DBOpenHelper helper;

    public static final String TAG = DataUpdateTask.class.getSimpleName();

    public DataUpdateTask(Context context, ProgressCallback callback){
        connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.progressCallback = callback;
        
        String urlFormat = "https://docs.google.com/spreadsheets/%s/pub?output=csv";

        categories = String.format(urlFormat, context.getString(R.string.categories));
        prefectures = String.format(urlFormat, context.getString(R.string.prefectures));
        mountains = String.format(urlFormat, context.getString(R.string.mountains));

        helper = new DBOpenHelper(context);
        
        handler = new Handler();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        
        if(!hasConnection()){
            progressCallback.badConnection();
            return null;
        }
        
        List<String> value = new ArrayList<>();

        if(update(categories, value)) {
            helper.registerCategories(value);
        }else{
            taskFinish(false);
            return null;
        }

        if(update(prefectures, value)) {
            helper.registerPrefectures(value);
        }else{
            taskFinish(false);
            return null;
        }

        if(update(mountains, value)) {
            helper.registerMountains(value);
        }else{
            taskFinish(false);
            return null;
        }

        taskFinish(true);

        return null;

    }

    private void taskFinish(final boolean taskFinished){
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressCallback.taskFinished(taskFinished);
            }
        });
    }
    
    public List<String> downloadHtml(String url){
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        ArrayList<String> lineList = new ArrayList<String>();
        try {
            //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.anan-nct.ac.jp", 8080));
            //URLConnection conn = new URL(url).openConnection(proxy);
            URLConnection conn = new URL(url).openConnection();
            is = conn.getInputStream();
            isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);

            String line = null;
            while((line = br.readLine()) != null) {
                lineList.add(line);
            }
            return lineList;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }finally {
            try {
                br.close();
                br = null;
            }catch(IOException e) {
            }
            try {
                isr.close();
                isr = null;
            }catch(IOException e) {
            }
            try {
                is.close();
                is = null;
            }catch(IOException e) {
            }
        }
    }

    private boolean hasData(List list){
        return list != null && !list.isEmpty();
    }
    
    private boolean update(String dataUrl, List<String> value){
        int count = 0;
        while(count < 5){
            value.clear();
            value.addAll(downloadHtml(dataUrl));
            if(hasData(value) && value.get(0).contains("id")){
                Log.d(TAG, value.toString());
                break;
            }
            count++;
        }
        return count < 5;
    }

    private boolean hasConnection() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null) {
            return connectivityManager.getActiveNetworkInfo().isConnected();
        }
        return false;
    }

}
