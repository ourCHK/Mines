package com.chk.mines.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.R;

/**
 * Created by chk on 18-2-3.
 */

public class CustomDialog extends Dialog {

    Context mContext;
    int mLayoutId;

    Button mLeftButton;
    Button mRightButton;
    TextView mTime;
    OnDialogButtonClickListener mOnDialogButtonClickListener;

    int time;   //-1时表示失败

    public CustomDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public CustomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    public CustomDialog(@NonNull Context context, int themeResId,int layoutId,int time) {
        super(context,themeResId);
        this.mContext = context;
        this.mLayoutId = layoutId;
        this.time = time;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mLayoutId);

        mLeftButton = findViewById(R.id.leftButton);
        mRightButton = findViewById(R.id.rightButton);
        if (time != -1) {   //-1表示弹出失败框
            mTime = findViewById(R.id.successTime);
            mTime.setText("时间："+time+"秒");
        }

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

    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener mOnDialogButtonClickListener) {
        this.mOnDialogButtonClickListener = mOnDialogButtonClickListener;
    }
}
