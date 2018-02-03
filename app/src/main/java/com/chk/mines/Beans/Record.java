package com.chk.mines.Beans;

/**
 * Created by chk on 18-2-3.
 * 游戏记录
 */

public class Record {
    private int gameType;   //游戏种类
    private int gameTime;   //游戏完成时间
    private int gameData;   //游戏日期
    private String name;    //玩家姓名

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

    public int getGameData() {
        return gameData;
    }

    public void setGameData(int gameData) {
        this.gameData = gameData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
