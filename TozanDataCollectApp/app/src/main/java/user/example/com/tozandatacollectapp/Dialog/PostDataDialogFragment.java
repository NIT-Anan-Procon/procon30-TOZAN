package user.example.com.tozandatacollectapp.Dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import user.example.com.tozandatacollectapp.R;
import user.example.com.tozandatacollectapp.sub.DataUploadTask;
import user.example.com.tozandatacollectapp.sub.ZipManager;

public class PostDataDialogFragment extends DialogFragment {

    public static final String ARGS_STORAGEPATH = "storagePathKey";
    public static final String ARGS_MOUNTAINID = "mountainIdKey";

    public static PostDataDialogFragment newInstance(String storagePath, int mountainId)
    {
        PostDataDialogFragment fragment = new PostDataDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_STORAGEPATH, storagePath);
        args.putInt(ARGS_MOUNTAINID, mountainId);
        fragment.setArguments(args);
        return fragment;
    }

    private DialogBase dialogBase;
    private Dialog dialog;
    private TextView message;
    private ProgressBar progressBar;

    private String storagePath;
    private int mountainId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            storagePath = getArguments().getString(ARGS_STORAGEPATH);
            mountainId = getArguments().getInt(ARGS_MOUNTAINID);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        dialogBase = new DialogBase(getActivity());
        View contentView = dialogBase.setContentView(R.layout.dialog_post_data);

        dialogBase.setTitle("データを送信");

        message = contentView.findViewById(R.id.message);
        progressBar = contentView.findViewById(R.id.progressBar);
        dialogBase.setSubButtonText("キャンセル");
        dialogBase.setOnSubBttonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDismiss(dialog);
            }
        });

        setCancelable(false);
        initUploadTask();

        dialog = dialogBase.createDialog(getActivity());
        return dialog;
    }

    public void initUploadTask(){
        final DataUploadTask uploadTask = new DataUploadTask(getContext(), storagePath);
        uploadTask.setZipProgressListener(new ZipManager.ProgressCallback() {

            int max;

            @Override
            public void onCollectFileFinish(int max) {
                dialogBase.setConfirmButtonVisibility(false);
                dialogBase.setSubButtonVisibility(false);
                this.max = max;
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(false);
                progressBar.setMax(max);
            }

            @Override
            public void onProgressChange(int progress, File file) {
                progressBar.setProgress(progress);
                message.setText("(" + progress + "/" + max + ")\n " + file.getName() + "を圧縮中");
            }

            @Override
            public void onZipFinish(File out) {
                message.setText("データをアップロードしています…");
                progressBar.setIndeterminate(true);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                message.setText("データの圧縮に失敗しました。");
                dialogBase.setConfirmButtonVisibility(true);
                dialogBase.setSubButtonVisibility(true);
                dialogBase.setConfirmButtonText("再試行");
                dialogBase.setOnConfirmButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        initUploadTask();
                    }
                });
                dialogBase.setSubButtonText("キャンセル");
            }
        });
        uploadTask.setUploadStateCallback(new DataUploadTask.UploadStateCallback() {
            @Override
            public void onBadConnection() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void uploadFinished(boolean uploadSuccess, String msg) {
                progressBar.setVisibility(View.GONE);
                message.setText("アップロード" + (uploadSuccess ? "成功" : "失敗"));
                dialogBase.setConfirmButtonVisibility(true);
                if(uploadSuccess){
                    dialogBase.setConfirmButtonText("閉じる");
                    dialogBase.setOnConfirmButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onDismiss(dialog);
                        }
                    });
                }else{
                    dialogBase.setSubButtonVisibility(true);
                    dialogBase.setConfirmButtonText("再試行");
                    dialogBase.setOnConfirmButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            initUploadTask();
                        }
                    });
                    dialogBase.setSubButtonText("戻る");
                }
            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                uploadTask.execute(mountainId);
            }
        });
    }
}