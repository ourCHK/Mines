package com.chk.mines.Utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.chk.mines.Beans.CommunicateData;
import com.chk.mines.CustomService.ClientConnectService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.chk.mines.ConnectActivity.SOCKET_CONNECTED;
import static com.chk.mines.CustomService.ClientConnectService.RECEIVED_MESSAGE;

/**
 * Created by chk on 18-2-8.
 */

public class ClientSocketUtil {

    private String mIpAddressServer;
    private Socket mSocket;
    private int mPort = 8321;

    private Handler mActivityHandler;
    private Handler mServiceHandler;

    ConnectThread mConnectThread;
    ClientThread mClientThread;

    public ClientSocketUtil(String ipAddressServer,Handler handler,Handler serviceHandler) {
        this.mIpAddressServer = ipAddressServer;
        this.mActivityHandler = handler;
        this.mServiceHandler = serviceHandler;
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

    class ClientThread extends Thread {  //用于接受消息的一个线程
        @Override
        public void run() {
            DataInputStream reader;
            try {
                // 获取读取流
                reader = new DataInputStream(mSocket.getInputStream());
                while (true) {
                    String message = reader.readUTF();
                    Message msg = mServiceHandler.obtainMessage();
                    msg.what = RECEIVED_MESSAGE;
                    msg.obj = message;
                    mServiceHandler.sendMessage(msg);
                    Log.i("ClientSocketUtil","receivedMessage:"+message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void dealCommunicateData(String message) {
        CommunicateData communicateData = GsonUtil.stringToCommunicateData(message);
        if (communicateData != null) {
            Message msg = mServiceHandler.obtainMessage();
            msg.what = RECEIVED_MESSAGE;
            msg.obj = communicateData;
            mServiceHandler.sendMessage(msg);
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
