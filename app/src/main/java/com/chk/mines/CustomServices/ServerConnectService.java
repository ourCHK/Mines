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
import com.chk.mines.Utils.Constant;
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

    /**
     * 当前正在交互Activity的Handler,前面那么多个handler弱爆了
     */
    private Handler mCurActivityHandler;

    public static final int RECEIVED_MESSAGE = 1;
    public static final int SOCKET_DISCONNECTED = 2;

    public static final int HEART_BEAT_SEND_TIME = 1000;    //发送时间间隔,其实不用这个，接到客户端的消息就直接发送回去就可以了
    public static final int HEART_BEAT_TIME_OUT = 6 * 1000; //心跳包TimeOut时长,服务端要加上客户端每秒的延迟


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
        Log.i(TAG,"ServerConnectService destroy");
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
//                Log.i(TAG,"服务端收到心跳包");
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
                msg1.what = Constant.RECEIVED_MESSAGE_FROM_CLIENT;
                msg1.obj = communicateData;
                mGameActivityHandler.sendMessage(msg1);
                break;
            case CommunicateData.GAME_STATE:    //游戏状态改变
                if ((mCurActivityHandler = getCurActivityHandler())== null) {
                    Toast.makeText(this, "出现未知错误，请重启游戏", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG,"GAME_STATE CHANGED");
                    Message msg2 = mCurActivityHandler.obtainMessage();
                    msg2.what = Constant.RECEIVED_MESSAGE_FROM_CLIENT;
                    msg2.obj = communicateData;
                    mCurActivityHandler.sendMessage(msg2);
                }
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
        if (activity instanceof ChooseGameTypeActivity) {
            Log.i("TAG","curShowActivity："+ChooseGameTypeActivity.class.getSimpleName());
            return ((ChooseGameTypeActivity) activity).getHandler();
        } else if (activity instanceof CooperateGameActivityWithThread) {
            Log.i("TAG","curShowActivity："+CooperateGameActivityWithThread.class.getSimpleName());
            return ((CooperateGameActivityWithThread) activity).getHandler();
        }
        return null;
    }

    public class LocalBinder extends Binder {
        public ServerConnectService getService() {
            return ServerConnectService.this;
        }
    }
}
