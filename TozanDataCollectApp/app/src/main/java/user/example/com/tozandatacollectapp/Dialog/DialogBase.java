package user.example.com.tozandatacollectapp.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import user.example.com.tozandatacollectapp.R;

public class DialogBase {

    private LayoutInflater layoutInflater;

    private View baseView;
    private TextView title;
    private ImageView background;
    private FrameLayout contentFrame;
    private Button confirmBtn, subBtn;
    private float dpScale;

    public DialogBase(Context context){
        this.layoutInflater = LayoutInflater.from(context);

        dpScale = context.getResources().getDisplayMetrics().density;

        baseView = layoutInflater.inflate(R.layout.dialog_base, null);
        title = baseView.findViewById(R.id.title);
        background = baseView.findViewById(R.id.imageView);
        contentFrame = baseView.findViewById(R.id.content);
        confirmBtn = baseView.findViewById(R.id.confirm);
        subBtn = baseView.findViewById(R.id.sub);
    }

    public void setBackground(Bitmap srcBmp){
        background.setImageBitmap(srcBmp);
    }

    public void setBackground(Drawable srcDrawable){
        background.setImageDrawable(srcDrawable);
    }

    public void setBackground(int srcResId){
        background.setImageResource(srcResId);
    }

    public void setTitle(CharSequence srcStr){
        title.setText(srcStr);
    }

    public void setTitle(int srcResId){
        title.setText(srcResId);
    }

    public void setContentView(View content){
        contentFrame.removeAllViews();
        contentFrame.addView(content);
    }

    public View setContentView(int resId){
        contentFrame.removeAllViews();
        return layoutInflater.inflate(resId, contentFrame);
    }

    public void setOnConfirmButtonClickListener(View.OnClickListener listener) {
        confirmBtn.setOnClickListener(listener);
    }

    public Dialog createDialog(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(baseView);
        return builder.create();
    }

    protected float getDpScale(){
        return dpScale;
    }

    public void setConfirmButtonText(CharSequence src){
        confirmBtn.setText(src);
    }

    public void setConfirmButtonText(int srcResId){
        confirmBtn.setText(srcResId);
    }

    public void setConfirmButtonEnabled(boolean enabled){
        confirmBtn.setEnabled(enabled);
    }

    public void setConfirmButtonVisibility(boolean visibility){
        confirmBtn.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    public void setSubButtonText(CharSequence src){
        subBtn.setText(src);
    }

    public void setSubButtonText(int srcResId){
        subBtn.setText(srcResId);
    }

    public void setSubButtonEnabled(boolean enabled){
        subBtn.setEnabled(enabled);
    }

    public void setSubButtonVisibility(boolean visibility){
        subBtn.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    public void setOnSubBttonClickListener(View.OnClickListener listener){
        subBtn.setOnClickListener(listener);
    }
}
