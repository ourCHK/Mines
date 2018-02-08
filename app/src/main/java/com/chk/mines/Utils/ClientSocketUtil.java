package com.chk.mines.Utils;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

import static com.chk.mines.ConnectActivity.SOCKET_CONNECTED;

/**
 * Created by chk on 18-2-8.
 */

public class ClientSocketUtil {

    private String mIpAddressServer;
    private String mIpAddressClient;
    private Socket mSocket;
    private int mPort = 7876;

    private Handler mActivityHandler;

    ConnectThread mConnectThread;

    public ClientSocketUtil(String ipAddressClient,String ipAddressServer,Handler handler) {
        this.mIpAddressClient = ipAddressClient;
        this.mIpAddressServer = ipAddressServer;
        this.mActivityHandler = handler;
        mConnectThread = new ConnectThread();
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ClientThread extends Thread{  //用于通信的一个线程
        @Override
        public void run() {
        }
    }
}
