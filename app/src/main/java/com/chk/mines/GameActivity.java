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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chk.mines.Beans.Mine;
import com.chk.mines.Interface.GameState;
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

import static com.chk.mines.GameActivity.PointType.DRAG;
import static com.chk.mines.GameActivity.PointType.FLAG;

public class GameActivity extends AppCompatActivity implements View.OnClickListener,GameState{

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
    public final static int GAME_RESTART = 4;
    public final static int GAME_INIT = 5;  //初始化
    public final static int TIME_CHANGED = 8;
    private  int GAME_STATE;  //游戏初始化

    public final static int PointDown = 10; //接收View传来的消息

    Handler mHandler;
    Timer timer;
    int time;

    private int mChoosedGameType;
    private boolean isSingle;

    MineView mMineView;
    Mine[][] mines;
    int rows;
    int columns;
    int mMineCount;

    public enum PointType {    //用于判断点击下去时是什么状态，挖雷状态还是标记状态,又或者是疑惑雷标记等等
        DRAG, FLAG, FLAG_CONFUSED;

    }
    PointType mCurrentType = DRAG;    //默认是挖雷状态

    @BindView(R.id.mineViewContainer)
    LinearLayout mMineViewContainer;

    @BindView(R.id.shovel)
    ImageView mShovel;

    @BindView(R.id.flag)
    ImageView mFlag;

    @BindView(R.id.flag_confused)
    ImageView mFlagConfused;

    @BindView(R.id.timeView)
    TimeTextView mTimeView;

    @BindView(R.id.restart)
    ImageView mRestart;

