package com.chk.mines;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chk.mines.Beans.CommunicateData;
import com.chk.mines.Beans.Mine;
import com.chk.mines.CustomDialogs.CustomDialog;
import com.chk.mines.CustomDialogs.DisconnectDialog;
import com.chk.mines.CustomDialogs.RestartDialog;
import com.chk.mines.CustomDialogs.WaitingForConfirmDialog;
import com.chk.mines.CustomDialogs.WaitingForSyncDialog;
import com.chk.mines.CustomServices.ClientConnectService;
import com.chk.mines.CustomServices.ServerConnectService;
import com.chk.mines.CustomViews.CustomMineView;
import com.chk.mines.CustomViews.TimeTextView;
import com.chk.mines.Interfaces.GameState;
import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.Utils.Constant;
import com.chk.mines.Utils.GsonUtil;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.chk.mines.ChooseGameTypeActivity.CLIENT;
import static com.chk.mines.ChooseGameTypeActivity.SERVER;

public class RemovalGameActivity extends BaseActivity implements GameState,View.OnClickListener{

    private static final String TAG = RemovalGameActivity.class.getSimpleName();
    ExecutorService executorService;
    Runnable gameRunnable;
    boolean isRunning = true;
    Timer timer;
    int time;   //游戏时间
    Handler mGameHandler;

    LinearLayout.LayoutParams lp;
    LinearLayout mMineViewContainer;
    ImageView mShovel;
    ImageView mFlag;
    ImageView mFlagConfused;
    TimeTextView mTimeView;
    ImageView mRestart;
    ImageView mStartOrPaused;
    TextView mRemainMines;
    ScrollView mGameView;
    TextView mPausedView;

    CustomMineView mMineView;
    Mine[][] mines;
    int rows;
    int columns;
    int mMineCount;
    private String mMinesString;    //多人游戏时存储的雷的数据

    int curGameState;
    int preGameState;

    int mCurrentType = Constant.DRAG;   //默认是这个,挖掘的类型

