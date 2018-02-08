package com.chk.mines.Utils;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static com.chk.mines.ConnectActivity.SOCKET_ACCEPTED;

/**
 * Created by chk on 18-2-8.
 */

public class ServerSocketUtil {

    private String mIpAddressServer;    //貌似不用这个也可以
    private ServerSocket mServerSocket;
    private Socket mSocket;
    private int mPort = 7876;   //端口号

    private Handler mActivityHandler;

    AcceptThread mAcceptThread;

    public ServerSocketUtil(String ipAddressServer,Handler handler) {
        this.mIpAddressServer = ipAddressServer;
        this.mActivityHandler = handler;
        mAcceptThread = new AcceptThread();

        try {
            mServerSocket = new ServerSocket(mPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startListener() {
        mAcceptThread.start();
    }

    class AcceptThread extends Thread {
        @Override
        public void run() {
            try {
                Log.i("SocketUtil","开始接受客户端请求");
                mSocket = mServerSocket.accept();
                mActivityHandler.sendEmptyMessage(SOCKET_ACCEPTED);
                Log.i("SocketUtil","接收到客户端请求");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ServerThread extends Thread{  //用于通信的一个线程
        @Override
        public void run() {
        }
    }
}
