package com.chk.mines.CustomService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

import com.chk.mines.Utils.ClientSocketUtil;
import com.chk.mines.Utils.ServerSocketUtil;

/**
 * 连接连接WiFi的服务
 */
public class ConnectService extends Service {

    private static final String TAG = ConnectService.class.getSimpleName();
    private LocalBinder localBinder;
    private ClientSocketUtil mClientSocketUtil;
    private ServerSocketUtil mServerSocketUtil;

    WifiManager mWifiManager;
    private Thread mMonitorIpThread;
    boolean isMonitorRun = true;
    String mIpAddress;




    public ConnectService() {
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mMonitorIpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isMonitorRun) {
                    try {
                        if (mIpAddress != getIpAddress()) {
                            mIpAddress = getIpAddress();
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

    void init() {
        localBinder = new LocalBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    String Received() {
        return null;
    }

    String Sent() {
        return null;
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

    class LocalBinder extends Binder {
        ConnectService getService() {
            return ConnectService.this;
        }
    }
}
