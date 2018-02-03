package com.chk.mines.Views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.chk.mines.Interface.OnDialogButtonClickListener;
import com.chk.mines.R;

/**
 * Created by chk on 18-2-3.
 */

public class CustomDialog extends Dialog {

    Context mContext;
    View mLayoutView;
    int mLayoutId;

    Button mLeftButton;
    Button mRightButton;
    OnDialogButtonClickListener mOnDialogButtonClickListener;



    public CustomDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public CustomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    public CustomDialog(@NonNull Context context, int themeResId,int layoutId) {
        super(context,themeResId);
        this.mContext = context;
        this.mLayoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mLayoutId);

        mLeftButton = findViewById(R.id.leftButton);
        mRightButton = findViewById(R.id.rightButton);

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("CustomDialog","leftButtonClicked");
                if (mOnDialogButtonClickListener != null) {
                    mOnDialogButtonClickListener.onLeftClickListener();
                }
                dismiss();
            }
        });

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnDialogButtonClickListener != null) {
                    Log.i("CustomDialog","RightButtonClicked");
                    mOnDialogButtonClickListener.onRightClickListener();
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
