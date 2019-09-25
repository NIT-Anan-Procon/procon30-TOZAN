package user.example.com.tozandatacollectapp.sub;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import user.example.com.tozandatacollectapp.R;

public class ToggleImageButton extends FrameLayout {

    public ToggleImageButton(Context context) {
        this(context, null, 0);
    }

    public ToggleImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToggleImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public interface OnToggleStateChangeListener {
        void onToggleStateChange(View view, boolean toggleState);
    }

    private boolean toggleState = false;
    private ImageButton imageButton;
    private int resMode = 0;
    private int[] resIds = new int[]{R.drawable.toggle_off, R.drawable.toggle_on};
    private Drawable[] drawables;
    private Bitmap[] bitmaps;
    private OnToggleStateChangeListener onToggleStateChangeListener;

    public void init(AttributeSet attrs){
        Context c = getContext();
        imageButton = new ImageButton(c);
        imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageButton.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(imageButton);

        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setToggleState(!toggleState);
                if(onToggleStateChangeListener != null)
                    onToggleStateChangeListener.onToggleStateChange(imageButton, toggleState);
            }
        });

        if(attrs == null) return;

        TypedArray a = c.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ToggleImageButton,
                0, 0);

        Drawable offDrawable, onDrawable;

        try {
            offDrawable = a.getDrawable(R.styleable.ToggleImageButton_offSrc);
            onDrawable = a.getDrawable(R.styleable.ToggleImageButton_onSrc);
            if(offDrawable != null && onDrawable != null) {
                resMode = 1;
                drawables = new Drawable[]{offDrawable, onDrawable};
            }
            toggleState = a.getBoolean(R.styleable.ToggleImageButton_toggleState, false);
        } finally {
            a.recycle();
        }

        setToggleState(toggleState);

    }

    public void setImageResource(int off, int on) {
        resMode = 0;
        resIds = new int[]{off, on};
        setToggleState(toggleState);
    }

    public void setImageDrawable(Drawable off, Drawable on){
        resMode = 1;
        drawables = new Drawable[]{off, on};
        setToggleState(toggleState);
    }

    public void setImageBitmap(Bitmap off, Bitmap on){
        resMode = 2;
        bitmaps = new Bitmap[]{off, on};
        setToggleState(toggleState);
    }

    public void setToggleState(boolean toggleState) {
        switch (resMode){
            case 0:
                imageButton.setImageResource(toggleState ? resIds[1] : resIds[0]);
                break;
            case 1:
                imageButton.setImageDrawable(toggleState ? drawables[1] : drawables[0]);
                break;
            case 2:
                imageButton.setImageBitmap(toggleState ? bitmaps[1] : bitmaps[0]);
                break;
        }
        this.toggleState = toggleState;
    }

    private Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void setOnToggleStateChangeListener(OnToggleStateChangeListener onToggleStateChangeListener) {
        this.onToggleStateChangeListener = onToggleStateChangeListener;
    }
}
