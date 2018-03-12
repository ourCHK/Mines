package com.chk.mines.Utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.chk.mines.Beans.CommunicateData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;

import static com.chk.mines.ConnectActivity.SOCKET_ACCEPTED;
import static com.chk.mines.CustomServices.ServerConnectService.RECEIVED_MESSAGE;

/**
 * Created by chk on 18-2-8.
 */

public class ServerSocketUtil {

    private String mIpAddressServer;    //貌似不用这个也可以
    private ServerSocket mServerSocket;
    private Socket mSocket;
    private int mPort = 8321;   //端口号

    private Handler mActivityHandler;
    private Handler mServiceHandler;

    AcceptThread mAcceptThread;
    ServerThread mServerThread;

    Timer timer;

    public ServerSocketUtil(Handler handler,Handler serviceHandler) {
//        this.mIpAddressServer = ipAddressClient;
        this.mActivityHandler = handler;
        this.mServiceHandler = serviceHandler;
//        mAcceptThread = new AcceptThread();
        mServerThread = new ServerThread();

        try {
            mServerSocket = new ServerSocket(mPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setActivityHandler(Handler mActivityHandler) {
        this.mActivityHandler = mActivityHandler;
    }


    /**
     * 服务端开始监听客户端网络请求
     */
    public void startListener() {
        if (mAcceptThread == null || !mAcceptThread.isAlive() || mAcceptThread.isInterrupted()) {
            mAcceptThread = new AcceptThread();
        }
        mAcceptThread.start();
    }

    class AcceptThread extends Thread {
        @Override
        public void run() {
            try {
                Log.i("SocketUtil","开始接受客户端请求");
                mSocket = mServerSocket.accept();
                mServerThread.start();
                mActivityHandler.sendEmptyMessage(SOCKET_ACCEPTED);
                Log.i("SocketUtil","接收到客户端请求");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ServerThread extends Thread {  //用于接受消息的一个线程
        @Override
        public void run() {
            DataInputStream reader;
            try {
                // 获取读取流
                reader = new DataInputStream(mSocket.getInputStream());
                while (true) {
                    // 读取数据
                    String message = reader.readUTF();
                    Message msg = mServiceHandler.obtainMessage();
                    msg.what = RECEIVED_MESSAGE;
                    msg.obj = message;
                    mServiceHandler.sendMessage(msg);
                    Log.i("ServerSocketUtil",message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream writer = null;
                try {
                    writer = new DataOutputStream(mSocket.getOutputStream());
                    writer.writeUTF(message); // 写一个UTF-8的信息
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void send(CommunicateData communicateData) {
        final String message = GsonUtil.communicateDataToString(communicateData);
        Log.i("ServerSocketUtil","sendMessage:"+message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream writer = null;
                try {
                    writer = new DataOutputStream(mSocket.getOutputStream());
                    writer.writeUTF(message+""); // 写一个UTF-8的信息
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mSocket != null)
            mSocket.close();
        if (mServerSocket != null)
            mServerSocket.close();
        Log.i("ServerSocketUtil","服务端Socket关闭");
    }
}
