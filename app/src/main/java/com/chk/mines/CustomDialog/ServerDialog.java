package com.chk.mines.CustomDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.chk.mines.R;

/**
 * Created by chk on 18-2-3.
 * 服务端等待Dialog
 */

public class ServerDialog extends Dialog {

    Context mContext;
    TextView serverIp;
    String IpAddress;

    public ServerDialog(@NonNull Context context, int themeResId,String IpAddress) {
        super(context, themeResId);
        this.mContext = context;
        this.IpAddress = IpAddress;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_server);

        serverIp = findViewById(R.id.serverIp);
        serverIp.setText(IpAddress);
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.width = display.getWidth() * 4 / 5 ; // 设置dialog宽度为屏幕的4/5
//        lp.height = display.getHeight() * 2 / 5;
//        getWindow().setAttributes(lp);
    }
}