    @BindView(R.id.startAndPaused)
    ImageView mStartAndPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_second);
        init();
    }

    @SuppressLint("HandlerLeak")
    void init() {
        InitBindView.init(this);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case GAME_INIT:
                        gameInit();
                        break;
                    case GAME_SUCCESS:
                        gameSuccess();
                        break;
                    case GAME_OVER:
                        gameOver();
                        break;
                    case GAME_START:
                        gameStart();
                        break;
                    case GAME_PAUSED:
                        gamePause();
                        break;
                    case GAME_RESTART:
                        gameRestart();
                        break;
                    case TIME_CHANGED:
                        mTimeView.setText("TIME:"+ ++time);
                        break;
                    case PointDown:
                        pointDownCube(msg.arg1,msg.arg2);
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

        mShovel.setOnClickListener(this);
        mFlag.setOnClickListener(this);
        mFlagConfused.setOnClickListener(this);
        mRestart.setOnClickListener(this);
        mStartAndPaused.setOnClickListener(this);

        mHandler.sendEmptyMessage(GAME_INIT);
    }

    void initMines(Mine[][] mines,int mineCount) {
        Random random = new Random(System.currentTimeMillis());
        int createdMines = 0;
        int row;
        int column;
        rows = mines.length;
        columns = mines[0].length;

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flagButton:
                mMineView.setCurrentType();
                break;
            case R.id.restart:
                gameRestart();
                break;
            case R.id.showDialog:
                break;
            case R.id.flag:     //这里对按钮背景或则资源进行设置
            case R.id.flag_confused:
            case R.id.shovel:
            case R.id.startAndPaused:
                setBackgroundOrSrc(v.getId());
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
                    gameRestart();
                }
            });
            dialog.show();
        }
    }


    void pointDownCube(int row,int column) {
        switch (GAME_STATE) {
            case GAME_INIT:
                mHandler.sendEmptyMessage(GAME_START);
                break;
            case GAME_PAUSED:
            case GAME_OVER:
                return;
        }

        switch (mCurrentType) {
            case DRAG:
                openCube(row,column);
                break;
            case FLAG:
                flagCube(row, column);
                break;
            case FLAG_CONFUSED:
                confuseCube(row, column);
                break;
        }
        mMineView.invalidate();
    }

    /**
     * 对周围进行递归找出可以打开的方块
     * @param row
     * @param column
     */
    void openCube(int row, int column) {
        if (mines[row][column].isMine()) {  //打开的是雷，游戏直接结束
            setGameOver();
            return;
        }

        if (mines[row][column].isOpen())    //已经打开过了，结束
            return;

        mines[row][column].setOpen(true);   //首先设置为打开状态
        mines[row][column].setFlaged(false);    //去掉标记状态

        if (mines[row][column].getNum() != 0) {    //如果打开的不是0，结束
            return;
        } else {     //如果打开的是0， 判断边界是否合法，对周围8个方向进行递归
            if (row-1 >= 0 && column-1 >= 0) {
                openCube(row-1,column-1);
            }

            if (row-1 >= 0) {
                openCube(row-1,column);
            }

            if (row-1 >= 0 && column+1 < columns) {
                openCube(row-1,column+1);
            }

            if (column-1 >= 0) {
                openCube(row,column-1);
            }

            if (column+1 < columns) {
                openCube(row,column+1);
            }

            if (row+1 < rows && column-1 >= 0 ) {
                openCube(row+1,column-1);
            }

            if (row+1 < rows) {
                openCube(row+1,column);
            }

            if (row+1 < rows && column+1 < columns) {
                openCube(row+1,column+1);

            }
        }
    }

    /**
     * 标记雷
     * @param row
     * @param column
     */
    void flagCube(int row, int column) {
        if (mines[row][column].isOpen())
            return;
        if (mines[row][column].isFlaged())
            mines[row][column].setFlaged(false);
        else
            mines[row][column].setFlaged(true);
    }

    void confuseCube(int row, int column) {

    }

    void setBackgroundOrSrc(int id) {
        if (mFlag.getId() == id)  {
            mFlag.setBackgroundResource(R.drawable.image_background);
            mFlagConfused.setBackgroundResource(0);
            mShovel.setBackgroundResource(0);
            mCurrentType = FLAG;
        } else if (mFlagConfused.getId() == id) {
            mFlagConfused.setBackgroundResource(R.drawable.image_background);
            mFlag.setBackgroundResource(0);
            mShovel.setBackgroundResource(0);
            mCurrentType = PointType.FLAG_CONFUSED;
        } else if (mShovel.getId() == id) {
            mShovel.setBackgroundResource(R.drawable.image_background);
            mFlagConfused.setBackgroundResource(0);
            mFlag.setBackgroundResource(0);
            mCurrentType = DRAG;
        } else if (mStartAndPaused.getId() == id) {
            if (GAME_STATE == GAME_START || GAME_STATE == GAME_PAUSED) {
                if (mStartAndPaused.isPressed())
                    gamePause();
                else
                    gameStart();
            }
//            if (GAME_STATE == GAME_START) {
//                mStartAndPaused.setImageResource(R.mipmap.start);
//            } else if (GAME_STATE == GAME_PAUSED) {
//                mStartAndPaused.setImageResource(R.mipmap.pause);
//            } else if (GAME_STATE == GAME_INIT) {
//                mStartAndPaused.setImageResource(R.mipmap.start);
//            } else if (GAME_STATE == GAME_RESTART) {
//                mStartAndPaused.setImageResource(R.mipmap.pause);
//            }
        }
    }

    void setGameOver() {
        mMineView.setGameOver();
        mHandler.sendEmptyMessage(GAME_OVER);
        Log.i("MineView","GameOver");
    }

    @Override
    public void gameInit() {
        GAME_STATE = GAME_INIT;
        timer = new Timer();
    }

    @Override
    public void gameStart() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (GAME_STATE == GAME_START)
                    mHandler.sendEmptyMessage(TIME_CHANGED);
            }
        },1000,1000);
        mStartAndPaused.setPressed(true);
        GAME_STATE = GAME_START;
    }

    @Override
    public void gamePause() {
        mStartAndPaused.setPressed(false);
        GAME_STATE = GAME_PAUSED;
    }

    @Override
    public void gameOver() {
        GAME_STATE = GAME_OVER;
        showCustomDialog(GAME_OVER);
        Log.i("GameActivity","GameOver");
        if (timer != null)
            timer.cancel();
    }

    @Override
    public void gameRestart() {
        if (GAME_STATE == GAME_INIT)    //没有开始的时候不进行restart
            return;
        GAME_STATE = GAME_RESTART;
        if (timer != null)
            timer.cancel();
        time = 0;
        mTimeView.setText("Time:0");
        initMines(mines,mMineCount);
        mMineView.restart(mines,mMineCount);
        setBackgroundOrSrc(mShovel.getId());
//        setBackgroundOrSrc(mStartAndPaused.getId());
//        GAME_STATE = GAME_INIT;
        mHandler.sendEmptyMessage(GAME_INIT);
    }

    @Override
    public void gameSuccess() {
        GAME_STATE = GAME_SUCCESS;
        showCustomDialog(GAME_SUCCESS);
        if (timer != null)
            timer.cancel();
        Log.i("GameActivity","Success");
    }

}
