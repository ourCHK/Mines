package com.chk.mines;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chk.mines.Utils.BindView;
import com.chk.mines.Utils.InitBindView;
import com.chk.mines.Utils.WifiHotspotUtil;
import com.chk.mines.Views.WaitingDialog;

import java.lang.reflect.Method;

public class ConnectActivity extends AppCompatActivity implements View.OnClickListener{

    public final static int WIFI = 0;
    public final static int BLUETOOTH = 1;
    public final static int AP_ON = 2;
    public final static int Ap_CONNECTED = 3;   //通过Ap地址判断是否连接了Ap
    public final static int SERVER = 4; //服务端
    public final static int CLIENT = 5; //客户端
    int mConnectType;
    WifiHotspotUtil mWifiUtil;

    WifiManager mWifiManager;
    String IpAddress;

    @BindView(R.id.openHotspot)
    Button mOpenHotspot;

    @BindView(R.id.openWifi)
    Button mOpenWifi;

    Handler mHandler;

    Thread mServerThread; //用于检测wifi热点是否开启;
    boolean isServerRun = true;
    Thread mClientThread; //用于判断是否连接上了Wifi热点;
    boolean isClientRun = true;

    int mServerOrClient;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AP_ON:
                        mServerOrClient = SERVER;
                        Toast.makeText(ConnectActivity.this, "Wifi Ap is On!", Toast.LENGTH_SHORT).show();
                        showServerDialog();
                        getIpAddress();
                        break;
                    case Ap_CONNECTED:
                        mServerOrClient = CLIENT;
                        Toast.makeText(ConnectActivity.this, "Wifi Ap connected!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        init();
    }

    void init() {
        InitBindView.init(this);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isServerRun) {
                    if (isApOn()) {
                        isServerRun = false;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mClientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isClientRun) {

                    String IpAddress = getIpAddress();
                    if (IpAddress != null && !IpAddress.isEmpty()) {
                        if (IpAddress.contains("192.168.43.")) {
                            isClientRun = false;
                            mHandler.sendEmptyMessage(Ap_CONNECTED);
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

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

        mOpenHotspot.setOnClickListener(this);
        mOpenWifi.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openHotspot:
                if (isApOn()) {
                    mHandler.sendEmptyMessage(AP_ON);
                    Log.i("ConnectActivity","这里不走吗");
                    return;
                }
                openSettingsUI();
                if (!mServerThread.isAlive())
                        mServerThread.start();
                mHandler.sendEmptyMessage(AP_ON);
                Toast.makeText(this, "请手动开启WiFi热点", Toast.LENGTH_SHORT).show();
                break;
            case R.id.openWifi:
                openWifiUI();
                if (!mClientThread.isAlive())
                    mClientThread.start();
                break;
        }
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //6.0及以上
            if (checkSelfPermission(Manifest.permission.WRITE_SETTINGS) !=PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "the app need the permission tu run,please grant it", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            } else {
                init();
            }
        } else {    //以下直接初始化
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length >0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {  //授权了
                init();
            } else {
                Toast.makeText(this, "sorry you do not have granted the permission to me ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 用于检测WiFi热点是否开启
     * @return
     */
    public boolean isApOn() {
        try {
            Method method = mWifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifiManager);
        }
        catch (Throwable ignored) {
            Log.i("ConnectActivity",ignored.toString());
        }
        return false;
    }

    /**
     * 打开设置页面让用户开启WiFi热点
     */
    private void openSettingsUI() {
        Intent intent =  new Intent(Settings.ACTION_SETTINGS);
        startActivity(intent);
    }

    private void openWifiUI() {
        Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
        startActivity(wifiSettingsIntent);
    }

    void showServerDialog() {
        WaitingDialog dialog = null;
        dialog = new WaitingDialog(this,R.style.Waiting_Dialog_Style,R.layout.dialog_layout_waiting);
        dialog.show();
    }

    String getIpAddress() {
        if (!mWifiManager.isWifiEnabled()) {
            return null;
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String IPAddress = intToIp(wifiInfo.getIpAddress());
        Log.i("ipAddress",IPAddress);
        return IPAddress;

//        DhcpInfo dhcpinfo = mWifiManager.getDhcpInfo();
//        String serverAddress = intToIp(dhcpinfo.serverAddress);
//        Log.i("ipAddress",serverAddress);
//        return null;
    }

    private String intToIp(int paramInt)
    {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }
}
