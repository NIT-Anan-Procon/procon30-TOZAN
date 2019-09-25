package user.example.com.tozandatacollectapp.Recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import user.example.com.tozandatacollectapp.R;

public class ContentHolder extends RecyclerView.ViewHolder {
    public TextView name;

    public ContentHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.item);
    }
}
