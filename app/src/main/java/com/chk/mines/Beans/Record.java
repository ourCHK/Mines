package com.chk.mines.Beans;

/**
 * Created by chk on 18-2-3.
 * 游戏记录
 */

public class Record implements Cloneable{
    private int gameType;   //游戏种类
    private int gameTime;   //游戏完成时间
    private String gameData;   //游戏日期
    private String gamePlayer;    //玩家姓名

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public int getGameTime() {
        return gameTime;
    }

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }

    public String getGameData() {
        return gameData;
    }

    public void setGameData(String gameData) {
        this.gameData = gameData;
    }

    public String getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(String gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
