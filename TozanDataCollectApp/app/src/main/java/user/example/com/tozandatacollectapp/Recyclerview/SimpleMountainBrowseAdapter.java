package user.example.com.tozandatacollectapp.Recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import user.example.com.tozandatacollectapp.R;
import user.example.com.tozandatacollectapp.sub.MountainData;

public class SimpleMountainBrowseAdapter extends RecyclerView.Adapter<SimpleMountainDataHolder>{

    public interface OnItemClickListener{
        void onItemClick(int selection, MountainData mountainData);
    }
    public interface OnItemCountChangeListener{
        void onItemCountChanged(int count);
    }

    public static final String TAG = MountainBrowseAdapter.class.getSimpleName();

    private List<MountainData> mountainDataList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private OnItemCountChangeListener onItemCountChangeListener;

    @Override
    public void onBindViewHolder(@NonNull final SimpleMountainDataHolder holder, final int position) {

        final MountainData md = mountainDataList.get(position);

        final String name = md.getmName();
        final String pref = md.getpName();

        holder.name.setText(name);
        holder.pref.setText(pref);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClick(md.getmId(), md);
            }
        });

    }

    @NonNull
    @Override
    public SimpleMountainDataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_browse_simple, parent,false);
        SimpleMountainDataHolder vh = new SimpleMountainDataHolder(inflate);
        return vh;
    }

    @Override
    public int getItemCount() {
        return mountainDataList.size();
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