    RestartDialog restartDialog;
    CustomDialog successDialog;
    CustomDialog failDialog;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_second);

        mGameHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constant.GAME_INIT:
                        gameInit();
                        break;
                    case Constant.GAME_PAUSE:
                        gamePause();
                        break;
                    case Constant.GAME_START:
                        gameStart();
                        break;
                    case Constant.GAME_SUCCESS:
                        gameSuccess();
                        break;
                    case Constant.GAME_OVER:
                        gameOver();
                        break;
                    case Constant.GAME_RESTART:
                        gameRestart();
                        break;
                    case Constant.PointDown:
                        pointDownCube(msg.arg1, msg.arg2);
                        break;
                    case Constant.TIME_CHANGED:
                        mTimeView.setText("TIME:" + time);
                        break;
                }
            }
        };
        init();
    }

    void init() {
        dataInit();
        minesInit();
        viewInit();
        curGameState = Constant.GAME_INIT;  //给定一个初始状态
    }

    void dataInit() {
        executorService = Executors.newCachedThreadPool();
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "GameThread is Running");
                while (isRunning) {
                    long startTime = System.currentTimeMillis();
                    if (curGameState != preGameState) { //状态发生改变的时候
                        mGameHandler.sendEmptyMessage(curGameState);    //通知主线程状态发生改变
                        preGameState = curGameState;    //更新至当前的状态
                    }
                    long endTime = System.currentTimeMillis();
                    try {
                        Thread.sleep(30 - endTime + startTime); //休息30ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        executorService.execute(gameRunnable);  //启动线程

        Intent intent = getIntent();
        rows = intent.getIntExtra("rows",-1);
        columns = intent.getIntExtra("columns",-1);
        mMineCount = intent.getIntExtra("mMineCount",-1);

        rows = 8;
        columns = 8;
        mMineCount = 10;
    }

    void minesInit() {
        mMineView = new CustomMineView(this, rows, columns);
        mMineView.setHandler(mGameHandler);
        resetMines();
    }

    void viewInit() {
        mMineViewContainer = findViewById(R.id.mineViewContainer);
        mShovel = findViewById(R.id.shovel);
        mFlag = findViewById(R.id.flag);
        mFlagConfused = findViewById(R.id.flag_confused);
        mTimeView = findViewById(R.id.timeView);
        mRestart = findViewById(R.id.restart);
        mStartOrPaused = findViewById(R.id.startAndPaused);
        mRemainMines = findViewById(R.id.remainMines);
        mGameView = findViewById(R.id.gameView);
        mPausedView = findViewById(R.id.pausedView);

        mShovel.setOnClickListener(this);
        mFlag.setOnClickListener(this);
        mFlagConfused.setOnClickListener(this);
        mRestart.setOnClickListener(this);
        mStartOrPaused.setOnClickListener(this);
    }

    /**
     * 重置雷的数据
     */
    void resetMines() {
        Random random = new Random(System.currentTimeMillis());
        int createdMines = 0;
        mines = new Mine[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                mines[i][j] = new Mine();
            }
        }

        int row;
        int column;
        while (createdMines < mMineCount) {
            row = random.nextInt(rows);
            column = random.nextInt(columns);
            if (!mines[row][column].isMine()) {
                mines[row][column].setMine(true);
                mines[row][column].setNum(-1);
                createdMines++;
            }
        }

        //下面开始生成雷周围的数字
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (mines[i][j].isMine()) {
                    if (i - 1 >= 0 && j - 1 >= 0 && !mines[i - 1][j - 1].isMine())
                        mines[i - 1][j - 1].setNum(mines[i - 1][j - 1].getNum() + 1);

                    if (i - 1 >= 0 && !mines[i - 1][j].isMine())
                        mines[i - 1][j].setNum(mines[i - 1][j].getNum() + 1);

                    if (i - 1 >= 0 && j + 1 < columns && !mines[i - 1][j + 1].isMine())
                        mines[i - 1][j + 1].setNum(mines[i - 1][j + 1].getNum() + 1);

                    if (j - 1 >= 0 && !mines[i][j - 1].isMine())
                        mines[i][j - 1].setNum(mines[i][j - 1].getNum() + 1);

                    if (j + 1 < columns && !mines[i][j + 1].isMine())
                        mines[i][j + 1].setNum(mines[i][j + 1].getNum() + 1);

                    if (i + 1 < rows && j - 1 >= 0 && !mines[i + 1][j - 1].isMine())
                        mines[i + 1][j - 1].setNum(mines[i + 1][j - 1].getNum() + 1);

                    if (i + 1 < rows && !mines[i + 1][j].isMine())
                        mines[i + 1][j].setNum(mines[i + 1][j].getNum() + 1);

                    if (i + 1 < rows && j + 1 < columns && !mines[i + 1][j + 1].isMine())
                        mines[i + 1][j + 1].setNum(mines[i + 1][j + 1].getNum() + 1);
                }
            }
        }

        mMinesString = GsonUtil.minesToString(mines);   //将雷的数组转化为Json数据

        for (int i = 0; i < rows; i++) {    //打印雷的数据
            String string = new String();
            for (int j = 0; j < columns; j++) {
                if (mines[i][j].getNum() == -1)
                    string += "*" + " ";
                else
                    string += mines[i][j].getNum() + " ";
            }
            Log.i("GameActivity", string);
        }
    }

    void startOrPauseGame() {
        if (curGameState == Constant.GAME_START) {
            curGameState = Constant.GAME_PAUSE;
        } else if (curGameState == Constant.GAME_PAUSE) {
            curGameState = Constant.GAME_START;
        }
    }


    /**
     * 设置按钮的背景和设置当前挖雷的类型
     *
     * @param id 按钮id
     */
    void setBackgroundAndCurrentType(int id) {
        if (mFlag.getId() == id) {
            mFlag.setBackgroundResource(R.drawable.image_background);
            mFlagConfused.setBackgroundResource(0);
            mShovel.setBackgroundResource(0);
            mCurrentType = Constant.FLAG;
        } else if (mFlagConfused.getId() == id) {
            mFlagConfused.setBackgroundResource(R.drawable.image_background);
            mFlag.setBackgroundResource(0);
            mShovel.setBackgroundResource(0);
            mCurrentType = Constant.FLAG_CONFUSED;
        } else if (mShovel.getId() == id) {
            mShovel.setBackgroundResource(R.drawable.image_background);
            mFlagConfused.setBackgroundResource(0);
            mFlag.setBackgroundResource(0);
            mCurrentType = Constant.DRAG;
        }
    }

    void pointDownCube(int row, int column) {
        switch (curGameState) {
            case Constant.GAME_INIT:
                curGameState = Constant.GAME_START;
                break;
            case Constant.GAME_PAUSE:
            case Constant.GAME_OVER:
            case Constant.GAME_SUCCESS:
                return;
        }

        switch (mCurrentType) {
            case Constant.DRAG:
                if (mines[row][column].isFlaged())  //flag状态下也不可点击
                    return;
                else
                    openCube(row, column);
                break;
            case Constant.FLAG:
                flagCube(row, column);
                break;
            case Constant.FLAG_CONFUSED:
                confuseCube(row, column);
                break;
        }
        mMineView.invalidate();     //刷新界面
        setRemainMinesOrCheckResult();
    }

    /**
     * 对周围进行递归找出可以打开的方块
     *
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
        mines[row][column].setConfused(false);  //去掉？标记

        if (mines[row][column].getNum() != 0) {    //如果打开的不是0，结束
            return;
        } else {     //如果打开的是0， 判断边界是否合法，对周围8个方向进行递归
            if (row - 1 >= 0 && column - 1 >= 0)
                openCube(row - 1, column - 1);
            if (row - 1 >= 0)
                openCube(row - 1, column);
            if (row - 1 >= 0 && column + 1 < columns)
                openCube(row - 1, column + 1);
            if (column - 1 >= 0)
                openCube(row, column - 1);
            if (column + 1 < columns)
                openCube(row, column + 1);
            if (row + 1 < rows && column - 1 >= 0)
                openCube(row + 1, column - 1);
            if (row + 1 < rows)
                openCube(row + 1, column);
            if (row + 1 < rows && column + 1 < columns)
                openCube(row + 1, column + 1);
        }
    }

    /**
     * 标记雷
     *
     * @param row
     * @param column
     */
    void flagCube(int row, int column) {
        if (mines[row][column].isOpen())
            return;
        if (mines[row][column].isConfused())
            mines[row][column].setConfused(false);
        if (mines[row][column].isFlaged())
            mines[row][column].setFlaged(false);
        else
            mines[row][column].setFlaged(true);
    }

    void confuseCube(int row, int column) {
        if (mines[row][column].isOpen())
            return;
        if (mines[row][column].isFlaged()) {
            mines[row][column].setFlaged(false);
        }
        if (mines[row][column].isConfused())
            mines[row][column].setConfused(false);
        else
            mines[row][column].setConfused(true);
    }

    /**
     * 检查已标记的雷或者检查是否排雷成功
     */
    void setRemainMinesOrCheckResult() {
        int flagMines = 0;
        int openedCount = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (mines[i][j].isFlaged())
                    flagMines++;
                if (!mines[i][j].isMine() && mines[i][j].isOpen())
                    openedCount++;

            }
        }
        if (openedCount == rows * columns - mMineCount)     //说明成功了
            curGameState = Constant.GAME_SUCCESS;    //通知GameActivity
        mRemainMines.setText("Mines:" + flagMines + "/" + mMineCount);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restart:
                showRestartDialog();
                break;
            case R.id.showDialog:
                break;
            case R.id.flag:     //这里对按钮背景或则资源进行设置
            case R.id.flag_confused:
            case R.id.shovel:
                setBackgroundAndCurrentType(v.getId());
                break;
            case R.id.startAndPaused:
                startOrPauseGame(); //暂停或者开始游戏
                break;
        }
    }

    @Override
    public void gameInit() {
        dismissAllDialog();
        mShovel.performClick();   //设置默认为点击挖掘背景
        mMineView.setMines(mines, mMineCount);
        if (mMineViewContainer.getChildCount() == 0) {   //mMineViewContainer不能重复添加同一个View，判断内部View是否为0
            lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mMineViewContainer.addView(mMineView, lp);
        } else {
            mMineView.setMines(mines, mMineCount);   //否则直接刷新
        }

        if (timer != null) {    //防止多个计时器一起并发
            timer.cancel();
            timer = null;
            time = 0;   //跟新ui至0
            mGameHandler.sendEmptyMessage(Constant.TIME_CHANGED);
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (curGameState == Constant.GAME_START) {
                    time++;
                    mGameHandler.sendEmptyMessage(Constant.TIME_CHANGED);
                }
            }
        }, 1000, 1000);

        mStartOrPaused.setImageResource(R.mipmap.pause);
        showView();
        Log.i(TAG, "GAME_INIT");
    }

    @Override
    public void gameStart() {
        mStartOrPaused.setImageResource(R.mipmap.start);
        showView();
        Log.i(TAG, "GAME_START");
    }

    @Override
    public void gamePause() {
        mStartOrPaused.setImageResource(R.mipmap.pause);
        showView();
        Log.i(TAG, "GAME_PAUSE");
    }

    @Override
    public void gameOver() {
        mStartOrPaused.setImageResource(R.mipmap.pause);
        showFailDialog();
        Log.i(TAG, "GAME_OVER");
    }

    @Override
    public void gameRestart() {
        resetMines();
        Log.i(TAG, "GAME_RESTART");
    }

    @Override
    public void gameSuccess() {
        mStartOrPaused.setImageResource(R.mipmap.pause);
        showSuccessDialog();
        Log.i(TAG, "GAME_SUCCESS");
    }

    void setGameOver() {
        curGameState = Constant.GAME_OVER;
        mMineView.setGameOver();
        Log.i("MineView", "GameOver");
    }

    void showView() {
        AlphaAnimation appearAnimation = new AlphaAnimation(0, 1);
        appearAnimation.setDuration(500);
        AlphaAnimation disappearAnimation = new AlphaAnimation(1, 0);
        disappearAnimation.setDuration(500);

        if (curGameState == Constant.GAME_START) {
            if (!mGameView.isShown()) { //不在显示的情况之下
                mGameView.setAnimation(appearAnimation);
                mGameView.setVisibility(View.VISIBLE);
                mPausedView.setAnimation(disappearAnimation);
                mPausedView.setVisibility(View.GONE);
            }
        } else if (curGameState == Constant.GAME_PAUSE) {
            if (!mPausedView.isShown()) { //不在显示的情况之下
                mPausedView.setAnimation(appearAnimation);
                mPausedView.setVisibility(View.VISIBLE);
                mGameView.setAnimation(disappearAnimation);
                mGameView.setVisibility(View.GONE);
            }
        } else if (curGameState == Constant.GAME_RESTART) {
            if (!mGameView.isShown()) { //不在显示的情况之下
                mGameView.setVisibility(View.VISIBLE);
                mPausedView.setVisibility(View.GONE);
            }
        } else if (curGameState == Constant.GAME_INIT) {
            if (!mGameView.isShown()) {
                mGameView.setVisibility(View.VISIBLE);
                mPausedView.setVisibility(View.GONE);
            }
        }
    }


    /**
     * 关闭所有正在显示的Dialog
     */
    void dismissAllDialog() {
        dismissRestartDialog();
        dismissSuccessDialog();
        dismissFailDialog();
    }

    void showRestartDialog() {
        if (restartDialog == null) {
            restartDialog = new RestartDialog(this, R.style.Custom_Dialog_Style);
            restartDialog.setCancelable(false);
            restartDialog.setOnDialogButtonClickListener(new OnDialogButtonClickListener() {
                @Override
                public void onLeftClick() {     //返回    //这里发送一个拒绝的消息
                    if (curGameState != Constant.GAME_OVER && curGameState != Constant.GAME_INIT) //OVer状态无需变成Start状态
                        curGameState = Constant.GAME_START; //继续开始游戏
                }

                @Override
                public void onRightClick() {    //继续    //这里发送一个同意的消息

                }
            });
        }
        restartDialog.show();
    }

    void dismissRestartDialog() {
        if (restartDialog != null && restartDialog.isShowing())
            restartDialog.dismiss();
    }


    void showSuccessDialog() {
        if (successDialog == null) {
            successDialog = new CustomDialog(this, R.style.Custom_Dialog_Style, R.layout.dialog_layout_success, time);
            successDialog.setOnDialogButtonClickListener(new OnDialogButtonClickListener() {
                @Override
                public void onLeftClick() {
                }

                @Override
                public void onRightClick() {
                    mRestart.performClick();
                }
            });
        }
        successDialog.show();
    }

    void dismissSuccessDialog() {
        if (successDialog != null && successDialog.isShowing()) {
            successDialog.dismiss();
        }
    }

    void showFailDialog() {
        if (failDialog == null) {
            failDialog = new CustomDialog(this, R.style.Custom_Dialog_Style, R.layout.dialog_layout_fail, -1);
            failDialog.setOnDialogButtonClickListener(new OnDialogButtonClickListener() {
                @Override
                public void onLeftClick() {

                }

                @Override
                public void onRightClick() {
                    mRestart.performClick();
                }
            });
        }
        failDialog.show();
    }

    void dismissFailDialog() {
        if (failDialog != null && failDialog.isShowing()) {
            failDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }
}
