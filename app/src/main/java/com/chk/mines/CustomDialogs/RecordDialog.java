package com.chk.mines.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.WindowManager;

import com.chk.mines.R;

/**
 * Created by chk on 18-3-15.
 */

public class RecordDialog extends Dialog {
    Context mContext;

    public RecordDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public RecordDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_record);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth(); // 设置dialog宽度为屏幕的4/5
        getWindow().setAttributes(lp);
    }
}
