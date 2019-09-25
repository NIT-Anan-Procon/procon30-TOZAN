package user.example.com.tozandatacollectapp.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import user.example.com.tozandatacollectapp.R;

public class TestLoginDialogFragment extends DialogFragment {

    public interface OnLoginClickListener{
        void onLoginSuccess(String id, String pass);
    }

    public static TestLoginDialogFragment newInstance()
    {
        return new TestLoginDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Activity activity = getActivity();
        final OnLoginClickListener listener = (OnLoginClickListener) activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_login, null);

        final TextInputEditText id = dialogView.findViewById(R.id.id), pass = dialogView.findViewById(R.id.password);

        dialogView.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idStr = id.getText().toString(), passStr = pass.getText().toString();
                if(!(idStr.isEmpty() || passStr.isEmpty())){
                    listener.onLoginSuccess(idStr, passStr);
                }
            }
        });

        builder.setView(dialogView)
                .setTitle("ログイン");

        return builder.create();
    }
}