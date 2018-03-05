package com.chk.mines.CustomDialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.Interfaces.OnTimeEnd;
import com.chk.mines.R;
import com.chk.mines.Utils.Constant;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chk on 18-2-3.
 * 确认重新开始的对话框
 */

public class RestartDialog extends Dialog implements OnTimeEnd {

    private final static String TAG = RestartDialog.class.getSimpleName();

    Context mContext;

    Button mLeftButton;
    Button mRightButton;
    OnDialogButtonClickListener mOnDialogButtonClickListener;

    Handler mDialogHandler;
    Timer timer;
    int time;

    public RestartDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public RestartDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_restart_confirm);

        mDialogHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constant.TIME_CHANGED:
                        mLeftButton.setText("拒绝("+time+")");
                        if (time == 0) {
                            timer.cancel();
                            mLeftButton.performClick();
                        }
                        break;
                }
            }
        };

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

        Log.i(TAG,"onCreate");
    }

    @Override
    public void onTimeEnd() {

    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener mOnDialogButtonClickListener) {
        this.mOnDialogButtonClickListener = mOnDialogButtonClickListener;
    }

    @Override
    public void show() {
        super.show();
        Log.i(TAG,"show");
    }

    @Override
    public void hide() {
        super.hide();
        Log.i(TAG,"hide");

    }

    @Override
    public void dismiss() {
        super.dismiss();
        Log.i(TAG,"dismiss");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"start");
        time = 5;
        mLeftButton.setText("拒绝("+time+")");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                time--;
                mDialogHandler.sendEmptyMessage(Constant.TIME_CHANGED);
            }
        },1000,1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"stop");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.i(TAG,"cancel");
    }

}
