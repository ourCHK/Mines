package com.chk.mines.Beans;

/**
 * Created by chk on 18-2-10.
 * 用于网络通信的数据类
 */

public class CommunicateData {
    public static final int GAME_STATE = 1;    //动作，用于控制游戏的逻辑，比如开始，重新开始等等
    public static final int USER_OPERATION = 2; //用户点击方块的操作
    public static final int OTHER = 3;  //其他的一些消息类型
    public static final int HEART_BEAT = 4;    //用于做心跳检测
    public static final int BIND_SERVICE = 5;    //用于判断对方是否绑定了服务
    int type;   //Action Or OPERATION Or Other


    public static final int GAME_INIT = 2;
    public static final int GAME_PAUSE = 8;
    public static final int GAME_START = 3;
    public static final int GAME_RESTART = 4;
    public static final int GAME_OVER = 5;
    public static final int GAME_WIN = 6;
    public static final int GAME_LOSE = 7;

    public static final int CLIENT_RECEIVED_MESSAGE = 12;   //客户端已经接收到服务端的数据，说明游戏可以开始了
    public static final int ACCEPTED = 13;  //接受重新开始
    public static final int REJECTED = 14;  //拒绝重新开始
    public static final int ASK_FOR_RESTART = 15;    //请求重新开始
    public static final int RECEIVED_MINES_DATA = 16;   //接收到雷的数据
    public static final int SEND_MINES_DATA = 17;    //发送雷的数据

    public static final int ASK_FOR_NEW_GAME = 18;  //服务端请求新游戏
    public static final int ACCEPT_NEW_GAME = 19;
    public static final int REJECT_NEW_GAME = 20;

    public static final int LEAVE_CUR_GAME = 21; //对方退出当前游戏
    public static final int LEAVE_MUTIPLE_GAME = 22;    //对方离开多人游戏

    int game_state; //GameState that above;

    public static final int DRAG = 1;
    public static final int FLAG = 2;
    public static final int FLAG_CONFUSED = 3;
    int user_operation;

    public static final int HEART_BEAT_SEND = 1;
    public static final int HEART_BEAT_RECEIVED = 2;
    int heart_beat;    //心跳检测类型

    int row = -1;   //默认都是-1
    int column = -1;

    int rows = -1;
    int columns = -1;
    String mines = null;   //将雷初始化的数据存储到mines

    String message;    //其他类型的消息

    public CommunicateData() {

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getGame_state() {
        return game_state;
    }

    public void setGame_state(int game_state) {
        this.game_state = game_state;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public String getMines() {
        return mines;
    }

    public void setMines(String mines) {
        this.mines = mines;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUser_operation() {
        return user_operation;
    }

    public void setUser_operation(int user_operation) {
        this.user_operation = user_operation;
    }

    public int getHeart_beat() {
        return heart_beat;
    }

    public void setHeart_beat(int heart_beat) {
        this.heart_beat = heart_beat;
    }
}
