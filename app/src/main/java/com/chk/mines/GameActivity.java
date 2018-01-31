package com.chk.mines;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chk.mines.Beans.Mine;
import com.chk.mines.Views.MineView;

public class GameActivity extends AppCompatActivity {

    public final static String GAME_TYPE = "GameType";

    public final static int TYPE_1 = 1; //8*8
    public final static int TYPE_2 = 1<<1; //16*16
    public final static int TYPE_3 = 1<<2; //16*30
    public final static int TYPE_4 = 1<<3; //custom
    public final static int FLAG_IS_SINGLE = 1<<4;
    public final static int FLAG_IS_DOUBLE = 1<<5;

    private int mChoosedGameType;
    private boolean isSingle;

    MineView mMineView;
    Mine[][] mines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(mMineView);
    }

    void init() {
        Intent intent = getIntent();
        int gameType = intent.getIntExtra(GAME_TYPE,-1);
        mChoosedGameType = gameType & (TYPE_1 | TYPE_2 | TYPE_3 | TYPE_4);
        isSingle = (gameType & FLAG_IS_SINGLE) == FLAG_IS_SINGLE;

        mMineView = new MineView(this);
        switch (mChoosedGameType) {
            case TYPE_1:
                mines = new Mine[8][8];
                break;
            case TYPE_2:
                mines = new Mine[16][16];
                break;
            case TYPE_3:
                mines = new Mine[16][30];
                break;
            case TYPE_4:
                break;
            default:
                break;
        }
        mMineView.setMines(mines);
    }
}
