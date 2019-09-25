package user.example.com.tozandatacollectapp.Dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import user.example.com.tozandatacollectapp.sub.MountainData;

public class ListDialogFragment extends DialogFragment {

    public static final String ARGS_MOUNTAIN_DATA = "mountain_data", ARGS_ITEM_LIST = "itemList";
    private MountainData mountainData;
    private String[] itemList;
    private ItemClickListener listener;

    public interface ItemClickListener{
        void onClick(MountainData mountainData, String[] itemList, int which);
    }

    public static ListDialogFragment newInstance(MountainData mountainData, String[] itemList){
        ListDialogFragment listDialogFragment = new ListDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_MOUNTAIN_DATA, mountainData);
        args.putStringArray(ARGS_ITEM_LIST, itemList);
        listDialogFragment.setArguments(args);
        return listDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mountainData = getArguments().getParcelable(ARGS_MOUNTAIN_DATA);
            itemList = getArguments().getStringArray(ARGS_ITEM_LIST);
        }

        if(getActivity() instanceof ItemClickListener){
            listener = (ItemClickListener) getActivity();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mountainData.getmName());
        builder.setItems(itemList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(listener != null)
                    listener.onClick(mountainData, itemList, i);
            }
        });
        return builder.create();
    }
}