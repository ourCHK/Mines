package com.chk.mines.Utils;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by chk on 18-2-7.
 * 用于进行Socket通信的工具类
 */

public class SocketUtil {
    public static final int SERVER = 1;
    public static final int CLIENT = 2;

    String IpAddressServer = "192.168.43.1"; //服务端IP地址,默认就是这个啦
    String IpAddressClient; //客户端IP地址

    int mServerOrClient;

    Handler mHandler;

    int port = 7777;

    ServerSocket mServerSocket; //用于服务端接受客户端连接的ServerSocket
    Socket mSocketServer;   //服务端用来和客户端通信的Socket
    Socket mSocketClient;   //客户端用来和服务端通信的Socket


    //http://blog.csdn.net/huaxun66/article/details/53008542

    public SocketUtil(int serverOrClient) {
        mServerOrClient = serverOrClient;
        switch (mServerOrClient) {
            case SERVER:
                try {
                    mServerSocket = new ServerSocket(7777);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("SocketUtil","ServerSocket创建失败");
                }
                break;
            case CLIENT:
                try {
                    mSocketClient = new Socket(IpAddressServer,port);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("SocketUtil","客户端Socket创建失败");
                }
                break;
        }
    }

    public boolean startListener() {
        try {
            mSocketServer = mServerSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean startConnect() {
        return false;
    }


}
