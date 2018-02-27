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
import com.chk.mines.Utils.GsonUtil;
import com.chk.mines.Utils.ServerSocketUtil;

/**
 * 服务端Wifi连接服务
 */
public class ServerConnectService extends ConnectService {

    private static final String TAG = ServerConnectService.class.getSimpleName();
    private LocalBinder localBinder;
    private ServerSocketUtil mServerSocketUtil;

    private Handler mActivityHandler;
    private Handler mGameActivityHandler;
    private Handler mServiceHandler;

    public static final int RECEIVED_MESSAGE = 1;
    public static final int SOCKET_DISCONNECTED = 2;

    public static final int HEART_BEAT_SEND_TIME = 1000;    //发送时间间隔
    public static final int HEART_BEAT_TIME_OUT = 4 * 1000; //心跳包TimeOut时长,服务端要加上客户端每秒的延迟


    public ServerConnectService() {
        super();
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
                        Log.i(TAG,"ServerConnectService Received Message:"+msg.obj.toString());
                        receivedMessage(msg);
                        break;
                    case SOCKET_DISCONNECTED:   //我们这里可以发送一个广播出去
                        sendSocketDisconnectedBroadcast();
                        Toast.makeText(ServerConnectService.this, "对方已从连接断开", Toast.LENGTH_SHORT).show();
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
        Log.i(TAG,"ServerConnectService destory");
    }

    public void startAccept() {
        Log.i(TAG,"startAccept");
        if (mServerSocketUtil == null)
            mServerSocketUtil = new ServerSocketUtil(mActivityHandler,mServiceHandler);
        mServerSocketUtil.startListener();
    }

    public void sendMessage(String message) {
        mServerSocketUtil.send(message);
    }

    public void sendMessage(CommunicateData communicateData) {
        mServerSocketUtil.send(communicateData);
    }

    public void setHandler(Handler handler) {
        this.mActivityHandler = handler;
    }

    public void setGameActivityHandler(Handler handler) {   //用于和游戏activity进行通信
        mGameActivityHandler = handler;
    }

    /**
     * 处理从ClientSocketUtil发来的信息
     * @param message
     */
    void receivedMessage(Message message) {
        CommunicateData communicateData = GsonUtil.stringToCommunicateData((String) message.obj);
        switch (communicateData.getType()) {
            case CommunicateData.HEART_BEAT:    //检测到心跳包,我们也应该发送心跳包过去
                Log.i(TAG,"服务端收到心跳包");
                mServiceHandler.removeMessages(SOCKET_DISCONNECTED);    //先移除之前的这个为发送的消息，下面继续发送这个消息
                CommunicateData cd = new CommunicateData();
                cd.setType(CommunicateData.HEART_BEAT);
                sendMessage(cd);
                mServiceHandler.sendEmptyMessageDelayed(SOCKET_DISCONNECTED,HEART_BEAT_TIME_OUT);
//                mServiceHandler.postDelayed(new Runnable() {    //如果没有3秒内没有收到心跳包回复就算是断开连接了
//                    @Override
//                    public void run() {
//                        Message msg = mServiceHandler.obtainMessage();
//                        msg.what = SOCKET_DISCONNECTED;
//                        mServiceHandler.sendMessage(msg);
//                    }
//                },HEART_BEAT_TIME_OUT);
                break;
            case CommunicateData.USER_OPERATION:    //用户点击方块的操作
                Message msg1 = mGameActivityHandler.obtainMessage();
                msg1.what = CooperateGameActivity.RECEIVED_MESSAGE_FROM_CLIENT;
                msg1.obj = communicateData;
                mGameActivityHandler.sendMessage(msg1);
                break;
            case CommunicateData.GAME_STATE:    //游戏状态改变
//                switch (communicateData.getGame_state()) {
//                    case CommunicateData.GAME_INIT: //收到初始化的消息
//
//                        break;
//                }
                Log.i(TAG,"GAME_STATE CHANGED and Handler:"+(mGameActivityHandler == null));
                Message msg2 = mGameActivityHandler.obtainMessage();
                msg2.what = CooperateGameActivity.RECEIVED_MESSAGE_FROM_CLIENT;
                msg2.obj = communicateData;
                mGameActivityHandler.sendMessage(msg2);
                break;
//            case CommunicateData.OTHER:     //其他的消息，我们就知道应该是要跳转开始到游戏activity了
//                Log.i(TAG,communicateData.getMessage());
//                Message msg3 = mChoosedGameTypeActivityHandler.obtainMessage();
//                msg3.what = readyForStart;
//                msg3.obj = communicateData.getMessage();
//                mChoosedGameTypeActivityHandler.sendMessage(msg3);
//                break;
        }
    }

    public class LocalBinder extends Binder {
        public ServerConnectService getService() {
            return ServerConnectService.this;
        }
    }
}
