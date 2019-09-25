package user.example.com.tozandatacollectapp.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import user.example.com.tozandatacollectapp.R;
import user.example.com.tozandatacollectapp.sub.StorageAcquirer;

public class StoragePrefDialogFragment extends DialogFragment {

    public static StoragePrefDialogFragment newInstance(){
        return new StoragePrefDialogFragment();
    }

    public interface OnConfirmListener{
        void onConfirm(String value);
    }

    private DialogBase dialogBase;

    private View dialogView;

    private String selectedKey;
    private List<RadioButton> radioButtons = new ArrayList<>();
    private HashMap<String, String> path = new HashMap<>();
    private RadioGroup storage;

    StorageAcquirer sa;
    private SharedPreferences data;
    private OnConfirmListener onConfirmListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        dialogBase = new DialogBase(getContext());

        try{
            onConfirmListener = (OnConfirmListener) getActivity();
        }catch (Exception e){
            e.printStackTrace();
        }

        dialogBase.setTitle(R.string.preftitle_storage);

        data = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sa = StorageAcquirer.getInstance();
        dialogView = dialogBase.setContentView(R.layout.dialog_pref_storage);

        storage = dialogView.findViewById(R.id.storage);

        List<String> intStorage = sa.getInternalStorageList();
        List<String> extStorage = sa.getExternalStorageList();
        List<String> all = new ArrayList<>();

        Context context = getContext();

        if(!intStorage.isEmpty()){
            TextView tx = new TextView(context);
            tx.setText(context.getString(R.string.int_storage));
            storage.addView(tx);

            for(String str : sa.getInternalStorageList()){
                addRadioButton(context, str);
                all.add(str);
            }
        }

        if(!extStorage.isEmpty()){
            TextView tx = new TextView(context);
            tx.setText(context.getString(R.string.ext_storage));
            storage.addView(tx);

            for(String str : sa.getExternalStorageList()){
                addRadioButton(context, str);
                all.add(str);
            }
        }

        if(!all.isEmpty()){
            String pathStr = data.getString(getString(R.string.key_storage), intStorage.get(0));
            int idx = all.indexOf(pathStr);
            if(idx == -1){
                idx = 0;
                //Toast.makeText(getActivity(), sa.toStorageRootPath(pathStr) + " が見つかりませんでした", Toast.LENGTH_LONG).show();
            }
            radioButtons.get(idx).setChecked(true);
        }

        dialogBase.setOnConfirmButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedKey != null) {
                    String value = path.get(selectedKey);
                    data.edit().putString(getString(R.string.key_storage), value).apply();
                    if (onConfirmListener != null)
                        onConfirmListener.onConfirm(value);
                    onDismiss(getDialog());
                }
            }
        });

        return dialogBase.createDialog(getActivity());
    }

    public void addRadioButton(Context c, String path){
        RadioButton radioButton = new RadioButton(c);

        //フォルダパスのうちの書き込み可能なストレージのルート部分を抜き出す。
        //例:/storage/emulated/0/Android/data/user.example.com.tozandatacollectapp/filesなら
        // /Android 以下を消去した, /storage/emulated/0 となる
        String key = StorageAcquirer.toStorageRootPath(path);
        this.path.put(key, path);
        radioButton.setText(key);
        radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    selectedKey = "" + compoundButton.getText();
                    //Toast.makeText(compoundButton.getContext(), selectedKey, Toast.LENGTH_SHORT).show();
                }
            }
        });
        radioButton.setPadding((int)(dialogBase.getDpScale()*4), 0, 0, 0);
        radioButtons.add(radioButton);
        storage.addView(radioButton);
    }
}