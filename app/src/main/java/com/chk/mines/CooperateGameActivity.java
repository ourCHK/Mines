package com.chk.mines;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.chk.mines.Beans.CommunicateData;
import com.chk.mines.Beans.Mine;
import com.chk.mines.CustomDialog.CustomDialog;
import com.chk.mines.CustomDialog.WaitingForSyncDialog;
import com.chk.mines.CustomService.ClientConnectService;
import com.chk.mines.CustomService.ServerConnectService;
import com.chk.mines.Interfaces.GameState;
import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.Utils.GsonUtil;
import com.chk.mines.Views.MineView;
import com.chk.mines.Views.MineViewType1;
import com.chk.mines.Views.MineViewType2;
import com.chk.mines.Views.MineViewType3;
import com.chk.mines.Views.MineViewType4;
import com.chk.mines.Views.TimeTextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.chk.mines.ChooseGameTypeActivity.CLIENT;
import static com.chk.mines.ChooseGameTypeActivity.SERVER;
import static com.chk.mines.CooperateGameActivity.PointType.DRAG;
import static com.chk.mines.CooperateGameActivity.PointType.FLAG;
import static com.chk.mines.CooperateGameActivity.PointType.FLAG_CONFUSED;

public class CooperateGameActivity extends AppCompatActivity implements View.OnClickListener,GameState{
    public final static String TAG = CooperateGameActivity.class.getSimpleName();
    public final static String GAME_TYPE = "GameType";
    public final static String SERVER_OR_CLIENT = "ServerOrClient";

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
    private int GAME_STATE;  //游戏初始化

    public final static int PointDown = 10; //接收View传来的消息

    Handler mHandler;
    Timer timer;
    int time;

    private int mServerOrClient;
    private int mChoosedGameType;
    private boolean isSingle;

    MineView mMineView;
    Mine[][] mines;
    int rows;
    int columns;
    int mMineCount;
    private String mMinesString;    //多人游戏时存储的雷的数据

    boolean isReceivedMessage = true;    //用于判断是是否接收到了网络消息; 暂时不要管这个

    public enum PointType {    //用于判断点击下去时是什么状态，挖雷状态还是标记状态,又或者是疑惑雷标记等等
        DRAG, FLAG, FLAG_CONFUSED
    }
    PointType mCurrentType = DRAG;    //默认是挖雷状态

//    @BindView(R.id.mineViewContainer)
    LinearLayout mMineViewContainer;

//    @BindView(R.id.shovel)
    ImageView mShovel;

//    @BindView(R.id.flag)
    ImageView mFlag;

//    @BindView(R.id.flag_confused)
    ImageView mFlagConfused;

//    @BindView(R.id.timeView)
    TimeTextView mTimeView;

//    @BindView(R.id.restart)
    ImageView mRestart;

//    @BindView(R.id.startAndPaused)
    ImageView mStartAndPaused;

//    @BindView(R.id.remainMines)
    TextView mRemainMines;

//    @BindView(R.id.gameView)
    ScrollView mGameView;

//    @BindView(R.id.pausedView)
    TextView mPausedView;

    ServerConnectService mServerConnectService;
    ServiceConnection mServerConnection;
    ClientConnectService mClientConnectService;
    ServiceConnection mClientConnection;

