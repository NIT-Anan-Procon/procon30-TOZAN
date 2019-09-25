package user.example.com.tozandatacollectapp.sub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;

import user.example.com.tozandatacollectapp.R;

public class MyReceiver extends BroadcastReceiver {

    public static final String ACTION_REC_STATE = "recState", ACTION_SEND_IMG = "imgData";

    public static final String
            DATA_REC_STATE = "recState",
            DATA_IMG_PATH = "imgPath",
            DATA_IMG_TIME = "imgTime";

    public interface ReceiveListener{
        void onAcquiringStateReceived(int state);
        void onImageDateReceived(String path, String dateStr);
    }

    private ReceiveListener receiveListener;
    private SimpleDateFormat dateFormat;

    public MyReceiver(Context context, ReceiveListener receiveListener){
        this.receiveListener = receiveListener;
        this.dateFormat = new SimpleDateFormat(context.getString(R.string.format_date));
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //アクションを取得
        String action = intent.getAction();

        if(action.equals(ACTION_REC_STATE)){
            int state = intent.getIntExtra(DATA_REC_STATE, -1);
            if(receiveListener != null)
                receiveListener.onAcquiringStateReceived(state);
        }else if(action.equals(ACTION_SEND_IMG)){
            //画像のパスと撮影時間を受け取り
            String path = intent.getStringExtra(DATA_IMG_PATH);
            long time = intent.getLongExtra(DATA_IMG_TIME, -1);

            if (path == null || path.isEmpty()) return;
            File file = new File(path);
            if (file.exists()) {
                if(receiveListener != null)
                    receiveListener.onImageDateReceived(path, dateFormat.format(time));
            }
        }

        Log.d("BroadcastReceiver", "onreceive");
    }
}
