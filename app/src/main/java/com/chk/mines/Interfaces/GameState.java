package com.chk.mines.Interfaces;

/**
 * Created by chk on 18-2-5.
 * 游戏状态接口
 */

public interface GameState {
    void gameInit();
    void gameStart();
    void gamePause();
    void gameOver();
    void gameRestart();
    void gameSuccess();
}
