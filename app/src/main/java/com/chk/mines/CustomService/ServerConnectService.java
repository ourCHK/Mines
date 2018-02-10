package com.chk.mines.CustomService;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.chk.mines.Utils.ServerSocketUtil;

/**
 * 服务端Wifi连接服务
 */
public class ServerConnectService extends Service {

    private static final String TAG = ServerConnectService.class.getSimpleName();
    private LocalBinder localBinder;
    private ServerSocketUtil mServerSocketUtil;

    private Handler mActivityHandler;

    private Handler mServiceHandler;

    public ServerConnectService() {
        Log.i(TAG,"ServerConnectService inited");
        init();
    }

    @SuppressLint("HandlerLeak")
    void init() {
        localBinder = new LocalBinder();
        mServiceHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
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
        Log.i(TAG,"ServerConnectService destory");
    }

    public void startAccept() {
        Log.i(TAG,"startAccept");
        if (mServerSocketUtil == null)
            mServerSocketUtil = new ServerSocketUtil(mActivityHandler);
        mServerSocketUtil.startListener();
    }

    public void sent(String message) {
        mServerSocketUtil.send(message);
    }

    public void setHandler(Handler handler) {
        this.mActivityHandler = handler;
    }

    public class LocalBinder extends Binder {
        public ServerConnectService getService() {
            return ServerConnectService.this;
        }
    }
}
