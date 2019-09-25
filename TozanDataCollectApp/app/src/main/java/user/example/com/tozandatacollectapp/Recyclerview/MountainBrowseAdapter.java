package user.example.com.tozandatacollectapp.Recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import user.example.com.tozandatacollectapp.R;
import user.example.com.tozandatacollectapp.sub.LoadPictureTask;
import user.example.com.tozandatacollectapp.sub.MountainData;

public class MountainBrowseAdapter extends RecyclerView.Adapter<MountainDataHolder>{

    public interface OnItemClickListener{
        void onItemClick(int selection, MountainData mountainData);
    }
    public interface OnItemCountChangeListener{
        void onItemCountChanged(int count);
    }

    public static final String TAG = MountainBrowseAdapter.class.getSimpleName();

    private List<MountainData> mountainDataList = new ArrayList<>();
    private String fileDir;
    private OnItemClickListener onItemClickListener;
    private OnItemCountChangeListener onItemCountChangeListener;

    public MountainBrowseAdapter(String fileDir){
        this.fileDir = fileDir;
    }

    @Override
    public void onBindViewHolder(@NonNull final MountainDataHolder holder, final int position) {

        Log.d(getClass().getSimpleName(), "position:" + position);

        final MountainData md = mountainDataList.get(position);

        final String name = md.getmName();
        final String pref = md.getpName();

        holder.name.setText(name);
        holder.pref.setText(pref);

        File nView = new File(fileDir, md.getmId() + "/resources/n_view");
        File sView = new File(fileDir, md.getmId() + "/resources/s_view");

        holder.position = position;

        boolean hasSpecial = hasChild(sView);

        if(!hasSpecial){
            holder.special.setText("絶景写真無し");
        }else{
            holder.special.setText(String.format("%s枚の絶景写真", Integer.toString(sView.list().length)));
            new LoadPictureTask(holder.background, holder, position).execute(new File(sView, sView.list()[0]));
            holder.background.setImageBitmap(null);
        }

        if(!hasChild(nView)){
            holder.normal.setText("写真無し");
            holder.background.setImageResource(R.drawable.img_mountain_small);
        }else{
            holder.normal.setText(String.format("%s枚の写真", Integer.toString(nView.list().length)));
            if(!hasSpecial){
                new LoadPictureTask(holder.background, holder, position).execute(new File(nView, nView.list()[0]));
                holder.background.setImageBitmap(null);
            }
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClick(md.getmId(), md);
                return false;
            }
        });

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClick(md.getmId(), md);
            }
        });
    }

    @NonNull
    @Override
    public MountainDataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_browse, parent,false);
        MountainDataHolder vh = new MountainDataHolder(inflate);
        return vh;
    }

    @Override
    public int getItemCount() {
        return mountainDataList.size();
    }

    public boolean hasChild(File parent){
        return parent != null && parent.exists() && parent.list().length > 0;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemCountChangeListener(OnItemCountChangeListener onItemCountChangeListener) {
        this.onItemCountChangeListener = onItemCountChangeListener;
    }

    public void addItem(MountainData mountainData){
        int position = Collections.binarySearch(mountainDataList, mountainData);
        if(position >= 0) return;
        position = ~position;
        mountainDataList.add(position, mountainData);
        notifyItemInserted(position);
        notifyItemRangeChanged(0, mountainDataList.size()-1);

        if(onItemCountChangeListener != null)
            onItemCountChangeListener.onItemCountChanged(getItemCount());

        Log.d(TAG, "add");

    }

    public void setItemList(List<MountainData> dataList){

        mountainDataList.clear();
        notifyDataSetChanged();

        for(int i = 0; i < dataList.size(); i++){
            mountainDataList.add(dataList.get(i));
            notifyItemInserted(i);
        }

        notifyItemRangeChanged(0, mountainDataList.size());

        if(onItemCountChangeListener != null)
            onItemCountChangeListener.onItemCountChanged(getItemCount());

        Log.d(TAG, "set");

    }

    public void deleteItem(MountainData mountainData){
        int idx = mountainDataList.indexOf(mountainData);
        if(idx < 0) return;
        mountainDataList.remove(idx);
        notifyItemRemoved(idx);
        notifyItemRangeChanged(0, mountainDataList.size());

        Log.d(TAG, "delete");

    }

}
