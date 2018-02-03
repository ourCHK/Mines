package com.chk.mines;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.chk.mines.Beans.Mine;
import com.chk.mines.Interface.OnDialogButtonClickListener;
import com.chk.mines.Utils.BindView;
import com.chk.mines.Utils.InitBindView;
import com.chk.mines.Views.CustomDialog;
import com.chk.mines.Views.MineView;
import com.chk.mines.Views.MineViewType1;
import com.chk.mines.Views.MineViewType2;
import com.chk.mines.Views.MineViewType3;
import com.chk.mines.Views.MineViewType4;
import com.chk.mines.Views.TimeTextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{

    public final static String GAME_TYPE = "GameType";

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
    public final static int TIME_CHANGED = 8;

    Handler mHandler;
    Timer timer;
    int time;

    private int mChoosedGameType;
    private boolean isSingle;

    MineView mMineView;
    Mine[][] mines;
    int mMineCount;

    @BindView(R.id.mineViewContainer)
    LinearLayout mMineViewContainer;

    @BindView(R.id.flagButton)
    Button mFlagButton;

    @BindView(R.id.restart)
    Button mRestartGame;

    @BindView(R.id.showDialog)
    Button mShowDialog;

    @BindView(R.id.timeView)
    TimeTextView mTimeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        init();
    }

    @SuppressLint("HandlerLeak")
    void init() {
        InitBindView.init(this);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case GAME_SUCCESS:
                        showCustomDialog(GAME_SUCCESS);
                        timer.cancel();
                        Log.i("GameActivity","Success");
                        break;
                    case GAME_OVER:
                        showCustomDialog(GAME_OVER);
                        Log.i("GameActivity","GameOver");
                        timer.cancel();
                        break;
                    case GAME_START:
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mHandler.sendEmptyMessage(TIME_CHANGED);
                            }
                        },1000,1000);
                        break;
                    case TIME_CHANGED:
                        mTimeView.setText("TIME:"+ ++time);
                        break;
                }
            }
        };


        Intent intent = getIntent();
        int gameType = intent.getIntExtra(GAME_TYPE,-1);
        mChoosedGameType = gameType & (TYPE_1 | TYPE_2 | TYPE_3 | TYPE_4);
        isSingle = (gameType & FLAG_IS_SINGLE) == FLAG_IS_SINGLE;

        switch (mChoosedGameType) {
            case TYPE_1:
                mMineView = new MineViewType1(this);
                mines = new Mine[8][8];
                mMineCount = 10;
                break;
            case TYPE_2:
                mMineView = new MineViewType2(this);
                mines = new Mine[16][16];
                mMineCount = 40;
                break;
            case TYPE_3:
                mMineView = new MineViewType3(this);
                mines = new Mine[16][30];
                mMineCount = 99;
                break;
            case TYPE_4:
                mMineView = new MineViewType4(this);
                break;
            default:
                break;
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMineViewContainer.addView(mMineView,lp);
        initMines(mines, mMineCount);
        mMineView.setMines(mines, mMineCount);
        mMineView.setHandler(mHandler);

        mFlagButton.setOnClickListener(this);
        mRestartGame.setOnClickListener(this);
        mShowDialog.setOnClickListener(this);
    }

    void initMines(Mine[][] mines,int mineCount) {
        long startTime = System.currentTimeMillis();
        Random random = new Random(System.currentTimeMillis());
        int createdMines = 0;
        int row;
        int column;
        int rows = mines.length;
        int columns = mines[0].length;

        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                mines[i][j] = new Mine();
            }
        }

        while (createdMines < mineCount) {
            row = random.nextInt(rows);
            column = random.nextInt(columns);
            if (!mines[row][column].isMine()) {
                mines[row][column].setMine(true);
                mines[row][column].setNum(-1);
                createdMines++;
            }
        }

        //下面开始生成雷周围的数字
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                if (mines[i][j].isMine()) {
                    if (i-1 >= 0 && j-1 >= 0 && !mines[i-1][j-1].isMine())
                        mines[i-1][j-1].setNum(mines[i-1][j-1].getNum()+1);

                    if (i-1 >= 0 && !mines[i-1][j].isMine())
                        mines[i-1][j].setNum(mines[i-1][j].getNum()+1);

                    if (i-1 >= 0 && j+1 < columns && !mines[i-1][j+1].isMine())
                        mines[i-1][j+1].setNum(mines[i-1][j+1].getNum()+1);

                    if (j-1 >= 0 && !mines[i][j-1].isMine())
                        mines[i][j-1].setNum(mines[i][j-1].getNum()+1);

                    if (j+1 < columns && !mines[i][j+1].isMine())
                        mines[i][j+1].setNum(mines[i][j+1].getNum()+1);

                    if (i+1 < rows && j-1 >= 0 && !mines[i+1][j-1].isMine())
                        mines[i+1][j-1].setNum(mines[i+1][j-1].getNum()+1);

                    if (i+1 < rows && !mines[i+1][j].isMine())
                        mines[i+1][j].setNum(mines[i+1][j].getNum()+1);

                    if (i+1 < rows && j+1 < columns && !mines[i+1][j+1].isMine())
                        mines[i+1][j+1].setNum(mines[i+1][j+1].getNum()+1);
                }
            }
        }

        for (int i=0; i<rows; i++) {
            String string = new String();
            for (int j = 0; j < columns; j++) {
                if (mines[i][j].getNum() == -1)
                    string += "*" + " ";
                else
                    string += mines[i][j].getNum() + " ";
            }
            Log.i("GameActivity",string);
        }
        Log.i("GameActivity","initMines cost Time:"+(System.currentTimeMillis() - startTime));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flagButton:
                mMineView.setCurrentType();
                break;
            case R.id.restart:
                restartGame();
                break;
            case R.id.showDialog:
//                showDialog();
                break;
        }
    }




    void showCustomDialog(int GameType) {
        CustomDialog dialog = null;
        switch (GameType) {
            case GAME_SUCCESS:
                dialog = new CustomDialog(this,R.style.Custom_Dialog_Style,R.layout.dialog_layout_success);
                break;
            case GAME_OVER:
                dialog = new CustomDialog(this,R.style.Custom_Dialog_Style,R.layout.dialog_layout_fail);
                break;
        }
        if (dialog != null) {
            dialog.setOnDialogButtonClickListener(new OnDialogButtonClickListener() {
                @Override
                public void onLeftClickListener() {
                }

                @Override
                public void onRightClickListener() {
                    restartGame();
                }
            });
            dialog.show();
        }
    }

    void restartGame() {
        time = 0;
        mTimeView.setText("Time:0");
        initMines(mines,mMineCount);
        mMineView.restart(mines,mMineCount);
    }
}
