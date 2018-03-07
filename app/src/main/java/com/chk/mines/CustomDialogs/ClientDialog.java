package com.chk.mines.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chk.mines.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chk.mines.ConnectActivity.START_CONNECT;

/**
 * Created by chk on 18-2-3.
 */

public class ClientDialog extends Dialog {

    final String matches = "((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))";
    Pattern p = Pattern.compile(matches);

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
                if (ip == null && ip.isEmpty()) {
                    Toast.makeText(mContext, "ip不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (checkIp(ip)) {
                    Message msg = new Message();
                    msg.what = START_CONNECT;
                    msg.obj = ip;
                    mHandler.sendMessage(msg);
                } else {
                    Toast.makeText(mContext, "IP地址不合法", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 检查Ip是否合法
     * @param Ip
     * @return
     */
    boolean checkIp(String Ip) {
        Matcher matcher = p.matcher(Ip);
        if (matcher.lookingAt()) {  //从第一个字符开始匹配
            if (matcher.end() == Ip.length()) //判断尾部是否还有字符串
                return true;
        }
        return false;
    }


}
