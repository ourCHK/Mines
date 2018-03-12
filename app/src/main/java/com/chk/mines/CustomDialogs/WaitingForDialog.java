package com.chk.mines.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.R;

/**
 * Created by chk on 18-2-26.
 * 等待对方确定的窗口
 */

public class WaitingForDialog extends Dialog {

    TextView mShowText;

    Context mContext;
    String showText;

    OnDialogButtonClickListener mOnDialogButtonClickListener;

    public WaitingForDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public WaitingForDialog(@NonNull Context context, int themeResId, String showText) {
        super(context, themeResId);
        this.mContext = context;
        this.showText = showText;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_waiting_for);

        mShowText = findViewById(R.id.showText);
        mShowText.setText(showText);
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.width = display.getWidth() * 4 / 5 ; // 设置dialog宽度为屏幕的4/5
//        lp.height = display.getHeight() * 2 / 5;
//        getWindow().setAttributes(lp);
    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener mOnDialogButtonClickListener) {
        this.mOnDialogButtonClickListener = mOnDialogButtonClickListener;
    }
}
