package user.example.com.tozandatacollectapp.Recyclerview;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import user.example.com.tozandatacollectapp.R;

public class MountainDataHolder extends PositionHolder {
    public TextView name, pref, normal, special;
    public ImageView background;
    public ImageButton menu;

    public MountainDataHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        pref = itemView.findViewById(R.id.pref);
        normal = itemView.findViewById(R.id.normal);
        special = itemView.findViewById(R.id.special);
        background = itemView.findViewById(R.id.imageView);
        menu = itemView.findViewById(R.id.menu);
        itemView.setClipToOutline(true);
    }
}