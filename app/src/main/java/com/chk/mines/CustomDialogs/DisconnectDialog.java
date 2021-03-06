package com.chk.mines.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.R;

/**
 * Created by chk on 18-2-3.
 */

public class DisconnectDialog extends Dialog {

    Context mContext;

    Button mLeftButton;
    Button mRightButton;
    OnDialogButtonClickListener mOnDialogButtonClickListener;

    public DisconnectDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public DisconnectDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_network_disconnected);

        mLeftButton = findViewById(R.id.leftButton);
        mRightButton = findViewById(R.id.rightButton);

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDialogButtonClickListener != null) {
                    mOnDialogButtonClickListener.onLeftClick();
                }
                dismiss();
            }
        });

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnDialogButtonClickListener != null) {
                    mOnDialogButtonClickListener.onRightClick();
                }
                dismiss();
            }
        });

//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.width = display.getWidth() * 4 / 5 ; // 设置dialog宽度为屏幕的4/5
//        lp.height = display.getHeight() * 2 / 5;
//        getWindow().setAttributes(lp);
    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener mOnDialogButtonClickListener) {
        this.mOnDialogButtonClickListener = mOnDialogButtonClickListener;
    }
}
