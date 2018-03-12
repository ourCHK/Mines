package com.chk.mines.CustomDialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.Interfaces.OnTimeEnd;
import com.chk.mines.R;

import java.util.Timer;

/**
 * Created by chk on 18-2-3.
 * 离开的对话框
 */

public class LeaveForDialog extends Dialog implements OnTimeEnd {

    private final static String TAG = LeaveForDialog.class.getSimpleName();

    Context mContext;

    Button mConfirmButton;
    TextView mLeaveText;

    Handler mDialogHandler;
    Timer timer;
    int time;

    int layoutId;
    String showText;

    public LeaveForDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    /**
     *
     * 初始化
     * @param context   //一般是Activity
     * @param themeResId    //themeId
     * @param showText  //设置要显示的信息
     */
    public LeaveForDialog(@NonNull Context context, int themeResId, String showText) {
        super(context, themeResId);
        this.mContext = context;
        this.showText = showText;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_leave_for);

        mConfirmButton = findViewById(R.id.confirmButton);
        mLeaveText = findViewById(R.id.leaveText);

        mLeaveText.setText(showText);

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

//        mDialogHandler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                switch (msg.what) {
//                    case Constant.TIME_CHANGED:
//                        mLeftButton.setText("拒绝("+time+")");
//                        if (time == 0) {
//                            timer.cancel();
//                            mLeftButton.performClick();
//                        }
//                        break;
//                }
//            }
//        };
//
//        mLeftButton = findViewById(R.id.leftButton);
//        mRightButton = findViewById(R.id.rightButton);
//        mShowText = findViewById(R.id.showText);
//
//        mShowText.setText(showText);
//        mLeftButton.setText("拒绝("+time+")");
//
//        mLeftButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mOnDialogButtonClickListener != null) {
//                    mOnDialogButtonClickListener.onLeftClick();
//                }
//                dismiss();
//            }
//        });
//
//        mRightButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dismiss();
//                if (mOnDialogButtonClickListener != null) {
//                    mOnDialogButtonClickListener.onRightClick();
//                }
//                dismiss();
//            }
//        });
//
//        Log.i(TAG,"onCreate");
    }

    @Override
    public void onTimeEnd() {

    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener mOnDialogButtonClickListener) {
//        this.mOnDialogButtonClickListener = mOnDialogButtonClickListener;
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
//        mLeftButton.setText("拒绝("+time+")");
//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                time--;
//                mDialogHandler.sendEmptyMessage(Constant.TIME_CHANGED);
//            }
//        },1000,1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"stop");
//        if (timer != null) {
//            timer.cancel();
//            timer = null;
//        }
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.i(TAG,"cancel");
    }

}
