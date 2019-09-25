package user.example.com.tozandatacollectapp.Recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import user.example.com.tozandatacollectapp.R;

public class SimpleMountainDataHolder extends RecyclerView.ViewHolder {
    public TextView name, pref;

    public SimpleMountainDataHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        pref = itemView.findViewById(R.id.pref);
    }
}
