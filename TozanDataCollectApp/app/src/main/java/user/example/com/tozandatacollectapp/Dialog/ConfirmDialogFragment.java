package user.example.com.tozandatacollectapp.Dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class ConfirmDialogFragment extends DialogFragment {

    public static final String ARGS_NAME = "activityName";
    private String activityName;
    private PositiveButtonClickListener listener;

    public interface PositiveButtonClickListener{
        void onPositiveButtonClick();
    }

    public static ConfirmDialogFragment newInstance(String activityName){
        ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_NAME, activityName);
        confirmDialogFragment.setArguments(args);
        return confirmDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            activityName = getArguments().getString(ARGS_NAME);
        }

        if(getActivity() instanceof PositiveButtonClickListener){
            listener = (PositiveButtonClickListener) getActivity();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(activityName + "を終了してもよろしいですか？");
        builder.setPositiveButton("終了する", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(listener != null)
                    listener.onPositiveButtonClick();
            }
        });
        builder.setNegativeButton("キャンセル", null);
        return builder.create();
    }
}