package user.example.com.tozandatacollectapp.Recyclerview;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import user.example.com.tozandatacollectapp.R;

public class ImageHolder extends PositionHolder {
    public ImageView imageView;
    public FrameLayout container;

    public ImageHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);
        container = itemView.findViewById(R.id.container);
        itemView.setClipToOutline(true);
    }
}
