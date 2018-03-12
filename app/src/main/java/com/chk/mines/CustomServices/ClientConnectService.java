package com.chk.mines.CustomServices;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.chk.mines.BaseActivity;
import com.chk.mines.Beans.CommunicateData;
import com.chk.mines.ChooseGameTypeActivity;
import com.chk.mines.CooperateGameActivityWithThread;
import com.chk.mines.Utils.ClientSocketUtil;
import com.chk.mines.Utils.Constant;
import com.chk.mines.Utils.GsonUtil;

import static com.chk.mines.ChooseGameTypeActivity.readyForStart;

/**
 * 客户端Wifi连接服务
 */
public class ClientConnectService extends ConnectService {

    private static final String TAG = ClientConnectService.class.getSimpleName();

    private LocalBinder localBinder;
    private ClientSocketUtil mClientSocketUtil;

    private Handler mActivityHandler;
    private Handler mServiceHandler;    //Service用来后台接收服务端发来的消息
    private Handler mGameActivityHandler;
    private Handler mChooseGameTypeActivityHandler;

    /**
     * 当前正在交互Activity的Handler
     */
    private Handler mCurActivityHandler;

    public static final int RECEIVED_MESSAGE = 1;
    public static final int SOCKET_DISCONNECTED = 2;    //socket断开连接
    public static final int SOCKET_CONNECTED = 3;   //socket刚连上去的时候

    public static final int HEART_BEAT_SEND_TIME = 1000;    //发送时间间隔
    public static final int HEART_BEAT_TIME_OUT = 5 * 1000; //心跳包TimeOut时长

    public ClientConnectService() {
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
                        receivedMessage(msg);
                        break;
                    case SOCKET_DISCONNECTED:   //我们这里可以发送一个广播出去
                        setSocketConnected(false);
                        sendSocketDisconnectedBroadcast();
                        Toast.makeText(ClientConnectService.this, "对方已从连接断开", Toast.LENGTH_SHORT).show();
                        break;
                    case SOCKET_CONNECTED:  //客户端连接上服务端的Socket的时候就开始发送心跳包
                        setSocketConnected(true);
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
        Log.i(TAG,"ClientConnectService Destroy");
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

    public void setChooseGameTypeActivityHandler(Handler handler) {    //用于和选择游戏类型activity进行通信
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
//                Log.i(TAG,"客户端收到心跳包");
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
                msg1.what = Constant.RECEIVED_MESSAGE_FROM_SERVER;
                msg1.obj = communicateData;
                mGameActivityHandler.sendMessage(msg1);
                break;
            case CommunicateData.GAME_STATE:    //游戏状态改变
                if ((mCurActivityHandler = getCurActivityHandler())== null) {
//                    Toast.makeText(this, "出现未知错误，请重启游戏", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG,"GAME_STATE CHANGED");
                    Message msg2 = mCurActivityHandler.obtainMessage();
                    msg2.what = Constant.RECEIVED_MESSAGE_FROM_SERVER;
                    msg2.obj = communicateData;
                    mCurActivityHandler.sendMessage(msg2);
                }
                break;
            case CommunicateData.OTHER:     //其他的消息，我们就知道应该是要跳转开始到游戏activity了
                Log.i(TAG,communicateData.getMessage());
                Message msg3 = mChooseGameTypeActivityHandler.obtainMessage();
                msg3.what = readyForStart;
                msg3.obj = communicateData.getMessage();
                mChooseGameTypeActivityHandler.sendMessage(msg3);
                break;
            case CommunicateData.BIND_SERVICE:  //客户端已经绑定服务了
                if (mGameActivityHandler == null) {
                    Message msg4 = new Message();
                    msg4.what = message.what;
                    msg4.obj = message.obj;
                    mServiceHandler.sendMessageDelayed(msg4,1000);   //我们自己的服务还没有绑定，通知1秒后重新发送这个包
                } else {    //mGameActivityHandler
                    mGameActivityHandler.sendEmptyMessage(Constant.BIND_SERVICE);
                }
                break;
        }
    }

    /**
     * 获取当前交互的Activity
     * @return
     */
    Handler getCurActivityHandler() {
        AppCompatActivity activity = BaseActivity.getCurResumeActivity();
        if (activity != null) {
            if (activity instanceof ChooseGameTypeActivity) {
                Log.i("TAG","curShowActivity："+ChooseGameTypeActivity.class.getSimpleName());
                return ((ChooseGameTypeActivity) activity).getHandler();
            } else if (activity instanceof CooperateGameActivityWithThread) {
                Log.i("TAG","curShowActivity："+CooperateGameActivityWithThread.class.getSimpleName());
                return ((CooperateGameActivityWithThread) activity).getHandler();
            }
        }
        return null;
    }

    public class LocalBinder extends Binder {
        public ClientConnectService getService() {
            return ClientConnectService.this;
        }
    }
}
