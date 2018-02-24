package com.chk.mines.Beans;

/**
 * Created by chk on 18-2-10.
 * 用于网络通信的数据类
 */

public class CommunicateData {
    public static final int GAME_STATE = 1;    //动作，用于控制游戏的逻辑，比如开始，重新开始等等
    public static final int USER_OPERATION = 2; //用户点击方块的操作
    public static final int OTHER = 3;  //其他的一些消息类型

    public static final int GAME_INIT = 2;
    public static final int GAME_START = 3;
    public static final int GAME_RESTART = 4;
    public static final int GAME_OVER = 5;
    public static final int GAME_WIN = 6;
    public static final int GAME_LOSE = 7;

    public static final int CLIENT_SERVICE_BIND = 11;   //客户端服务已经绑定，说明可开始发送数据了
    public static final int CLIENT_RECEIVED_MESSAGE = 12;   //客户端已经接收到服务端的数据，说明游戏可以开始了

    int type;   //Action Or OPERATION Or Other
    int game_state; //GameState that above;

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
}
