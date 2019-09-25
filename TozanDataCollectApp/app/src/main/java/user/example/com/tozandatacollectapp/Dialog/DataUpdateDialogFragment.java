package user.example.com.tozandatacollectapp.Dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import user.example.com.tozandatacollectapp.R;
import user.example.com.tozandatacollectapp.sub.DataUpdateTask;

public class DataUpdateDialogFragment extends DialogFragment {

    public static DataUpdateDialogFragment newInstance(){
        return new DataUpdateDialogFragment();
    }

    private DialogBase dialogBase;

    private View dialogView;
    private TextView message;
    private ProgressBar progressBar;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        dialogBase = new DialogBase(getContext());

        dialogBase.setTitle(R.string.title_dialog_update);
        dialogBase.setConfirmButtonText(R.string.ok);
        dialogBase.setConfirmButtonVisibility(false);
        dialogBase.setOnConfirmButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDismiss(getDialog());
            }
        });

        setCancelable(false);

        dialogView = dialogBase.setContentView(R.layout.dialog_data_update);
        message = dialogView.findViewById(R.id.message);
        progressBar = dialogView.findViewById(R.id.progressBar);

        message.setText(R.string.update_data);

        DataUpdateTask dataUpdateTask = new DataUpdateTask(getContext(), new DataUpdateTask.ProgressCallback() {
            @Override
            public void taskFinished(boolean updateSuccess) {
                onDismiss(getDialog());
                Toast.makeText(getContext(), getContext().getString(R.string.update_success), Toast.LENGTH_LONG).show();
            }

            @Override
            public void badConnection() {
                progressBar.setVisibility(View.GONE);
                message.setText(getContext().getString(R.string.data_update_bad_connection));
                dialogBase.setConfirmButtonVisibility(true);
            }
        });

        dataUpdateTask.execute();
        return dialogBase.createDialog(getActivity());
    }

}