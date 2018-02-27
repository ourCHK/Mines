package com.chk.mines.CustomService;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.chk.mines.Beans.CommunicateData;
import com.chk.mines.CooperateGameActivity;
import com.chk.mines.Utils.ClientSocketUtil;
import com.chk.mines.Utils.GsonUtil;

import static com.chk.mines.ChooseGameTypeActivity.readyForStart;

/**
 * 客户端Wifi连接服务
 */
public class ClientConnectService extends Service {

    private static final String TAG = ClientConnectService.class.getSimpleName();

    private LocalBinder localBinder;
    private ClientSocketUtil mClientSocketUtil;

    private Handler mActivityHandler;
    private Handler mServiceHandler;    //Service用来后台接收服务端发来的消息
    private Handler mGameActivityHandler;
    private Handler mChooseGameTypeActivityHandler;

    public static final int RECEIVED_MESSAGE = 1;
    public static final int SOCKET_DISCONNECTED = 2;    //socket断开连接
    public static final int SOCKET_CONNECTED = 3;   //socket刚连上去的时候

    public static final int HEART_BEAT_SEND_TIME = 1000;    //发送时间间隔
    public static final int HEART_BEAT_TIME_OUT = 3 * 1000; //心跳包TimeOut时长

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
                    case SOCKET_DISCONNECTED:   //我们这里可以发送一个广播出去
                        Toast.makeText(ClientConnectService.this, "对方已从连接断开", Toast.LENGTH_SHORT).show();
                        break;
                    case SOCKET_CONNECTED:  //客户端连接上服务端的Socket的时候就开始发送心跳包
                        Log.i(TAG,"客户端开始发送心跳包");
                        CommunicateData cd = new CommunicateData();
                        cd.setType(CommunicateData.HEART_BEAT);
                        ClientConnectService.this.sendMessage(cd);
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
        mGameActivityHandler = handler;
    }

    public void setChoosedGameTypeActivityHandler(Handler handler) {    //用于和选择游戏类型activity进行通信
        mChooseGameTypeActivityHandler = handler;
    }

    public void sendMessage(String message) {
        mClientSocketUtil.send(message);
    }

    public void sendMessage(CommunicateData communicateData) {
        mClientSocketUtil.send(communicateData);
    }

    /**
     * 处理从ClientSocketUtil发来的信息
     * @param message
     */
    void receivedMessage(Message message) {
        CommunicateData communicateData = GsonUtil.stringToCommunicateData((String) message.obj);
        switch (communicateData.getType()) {
            case CommunicateData.HEART_BEAT:    //检测到心跳包,我们也应该发送心跳包过去
                Log.i(TAG,"客户端收到心跳包");
                mServiceHandler.removeMessages(SOCKET_DISCONNECTED);    //先移除之前的这个为发送的消息，下面继续发送这个消息
                mServiceHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CommunicateData cd = new CommunicateData();
                        cd.setType(CommunicateData.HEART_BEAT);
                        sendMessage(cd);
                        mServiceHandler.sendEmptyMessageDelayed(SOCKET_DISCONNECTED,HEART_BEAT_TIME_OUT);
//                        mServiceHandler.postDelayed(new Runnable() {    //如果没有3秒内没有收到心跳包回复就算是断开连接了
//                            @Override
//                            public void run() {
//                                Message msg = mServiceHandler.obtainMessage();
//                                msg.what = SOCKET_DISCONNECTED;
//                                mServiceHandler.sendMessage(msg);
//                            }
//                        },HEART_BEAT_TIME_OUT);
                    }
                },HEART_BEAT_SEND_TIME);
                break;
            case CommunicateData.USER_OPERATION:    //用户点击方块的操作
                Message msg1 = mGameActivityHandler.obtainMessage();
                msg1.what = CooperateGameActivity.RECEIVED_MESSAGE_FROM_SERVER;
                msg1.obj = communicateData;
                mGameActivityHandler.sendMessage(msg1);
                break;
            case CommunicateData.GAME_STATE:    //游戏状态改变
//                switch (communicateData.getGame_state()) {
//                    case CommunicateData.GAME_INIT: //收到初始化的消息
//
//                        break;
//                }
//                if (mGameActivityHandler == null) {
//
//                }
                Log.i(TAG,"GAME_STATE CHANGED");
                Message msg2 = mGameActivityHandler.obtainMessage();
                msg2.what = CooperateGameActivity.RECEIVED_MESSAGE_FROM_SERVER;
                msg2.obj = communicateData;
                mGameActivityHandler.sendMessage(msg2);
                break;
            case CommunicateData.OTHER:     //其他的消息，我们就知道应该是要跳转开始到游戏activity了
                Log.i(TAG,communicateData.getMessage());
                Message msg3 = mChooseGameTypeActivityHandler.obtainMessage();
                msg3.what = readyForStart;
                msg3.obj = communicateData.getMessage();
                mChooseGameTypeActivityHandler.sendMessage(msg3);
                break;
        }
    }

    public class LocalBinder extends Binder {
        public ClientConnectService getService() {
            return ClientConnectService.this;
        }
    }
}
