package com.chk.mines.Utils;


/**
 * Created by chk on 18-2-27.
 * 用于存储静态常量
 */

public class Constant {
    public final static String SOCKET_DISCONNECTED_BROADCAST_ACTION = "com.chk.mines.broadcast.SocketDisconnected";
    public final static String GAME_TYPE = "GameType";
    public final static String SERVER_OR_CLIENT = "ServerOrClient";

    public final static int SERVER = 4;
    public final static int CLIENT = 5;

    public final static int TYPE_1 = 1; //8*8
    public final static int TYPE_2 = 1<<1; //16*16
    public final static int TYPE_3 = 1<<2; //16*30
    public final static int TYPE_4 = 1<<3; //custom
    public final static int FLAG_IS_SINGLE = 1<<4;
    public final static int FLAG_IS_DOUBLE = 1<<5;

    public final static int GAME_OVER = -1;
    public final static int GAME_SUCCESS = 1;
    public final static int GAME_PAUSED = 2;
    public final static int GAME_START = 3;     //用于开始计时
    public final static int GAME_RESTART = 4;
    public final static int GAME_INIT = 5;  //初始化
    public final static int RECEIVED_MESSAGE_FROM_SERVER = 6;
    public final static int RECEIVED_MESSAGE_FROM_CLIENT = 7;
    public final static int TIME_CHANGED = 8;
    public final static int SOCKET_DISCONNECTED = 9;
    public final static int BIND_SERVICE = 11;   //客户端已经绑定服务了

    public final static int DRAG = 0;
    public final static int FLAG = 1;
    public final static int FLAG_CONFUSED = 2;
    public final static int PointDown = 10; //接收View传来的消息
}
