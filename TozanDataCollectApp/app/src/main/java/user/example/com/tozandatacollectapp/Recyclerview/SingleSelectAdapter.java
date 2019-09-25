package user.example.com.tozandatacollectapp.Recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import user.example.com.tozandatacollectapp.R;
import user.example.com.tozandatacollectapp.sub.MountainData;

public class SingleSelectAdapter extends RecyclerView.Adapter<SimpleMountainDataHolder>{

    public interface OnSelectionChangeListener{
        void onSelectionChange(int selection);
    }

    private List<MountainData> list;
    private List<Boolean> selection;
    private int colorDefault, colorSelected;
    private MountainData selectedItem;

    private OnSelectionChangeListener onSelectionChangeListener;

    private RecyclerView recyclerView;

    public SingleSelectAdapter(List<MountainData> list, Context context) {
        this.list = list;
        this.selection = new ArrayList<>();
        for(boolean bdt : new boolean[list.size()]) selection.add(false);

        colorDefault = context.getColor(R.color.colorTextLight);
        colorSelected = context.getColor(R.color.colorAccent);
    }

    public boolean hasSelection(){
        return selection.contains(true);
    }
    public ArrayList<Boolean> getSelection() {
        return new ArrayList<>(selection);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    @Override
    public void onBindViewHolder(@NonNull final SimpleMountainDataHolder holder, final int position) {

        final MountainData data = list.get(position);

        holder.name.setText(data.getmName());
        holder.pref.setText(data.getpName());

        Log.d("contentpicker", position + ":" + selection.get(position));
        setSelection(holder, selection.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedItem = data;
                changeSelection(position);
            }
        });
        if(onSelectionChangeListener != null)
            onSelectionChangeListener.onSelectionChange(hasSelection() ? selection.indexOf(Boolean.TRUE) : -1);
    }

    @NonNull
    @Override
    public SimpleMountainDataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_mountain_data, parent,false);
        SimpleMountainDataHolder vh = new SimpleMountainDataHolder(inflate);
        return vh;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public int getItemPosition(String item){
        int index = -1;
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).equals(item)) index = i;
        }
        return index;
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener onSelectionChangeListener) {
        this.onSelectionChangeListener = onSelectionChangeListener;
    }

    public void changeSelection(int position){
        if(recyclerView == null) return;
        boolean bdt;
        for(int i = 0; i < list.size(); i++){
            bdt = (i == position);
            boolean bdt2 = selection.get(i);
            if(bdt != bdt2)
                selection.set(i, bdt);
            else if(bdt) {
                selection.set(i, false);
                selectedItem = null;
            }

            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
            if(viewHolder instanceof SimpleMountainDataHolder) {
                SimpleMountainDataHolder holder = (SimpleMountainDataHolder) viewHolder;
                setSelection(holder, selection.get(i));
            }else{
                if(bdt != bdt2){
                    notifyItemChanged(i);
                }
            }
        }
        if(onSelectionChangeListener != null)
            onSelectionChangeListener.onSelectionChange(hasSelection() ? position : -1);
    }

    public void setSelection(SimpleMountainDataHolder holder, boolean bdt){
        holder.name.setTextColor(bdt ? colorSelected : colorDefault);
    }



    public MountainData getSelectedItem(){
        return selectedItem;
    }

    public void addItem(MountainData mountainData){
        changeSelection(-1);
        final int idx = list.size();
        list.add(mountainData);
        selection.add(true);
        notifyItemInserted(idx);
        if(onSelectionChangeListener != null)
            onSelectionChangeListener.onSelectionChange(idx);
    }
}
