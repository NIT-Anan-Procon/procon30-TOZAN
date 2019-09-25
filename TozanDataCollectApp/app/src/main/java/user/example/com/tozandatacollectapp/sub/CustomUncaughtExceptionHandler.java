package user.example.com.tozandatacollectapp.sub;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

import user.example.com.tozandatacollectapp.TitleActivity;

public class CustomUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    public CustomUncaughtExceptionHandler(Context context) {
        mContext = context;

        // デフォルト例外ハンドラを保持する。
        mDefaultUncaughtExceptionHandler = Thread
                .getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // スタックトレースを文字列にします。
        StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        StackTraceElement[] stackTraces = ex.getStackTrace();

// スタックトレースの最大の深さを3に限定する
        String str = "";
        for(StackTraceElement s : stackTraces){
            str += s.toString();
        }
        Log.d("[error]", str);
        String stackTrace = stringWriter.toString();

        // スタックトレースを SharedPreferences に保存します。
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        preferences.edit().putString(TitleActivity.EX_STACK_TRACE, stackTrace)
                .commit();

        // デフォルト例外ハンドラを実行し、強制終了します。
        mDefaultUncaughtExceptionHandler.uncaughtException(thread, ex);
    }
}