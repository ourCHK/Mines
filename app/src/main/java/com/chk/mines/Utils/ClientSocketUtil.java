package com.chk.mines.Utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.chk.mines.ConnectActivity.RECEIVED_MESSAGE;
import static com.chk.mines.ConnectActivity.SOCKET_CONNECTED;

/**
 * Created by chk on 18-2-8.
 */

public class ClientSocketUtil {

    private String mIpAddressServer;
    private Socket mSocket;
    private int mPort = 8321;

    private Handler mActivityHandler;

    ConnectThread mConnectThread;
    ClientThread mClientThread;

    public ClientSocketUtil(String ipAddressServer,Handler handler) {
        this.mIpAddressServer = ipAddressServer;
        this.mActivityHandler = handler;
        mConnectThread = new ConnectThread();
        mClientThread = new ClientThread();
    }

    public void  startConnect() {
        mConnectThread.start();
    }

    class ConnectThread extends Thread {
        @Override
        public void run() {
            try {
                mSocket = new Socket(mIpAddressServer, mPort);
                mActivityHandler.sendEmptyMessage(SOCKET_CONNECTED);
                Log.i("SocketUtil","连接成功");
                mClientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ClientThread extends Thread{  //用于接受消息的一个线程
        @Override
        public void run() {
            DataInputStream reader;
            try {
                // 获取读取流
                reader = new DataInputStream(mSocket.getInputStream());
                while (true) {
                    String message = reader.readUTF();
                    Message msg = mActivityHandler.obtainMessage();
                    msg.what = RECEIVED_MESSAGE;
                    msg.obj = message;
                    mActivityHandler.sendMessage(msg);
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mSocket != null)
            mSocket.close();
        Log.i("ServerSocketUtil","客户端Socket关闭");
    }
}
