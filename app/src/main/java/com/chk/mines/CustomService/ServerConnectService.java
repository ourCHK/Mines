package com.chk.mines.CustomService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.chk.mines.Utils.ClientSocketUtil;
import com.chk.mines.Utils.ServerSocketUtil;

import java.net.ServerSocket;

import static com.chk.mines.ConnectActivity.IP_CHANGED;

/**
 * 服务端Wifi连接服务
 */
public class ServerConnectService extends Service {

    private static final String TAG = ServerConnectService.class.getSimpleName();
    private LocalBinder localBinder;
    private ServerSocketUtil mServerSocketUtil;

    private Handler mHandler;

    public ServerConnectService() {
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

    public void startAccept() {
        Log.i(TAG,"startAccept");
        if (mServerSocketUtil == null)
            mServerSocketUtil = new ServerSocketUtil(mHandler);
        mServerSocketUtil.startListener();
    }

    public void sent(String message) {
        mServerSocketUtil.send(message);
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public class LocalBinder extends Binder {
        public ServerConnectService getService() {
            return ServerConnectService.this;
        }
    }
}
