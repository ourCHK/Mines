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
import com.chk.mines.Utils.GsonUtil;

import static com.chk.mines.ChooseGameTypeActivity.readyForStart;

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
    private Handler mGameActivityHanlder;
    private Handler mChoosedGameTypeActivityHandler;

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
                        receivedMessage(msg);
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

    public void setHandler(Handler handler) {
        this.mActivityHandler = handler;
    }

    public void setGameActivityHandler(Handler handler) {   //用于和游戏activity进行通信
        mGameActivityHanlder = handler;
    }

    public void setChoosedGameTypeActivityHandler(Handler handler) {    //用于和选择游戏类型activity进行通信
        mChoosedGameTypeActivityHandler = handler;
    }

    public void sendMessage(String message) {
        mClientSocketUtil.send(message);
    }

    /**
     * 处理从ClientSocketUtil发来的信息
     * @param message
     */
    void receivedMessage(Message message) {
        CommunicateData communicateData = GsonUtil.stringToCommunicateData((String) message.obj);
        switch (communicateData.getType()) {
            case CommunicateData.USER_OPERATION:    //用户点击方块的操作
                break;
            case CommunicateData.GAME_STATE:    //游戏状态改变
                break;
            case CommunicateData.OTHER:     //其他的消息，我们就知道应该是要跳转开始到游戏activity了
                Log.i(TAG,communicateData.getMessage());
                Message msg = mChoosedGameTypeActivityHandler.obtainMessage();
                msg.what = readyForStart;
                msg.obj = communicateData.getMessage();
                mChoosedGameTypeActivityHandler.sendMessage(msg);
                break;
        }
    }

    public class LocalBinder extends Binder {
        public ClientConnectService getService() {
            return ClientConnectService.this;
        }
    }
}
