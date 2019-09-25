package user.example.com.tozandatacollectapp.Recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import user.example.com.tozandatacollectapp.R;

public class MapContentAdapter extends RecyclerView.Adapter<ContentHolder>{

    public interface OnItemClickListener{
        void onItemClick(int selection, String value);
    }

    private List<Integer> idList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();
    //private List<Boolean> selection;
    private int colorDefault, colorSelected;

    OnItemClickListener onItemClickListener;

    //private RecyclerView recyclerView;

    public MapContentAdapter(Context context) {
        colorDefault = context.getColor(R.color.colorTextLight);
        colorSelected = context.getColor(R.color.colorAccent);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        //this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        //this.recyclerView = null;
    }

    @Override
    public void onBindViewHolder(@NonNull final ContentHolder holder, final int position) {

        final String data = nameList.get(position);
        final Integer id = idList.get(position);

        holder.name.setText(data);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClick(id, data);
            }
        });
    }

    @NonNull
    @Override
    public ContentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_simple, parent,false);
        ContentHolder vh = new ContentHolder(inflate);
        return vh;
    }

    @Override
    public int getItemCount() {
        return idList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setItemList(TreeMap<Integer, String> dataList){
        idList.clear();
        nameList.clear();

        if(dataList.isEmpty()){

        }else{
            List<Integer> iList = new ArrayList<>(dataList.keySet());
            for(Integer key : iList){
                idList.add(key);
                nameList.add(dataList.get(key));
            }
        }
        notifyDataSetChanged();
    }

}
