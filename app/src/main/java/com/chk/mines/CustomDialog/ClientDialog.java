package com.chk.mines.CustomDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chk.mines.R;

import static com.chk.mines.ConnectActivity.START_CONNECT;

/**
 * Created by chk on 18-2-3.
 */

public class ClientDialog extends Dialog {

    Context mContext;
    Handler mHandler;

    Button mLeftButton;
    Button mRightButton;
    EditText mInputIp;

    public ClientDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public ClientDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    public ClientDialog(@NonNull Context context, int themeResId, Handler handler) {
        super(context,themeResId);
        this.mContext = context;
        this.mHandler = handler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_client);

        mLeftButton = findViewById(R.id.leftButton);
        mRightButton = findViewById(R.id.rightButton);
        mInputIp = findViewById(R.id.inputIp);

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = mInputIp.getText().toString().trim();
                if (ip != null && !ip.isEmpty()) {
                    Message msg = new Message();
                    msg.what = START_CONNECT;
                    msg.obj = ip;
                    mHandler.sendMessage(msg);
                } else {
                    Toast.makeText(mContext, "ip不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
