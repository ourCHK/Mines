package com.chk.mines.CustomService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.chk.mines.GameApplication;
import com.chk.mines.Utils.ClientSocketUtil;
import com.chk.mines.Utils.Constant;
import com.chk.mines.Utils.ServerSocketUtil;

/**
 * 连接连接WiFi的服务
 */
public class ConnectService extends Service {

    private static final String TAG = ConnectService.class.getSimpleName();
    private LocalBroadcastManager mLocalBroadcastManager;

    public ConnectService() {
        Log.i(TAG,"Service init");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
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

    public void sendSocketDisconnectedBroadcast() {
        Intent intent = new Intent(Constant.SOCKET_DISCONNECTED_BROADCAST_ACTION);
        mLocalBroadcastManager.sendBroadcast(intent);
    }
}