    WaitingForSyncDialog syncDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_second);
        init();
        showSyncDialog();
    }

    @SuppressLint("HandlerLeak")
    void init() {
//        InitBindView.init(this);

        mMineViewContainer = findViewById(R.id.mineViewContainer);
        mShovel = findViewById(R.id.shovel);
        mFlag = findViewById(R.id.flag);
        mFlagConfused = findViewById(R.id.flag_confused);
        mTimeView = findViewById(R.id.timeView);
        mRestart = findViewById(R.id.restart);
        mStartAndPaused = findViewById(R.id.startAndPaused);
        mRemainMines = findViewById(R.id.remainMines);
        mGameView = findViewById(R.id.gameView);
        mPausedView = findViewById(R.id.pausedView);

        mHandler = new Handler() {
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
                        mTimeView.setText("TIME:"+ time);
                        break;
                    case PointDown:
                        pointDownCube(msg.arg1,msg.arg2);
                        break;
                    case RECEIVED_MESSAGE_FROM_SERVER:  //接收服务端传来的消息
//                        Toast.makeText(CooperateGameActivity.this, "Received message from Server!", Toast.LENGTH_SHORT).show();
                        isReceivedMessage = true;
                        receivedMessageFromServer((CommunicateData)msg.obj);
                        break;
                    case RECEIVED_MESSAGE_FROM_CLIENT:
//                        Toast.makeText(CooperateGameActivity.this, "Received message from Client!", Toast.LENGTH_SHORT).show();
                        isReceivedMessage = true;
                        receivedMessageFromClient((CommunicateData)msg.obj);
                        break;
                }
            }
        };

        Intent intent = getIntent();
        int gameType = intent.getIntExtra(GAME_TYPE,-1);
        mServerOrClient = intent.getIntExtra(SERVER_OR_CLIENT,-1);
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

//        if (isSingle) { //单人游戏
//            initMines(mines, mMineCount);
//            mMineView.setMines(mines, mMineCount);
//            mMineView.setHandler(mHandler);
//            mHandler.sendEmptyMessage(GAME_INIT);
//        } else {    //双人游戏
        switch (mServerOrClient) {
            case SERVER:
                mHandler.sendEmptyMessage(GAME_INIT);
                startBindServerService();   //启动服务
//                mMinesString = GsonUtil.minesToString(mines);
//                CommunicateData communicateData = new CommunicateData();
//                communicateData.setType(CommunicateData.GAME_STATE);
//                communicateData.setGame_state(CommunicateData.GAME_INIT);
//                communicateData.setMessage(mMinesString);

//                CommunicateData communicateData = new CommunicateData();
//                communicateData.setType(CommunicateData.OTHER);
//                communicateData.setMessage(123+"");
//                mServerConnectService.sendMessage(communicateData); //调用服务端发送消息

//                while (mServerConnectService == null) {     //因为启动会比较慢所以等不为null时才进行操作
//                    if (mServerConnectService != null)
//                        mServerConnectService.sendMessage(communicateData); //调用服务端发送消息
//                }
                break;
            case CLIENT:    //客户端还需要等待服务端传数据过来
                startBindClientService();   //先启动服务
                break;
        }

        initMines(mines, mMineCount);

