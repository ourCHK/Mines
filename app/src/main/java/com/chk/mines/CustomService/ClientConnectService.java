package com.chk.mines.CustomService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.chk.mines.Utils.ClientSocketUtil;
import com.chk.mines.Utils.ServerSocketUtil;

/**
 * 客户端Wifi连接服务
 */
public class ClientConnectService extends Service {

    private static final String TAG = ClientConnectService.class.getSimpleName();
    private LocalBinder localBinder;
    private ClientSocketUtil mClientSocketUtil;

    private Handler mHandler;

    public ClientConnectService() {
        init();
    }

    void init() {
        localBinder = new LocalBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return localBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startConnect(String serverIpAddress) {
        if (mClientSocketUtil == null)
            mClientSocketUtil = new ClientSocketUtil(serverIpAddress,mHandler);
        mClientSocketUtil.startConnect();
    }

    public void sent(String message) {
        mClientSocketUtil.send(message);
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public class LocalBinder extends Binder {
        public ClientConnectService getService() {
            return ClientConnectService.this;
        }
    }
}
