package com.chk.mines;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chk.mines.Utils.ClientSocketUtil;
import com.chk.mines.Utils.ServerSocketUtil;
import com.chk.mines.Utils.SocketUtil;

import CustomDialog.ClientDialog;
import CustomDialog.ServerDialog;

public class ConnectActivity extends AppCompatActivity implements View.OnClickListener{

    public final static int WIFI = 0;
    public final static int BLUETOOTH = 1;
    public final static int AP_ON = 2;
    public final static int AP_CONNECTED = 3;   //通过Ap地址判断是否连接了Ap
    public final static int SERVER = 4; //服务端
    public final static int CLIENT = 5; //客户端
    public final static int SOCKET_ACCEPTED = 6;
    public final static int SOCKET_CONNECTED = 7;
    public final static int START_CONNECT = 8;
    public final static int START_ACCEPT = 9;
    public final static int IP_CHANGED = 10;
    public final static int RECEIVED_MESSAGE = 11;
    int mConnectType;

    Handler mHandler;

    WifiManager mWifiManager;
    String IpAddress = "0.0.0.0";
    String IpAddressServer;   //用户客户端输入的服务端Ip地址

    Button mServerButton;
    Button mClientButton;
    TextView mIpAddress;

    Button mSend;
    EditText mInputContent;
    TextView mReceivedContent;

    Thread mMonitorIpThread;
    boolean isMonitorRun = true;

    ServerDialog serverDialog;
    ClientDialog clientDialog;

    ServerSocketUtil mServerSocketUtil;
    ClientSocketUtil mClientSocketUtil;

    int mServerOrClient;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_copy);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case IP_CHANGED:
                        mIpAddress.setText("您的IP地址："+IpAddress);
                        break;
                    case START_CONNECT: //客户端开始连接服务端
                        IpAddressServer = (String) msg.obj;
                        mClientSocketUtil = new ClientSocketUtil(IpAddress,IpAddressServer,mHandler);
                        mClientSocketUtil.startConnect();
                        Toast.makeText(ConnectActivity.this, "StartConnect", Toast.LENGTH_SHORT).show();
                        break;
                    case START_ACCEPT:  //服务端开始接收客户端请求
                        mServerOrClient = SERVER;
                        mServerSocketUtil = new ServerSocketUtil(IpAddress,mHandler);
                        mServerSocketUtil.startListener();
                        Toast.makeText(ConnectActivity.this, "StartAccept", Toast.LENGTH_SHORT).show();
                        break;
                    case SOCKET_CONNECTED:  //客户端已经连接到服务端
                        mServerOrClient = CLIENT;
                        clientDialog.dismiss();
                        Toast.makeText(ConnectActivity.this, "已连接到服务端", Toast.LENGTH_SHORT).show();
                        break;
                    case SOCKET_ACCEPTED:   //服务端已经接收了客户端
                        serverDialog.dismiss();
                        Toast.makeText(ConnectActivity.this, "已接收到客户端", Toast.LENGTH_SHORT).show();
                        break;
                    case RECEIVED_MESSAGE:
                        received((String)msg.obj);
                        Toast.makeText(ConnectActivity.this, " ReceivedMessage", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        init();
    }

    void init() {
        viewInit();
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Intent intent = getIntent();
        mConnectType = intent.getIntExtra("ConnectType",-1);
        switch (mConnectType) {
            case WIFI:
                break;
            case BLUETOOTH:
                break;
                default:
                    break;
        }

        mMonitorIpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isMonitorRun) {
                    try {
                        if (IpAddress != getIpAddress()) {
                            IpAddress = getIpAddress();
                            mHandler.sendEmptyMessage(IP_CHANGED);
                        }
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mMonitorIpThread.start();
    }

    void viewInit() {
        mServerButton = findViewById(R.id.server);
        mClientButton = findViewById(R.id.client);
        mIpAddress = findViewById(R.id.ipAddress);
        mServerButton.setOnClickListener(this);
        mClientButton.setOnClickListener(this);

        mSend = findViewById(R.id.sent);
        mSend.setOnClickListener(this);
        mInputContent = findViewById(R.id.inputContent);
        mReceivedContent = findViewById(R.id.receivedContent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.server:   //这边应该还加一个判断Ip地址是否合法的一个判断
                showServerDialog();
                mHandler.sendEmptyMessage(START_ACCEPT);
                break;
            case R.id.client:
                showClientDialog();
                break;
            case R.id.sent:
                send();
                break;
        }
    }

    /**
     * 获取WiFi时的IP地址
     * @return
     */
    String getIpAddress() {
        if (!mWifiManager.isWifiEnabled()) {
            return null;
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String IPAddress = intToIp(wifiInfo.getIpAddress());
        return IPAddress;
    }

    private String intToIp(int paramInt)
    {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

    /**
     * 显示服务端Dialog
     */
    void showServerDialog() {
        if (serverDialog == null)
            serverDialog = new ServerDialog(this,R.style.Custom_Dialog_Style);
        serverDialog.show();
    }

    /**
     * 显示客户端Dialog
     */
    void showClientDialog() {
        if (clientDialog == null)
            clientDialog = new ClientDialog(this,R.style.Custom_Dialog_Style,mHandler);
        clientDialog.show();
    }

    void send() {
        switch (mServerOrClient) {
            case SERVER:
                mServerSocketUtil.send(mInputContent.getText().toString());
                break;
            case CLIENT:
                mClientSocketUtil.send(mInputContent.getText().toString());
                break;
        }
        mInputContent.setText("");
    }

    void received(String message) {
        if (message != null && !message.isEmpty())
            mReceivedContent.setText("RECEIVED:"+message);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientDialog != null) {
            clientDialog.dismiss();
            clientDialog = null;
        }
        if (serverDialog != null) {
            serverDialog.dismiss();
            serverDialog = null;
        }
    }
}
