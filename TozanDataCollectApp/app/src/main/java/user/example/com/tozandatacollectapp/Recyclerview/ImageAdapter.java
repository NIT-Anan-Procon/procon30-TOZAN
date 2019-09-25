package user.example.com.tozandatacollectapp.Recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import user.example.com.tozandatacollectapp.R;
import user.example.com.tozandatacollectapp.sub.LoadPictureTask;

public class ImageAdapter extends RecyclerView.Adapter<ImageHolder>{

    public interface OnItemClickListener{
        void onItemClick(File file);
    }

    private List<File> dataList = new ArrayList<>();

    private OnItemClickListener onItemClickListener;


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageHolder holder, final int position) {

        final File file = dataList.get(position);

        holder.position = position;
        new LoadPictureTask(holder.imageView, holder, position).execute(file);
        holder.imageView.setImageBitmap(null);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClick(file);
            }
        });
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_image, parent,false);
        ImageHolder vh = new ImageHolder(inflate);
        return vh;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void addItem(File file){
        dataList.add(file);
        notifyItemInserted(dataList.size()-1);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}