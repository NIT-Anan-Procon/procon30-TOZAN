package user.example.com.tozandatacollectapp.Dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class ErrorDialogFragment extends DialogFragment {

    private static String mExStackTrace;

    public static ErrorDialogFragment newInstance(String exStackTrace){
            mExStackTrace = exStackTrace;
            return new ErrorDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("前回強制終了したときのエラー情報を送信します。\nよろしいですか？");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + "1166204@st.anan-nct.ac.jp"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "不具合の報告");
                intent.putExtra(Intent.EXTRA_TEXT, mExStackTrace);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("キャンセル", null);
        return builder.create();
    }
}