package com.chk.mines.CustomService;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.chk.mines.Beans.CommunicateData;
import com.chk.mines.Utils.ClientSocketUtil;

/**
 * 客户端Wifi连接服务
 */
public class ClientConnectService extends Service {

    private static final String TAG = ClientConnectService.class.getSimpleName();
    public static final int RECEIVED_MESSAGE = 1;
    public static final int START_GAME = 2;

    private LocalBinder localBinder;
    private ClientSocketUtil mClientSocketUtil;

    private Handler mActivityHandler;
    private Handler mServiceHandler;    //Service用来后台接收服务端发来的消息

    public ClientConnectService() {
        Log.i(TAG,"ClientConnectService init");
        init();
    }

    @SuppressLint("HandlerLeak")
    void init() {
        localBinder = new LocalBinder();
        mServiceHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RECEIVED_MESSAGE:
                        dealMessage(msg);
                        break;
                }
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
        Log.i(TAG,"ClientConnectService Destory");
    }

    public void startConnect(String serverIpAddress) {
        if (mClientSocketUtil == null)
            mClientSocketUtil = new ClientSocketUtil(serverIpAddress, mActivityHandler, mServiceHandler);
        mClientSocketUtil.startConnect();
    }

    public void sent(String message) {
        mClientSocketUtil.send(message);
    }

    public void setHandler(Handler handler) {
        this.mActivityHandler = handler;
    }

    /**
     * 处理从ClientSocketUtil发来的信息
     * @param msg
     */
    void dealMessage(Message msg) {
        CommunicateData communicateData = (CommunicateData) msg.obj;
        switch (communicateData.getType()) {
            case CommunicateData.USER_OPERATION:
                break;
            case CommunicateData.GAME_STATE:
                break;
        }
    }

    public class LocalBinder extends Binder {
        public ClientConnectService getService() {
            return ClientConnectService.this;
        }
    }
}
