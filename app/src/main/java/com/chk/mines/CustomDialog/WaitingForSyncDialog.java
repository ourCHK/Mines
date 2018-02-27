package com.chk.mines.CustomDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.R;

/**
 * Created by chk on 18-2-26.
 */

public class WaitingForSyncDialog extends Dialog {

    Context mContext;
    int mLayoutId;

    OnDialogButtonClickListener mOnDialogButtonClickListener;

    public WaitingForSyncDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public WaitingForSyncDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_waiting_for_sync);

//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.width = display.getWidth() * 4 / 5 ; // 设置dialog宽度为屏幕的4/5
//        lp.height = display.getHeight() * 2 / 5;
//        getWindow().setAttributes(lp);
    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener mOnDialogButtonClickListener) {
        this.mOnDialogButtonClickListener = mOnDialogButtonClickListener;
    }
}
