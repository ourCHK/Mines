package com.chk.mines.CustomServices;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.chk.mines.Utils.Constant;

/**
 * 连接连接WiFi的服务
 */
public class ConnectService extends Service {

    private static final String TAG = ConnectService.class.getSimpleName();
    private boolean isSocketConnected;  //用于判断Socket是否连接


    public ConnectService() {
        Log.i(TAG,"Service init");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
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
        sendBroadcast(intent);
    }

    /**
     * 检查Socket是否连接
     * @return
     */
    public boolean isSocketConnected() {
        return isSocketConnected;
    }

    public void setSocketConnected(boolean socketConnected) {
        isSocketConnected = socketConnected;
    }
}