//        }

        mShovel.setOnClickListener(this);
        mFlag.setOnClickListener(this);
        mFlagConfused.setOnClickListener(this);
        mRestart.setOnClickListener(this);
        mStartAndPaused.setOnClickListener(this);
    }

    void initMines(Mine[][] mines,int mineCount) {
        Random random = new Random(System.currentTimeMillis());
        int createdMines = 0;
        int row;
        int column;
        rows = mines.length;
        columns = mines[0].length;

        if (mServerOrClient == CLIENT) {    //客户端只需要上面那些数据即可
            return;
        }

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
                setBackgroundOrSrc(v.getId());
                break;
            case R.id.startAndPaused:
                startOrPauseGame();
                break;
        }
    }

    void showCustomDialog(int GameType) {
        CustomDialog dialog = null;
        switch (GameType) {
            case GAME_SUCCESS:
                dialog = new CustomDialog(this,R.style.Custom_Dialog_Style,R.layout.dialog_layout_success,time);
                break;
            case GAME_OVER:
                dialog = new CustomDialog(this,R.style.Custom_Dialog_Style,R.layout.dialog_layout_fail,-1);
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
            case GAME_SUCCESS:
                return;
        }

        int currentType = -1;

        switch (mCurrentType) {     //我之前干嘛要在这里用枚举啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊！！！
            case DRAG:
                currentType = 1;
                if (mines[row][column].isFlaged())  //flag状态下也不可点击
                    return;
                else
                    openCube(row,column);
                break;
            case FLAG:
                currentType = 2;
                flagCube(row, column);
                break;
            case FLAG_CONFUSED:
                currentType = 3;
                confuseCube(row, column);
                break;
        }
        mMineView.invalidate();     //刷新界面
        setRemainMinesOrCheckResult();
        //在这里也许应该加一个网络通信的东西，没错，我们准备加入了

        CommunicateData communicateData = new CommunicateData();
        communicateData.setType(CommunicateData.USER_OPERATION);
        communicateData.setUser_operation(currentType);
        communicateData.setRow(row);
        communicateData.setColumn(column);
        switch (mServerOrClient) {
            case SERVER:
                mServerConnectService.sendMessage(communicateData);
                break;
            case CLIENT:
                mClientConnectService.sendMessage(communicateData);
                break;
        }
    }

    void pointDownCubeFromNetWork(CommunicateData communicateData) {
        switch (GAME_STATE) {
            case GAME_INIT:
                mHandler.sendEmptyMessage(GAME_START);
                break;
            case GAME_PAUSED:
            case GAME_OVER:
            case GAME_SUCCESS:
                return;
        }
        int user_operation = communicateData.getUser_operation();
        int row = communicateData.getRow();
        int column = communicateData.getColumn();

        switch (user_operation) {
            case CommunicateData.DRAG:
                openCube(row,column);
                break;
            case CommunicateData.FLAG:
                flagCube(row, column);
                break;
            case CommunicateData.FLAG_CONFUSED:
                confuseCube(row, column);
                break;
        }
        mMineView.invalidate();     //刷新界面
        setRemainMinesOrCheckResult();
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
        mines[row][column].setConfused(false);  //去掉？标记

        if (mines[row][column].getNum() != 0) {    //如果打开的不是0，结束
            return;
        } else {     //如果打开的是0， 判断边界是否合法，对周围8个方向进行递归
            if (row-1 >= 0 && column-1 >= 0)
                openCube(row-1,column-1);
            if (row-1 >= 0)
                openCube(row-1,column);
            if (row-1 >= 0 && column+1 < columns)
                openCube(row-1,column+1);
            if (column-1 >= 0)
                openCube(row,column-1);
            if (column+1 < columns)
                openCube(row,column+1);
            if (row+1 < rows && column-1 >= 0 )
                openCube(row+1,column-1);
            if (row+1 < rows)
                openCube(row+1,column);
            if (row+1 < rows && column+1 < columns)
                openCube(row+1,column+1);
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
            mCurrentType = FLAG_CONFUSED;
        } else if (mShovel.getId() == id) {
            mShovel.setBackgroundResource(R.drawable.image_background);
            mFlagConfused.setBackgroundResource(0);
            mFlag.setBackgroundResource(0);
            mCurrentType = DRAG;
        }
    }

    void setGameOver() {
        mMineView.setGameOver();
        mHandler.sendEmptyMessage(GAME_OVER);
        Log.i("MineView","GameOver");
    }

    void startOrPauseGame() {
        if (GAME_STATE == GAME_START) {
            showView();
            gamePause();
        } else if (GAME_STATE == GAME_PAUSED) {
            showView();
            gameStart();
        }
    }

    void setRemainMinesOrCheckResult() {
        int flagMines = 0;
        int openedCount = 0;
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                if (mines[i][j].isFlaged())
                    flagMines++;
                if (!mines[i][j].isMine() && mines[i][j].isOpen())
                    openedCount++;

            }
        }
        if (openedCount == rows * columns - mMineCount)     //说明成功了
            mHandler.sendEmptyMessage(GAME_SUCCESS);    //通知GameActivity
        mRemainMines.setText("Mines:"+flagMines+"/"+mMineCount);
    }

    @Override
    public void gameInit() {
        if (isReceivedMessage) {    //如果有接到网络消息
            switch (mServerOrClient) {
                case SERVER:
                    mMineView.setMines(mines, mMineCount);
                    mMineView.setHandler(mHandler);
                    break;
                case CLIENT:
                    mMineView.setMines(mines, mMineCount);
                    mMineView.setHandler(mHandler);
                    break;
            }
        }

        GAME_STATE = GAME_INIT;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (GAME_STATE == GAME_START) {
                    time++;
                    mHandler.sendEmptyMessage(TIME_CHANGED);
                }
            }
        },1000,1000);
        mStartAndPaused.setImageResource(R.mipmap.pause);
        mRemainMines.setText("Mines:"+0+"/"+mMineCount);
    }

    @Override
    public void gameStart() {
        GAME_STATE = GAME_START;
        mStartAndPaused.setImageResource(R.mipmap.start);
    }

    @Override
    public void gamePause() {
        GAME_STATE = GAME_PAUSED;
        mStartAndPaused.setImageResource(R.mipmap.pause);
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
        showView();
        if (timer != null)
            timer.cancel();
        mHandler.sendEmptyMessage(GAME_INIT);
    }

    @Override
    public void gameSuccess() {
        GAME_STATE = GAME_SUCCESS;
        mStartAndPaused.setImageResource(R.mipmap.pause);
        showCustomDialog(GAME_SUCCESS);
        if (timer != null)
            timer.cancel();
        Log.i("GameActivity","Success");
    }

    @Override
    public void gameOver() {
        GAME_STATE = GAME_OVER;
        mStartAndPaused.setImageResource(R.mipmap.pause);
        showCustomDialog(GAME_OVER);
        Log.i("GameActivity","GameOver");
        if (timer != null)
            timer.cancel();
    }

    /**
     * 处理客户端传来的消息
     * @param message
     */
    private void receivedMessageFromClient(CommunicateData message) {
        CommunicateData communicateData = message;
        switch (communicateData.getType()) {
            case CommunicateData.USER_OPERATION:    //用户点击方块的操作
                pointDownCubeFromNetWork(communicateData);
                break;
            case CommunicateData.GAME_STATE:
                switch (communicateData.getGame_state()) {
                    case CommunicateData.CLIENT_SERVICE_BIND:   //客户端服务已经绑定,我们可以开始传输雷的数据
                        mMinesString = GsonUtil.minesToString(mines);
                        CommunicateData cd1 = new CommunicateData();
                        cd1.setType(CommunicateData.GAME_STATE);
                        cd1.setGame_state(CommunicateData.GAME_INIT);
                        cd1.setMessage(mMinesString);
                        mServerConnectService.sendMessage(cd1);
                        break;
                    case CommunicateData.GAME_INIT:
                        break;
                }
            case CommunicateData.CLIENT_RECEIVED_MESSAGE:   //客户端已经接收到消息，已经可以准备开始游戏了
                //这里应该得是有一个dialog关闭消失的操作
                dismissSyncDialog();
                Toast.makeText(CooperateGameActivity.this, "客户端已接受我们服务端发出的消息", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 处理服务端传来的消息
     * @param message
     */
    private void receivedMessageFromServer(CommunicateData message) {
        CommunicateData communicateData = message;
        switch (communicateData.getType()) {
            case CommunicateData.USER_OPERATION:    //用户点击方块的操作
                pointDownCubeFromNetWork(communicateData);
                break;
            case CommunicateData.GAME_STATE:    //游戏状态改变
                switch (communicateData.getGame_state()) {
                    case CommunicateData.GAME_INIT:
                        String arrayJson = communicateData.getMessage();
                        Mine[][] tempMines = GsonUtil.stringToMines(arrayJson);
                        for (int i=0; i<rows; i++) {
                            String string = "";
                            for (int j=0; j<columns; j++) {
                                mines[i][j] = tempMines[i][j];
                                if (mines[i][j].getNum() == -1)
                                    string += "*" + " ";
                                else
                                    string += mines[i][j].getNum() + " ";
                            }
                            Log.i("GameActivity",string);
                        }
                        mHandler.sendEmptyMessage(GAME_INIT);   //通知服务端客户端已经接收到消息
                        mHandler.postDelayed(new Runnable() {   //客户端延迟发送等待服务端服务开启
                            @Override
                            public void run() {
                                CommunicateData cd1 = new CommunicateData();
                                cd1.setType(CommunicateData.GAME_STATE);
                                cd1.setGame_state(CommunicateData.CLIENT_RECEIVED_MESSAGE);
                                mClientConnectService.sendMessage(cd1);

                                dismissSyncDialog();
                            }
                        },2000);
                        break;
                }
                break;
            case CommunicateData.OTHER:     //其他的消息，准备接受服务端发来的消息
                Mine[][] tempMines = GsonUtil.stringToMines(communicateData.getMessage());
                for (int i=0 ;i<rows; i++) {
                    for (int j=0; j<columns; j++) {
                        mines[i][j] = tempMines[i][j];
                    }
                }
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                mMineViewContainer.addView(mMineView,lp);

                mMineView.setMines(mines, mMineCount);
                mMineView.setHandler(mHandler);
                mHandler.sendEmptyMessage(GAME_INIT);
                break;
        }
    }


    void showView() {
        AlphaAnimation appearAnimation = new AlphaAnimation(0, 1);
        appearAnimation.setDuration(500);
        AlphaAnimation disappearAnimation = new AlphaAnimation(1, 0);
        disappearAnimation.setDuration(500);

        if (GAME_STATE == GAME_START) {
            mPausedView.setAnimation(appearAnimation);
            mPausedView.setVisibility(View.VISIBLE);
            mGameView.setAnimation(disappearAnimation);
            mGameView.setVisibility(View.GONE);
        } else if (GAME_STATE == GAME_PAUSED) {
            mGameView.setAnimation(appearAnimation);
            mGameView.setVisibility(View.VISIBLE);
            mPausedView.setAnimation(disappearAnimation);
            mPausedView.setVisibility(View.GONE);
        } else if (GAME_STATE == GAME_RESTART) {
            mGameView.setVisibility(View.VISIBLE);
            mPausedView.setVisibility(View.GONE);
        }
    }

    void showSyncDialog() {
        if (syncDialog == null)
            syncDialog = new WaitingForSyncDialog(this,R.style.Custom_Dialog_Style);
        syncDialog.show();
    }

    void dismissSyncDialog() {
        if (syncDialog != null)
            syncDialog.dismiss();
    }

    void startBindServerService() {
        Intent serverIntent = new Intent(this,ServerConnectService.class);
        mServerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Toast.makeText(CooperateGameActivity.this, "ServerService has Started", Toast.LENGTH_SHORT).show();
                ServerConnectService.LocalBinder binder = (ServerConnectService.LocalBinder) service;
                mServerConnectService = binder.getService();
                mServerConnectService.setGameActivityHandler(mHandler);
                Log.i(TAG,"The ServerService has started!!");

//                mMinesString = GsonUtil.minesToString(mines);
//                CommunicateData communicateData = new CommunicateData();
//                communicateData.setType(CommunicateData.GAME_STATE);
//                communicateData.setGame_state(CommunicateData.GAME_INIT);
//                communicateData.setMessage(mMinesString);
//                mServerConnectService.sendMessage(communicateData);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServerConnectService = null;
                Toast.makeText(CooperateGameActivity.this, "the ServerService has Stopped", Toast.LENGTH_SHORT).show();
                Log.i(TAG,"The ServiceService has Stopped!");
            }
        };
        bindService(serverIntent,mServerConnection,BIND_AUTO_CREATE);
    }

    void startBindClientService() {
        Intent clientIntent = new Intent(this,ClientConnectService.class);
        mClientConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
//                Toast.makeText(CooperateGameActivity.this, "The ClientService has Started", Toast.LENGTH_SHORT).show();
                ClientConnectService.LocalBinder binder = (ClientConnectService.LocalBinder) service;
                mClientConnectService = binder.getService();
                mClientConnectService.setGameActivityHandler(mHandler);
                Log.i(TAG,"The ClientService has started!!");

                CommunicateData communicateData = new CommunicateData();    //通知服务已经绑定了
                communicateData.setType(CommunicateData.GAME_STATE);
                communicateData.setGame_state(CommunicateData.CLIENT_SERVICE_BIND);
                mClientConnectService.sendMessage(communicateData);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClientConnectService = null;
                Toast.makeText(CooperateGameActivity.this, "the ClientService has Stopped", Toast.LENGTH_SHORT).show();
                Log.i(TAG,"The ClientService has Stopped!");
            }
        };
        bindService(clientIntent,mClientConnection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (syncDialog != null) {
            syncDialog.dismiss();
            syncDialog = null;
        }
        if (mClientConnection != null)
            unbindService(mClientConnection);
        if (mServerConnection != null)
            unbindService(mServerConnection);
    }

}
