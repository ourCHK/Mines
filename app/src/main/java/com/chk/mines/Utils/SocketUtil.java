package com.chk.mines.Utils;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static com.chk.mines.ConnectActivity.SOCKET_ACCEPTED;
import static com.chk.mines.ConnectActivity.SOCKET_CONNECTED;

/**
 * Created by chk on 18-2-7.
 * 用于进行Socket通信的工具类
 */

public class SocketUtil {
    public static final int SERVER = 4;
    public static final int CLIENT = 5;

    String IpAddressServer; //服务端IP地址,默认就是这个啦
    String IpAddressClient; //客户端IP地址

    int mServerOrClient;

    Handler mHandler;

    int port = 7777;

    ServerSocket mServerSocket; //用于服务端接受客户端连接的ServerSocket
    Socket mSocketServer;   //服务端用来和客户端通信的Socket
    Socket mSocketClient;   //客户端用来和服务端通信的Socket

    Thread mServerThread;   //服务端通信线程
    Thread mAcceptThread;    //服务端接受线程

    Thread mClientThread;   //客户端通信线程
    Thread mConnectThread;  //客户端连接线程


    //http://blog.csdn.net/huaxun66/article/details/53008542

    public SocketUtil(int serverOrClient, String IpAddress, Handler handler) {
        mServerOrClient = serverOrClient;
        IpAddressServer = IpAddress;
        this.mHandler = handler;
        switch (mServerOrClient) {
            case SERVER:
                try {
                    mServerSocket = new ServerSocket(7777);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("SocketUtil","ServerSocket创建失败");
                }
                mAcceptThread = new AcceptThread();
                break;
            case CLIENT:
                mConnectThread = new ConnectThread();
                break;
        }
    }

    public SocketUtil(int serverOrClient, String IpAddress, String IpAddressServer, Handler handler) {
        mServerOrClient = serverOrClient;
        IpAddressClient = IpAddress;
        this.IpAddressServer = IpAddressServer;
        this.mHandler = handler;
        switch (mServerOrClient) {
            case SERVER:
                try {
                    mServerSocket = new ServerSocket(7777);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("SocketUtil","ServerSocket创建失败");
                }
                mAcceptThread = new AcceptThread();
                break;
            case CLIENT:
                mConnectThread = new ConnectThread();
                break;
        }
    }

    public void startListener() {
        mAcceptThread.start();
    }

    public void  startConnect() {
        mConnectThread.start();
    }


    class AcceptThread extends Thread {
        @Override
        public void run() {
            try {
                Log.i("SocketUtil","开始接受客户端请求");
                mSocketServer = mServerSocket.accept();
                mHandler.sendEmptyMessage(SOCKET_ACCEPTED);
                Log.i("SocketUtil","接收到客户端请求");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ConnectThread extends Thread {
        @Override
        public void run() {
            try {
                mSocketClient = new Socket(IpAddressServer,port);
                mHandler.sendEmptyMessage(SOCKET_CONNECTED);
                Log.i("SocketUtil","连接成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ServerThread extends Thread{
        @Override
        public void run() {
        }
    }

    class ClientThread extends Thread{
        @Override
        public void run() {
        }
    }
}
