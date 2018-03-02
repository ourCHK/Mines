package com.chk.mines;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.chk.mines.Beans.CommunicateData;
import com.chk.mines.Beans.Mine;
import com.chk.mines.CustomDialog.DisconnectDialog;
import com.chk.mines.CustomDialog.RestartDialog;
import com.chk.mines.CustomDialog.WaitingForSyncDialog;
import com.chk.mines.CustomService.ClientConnectService;
import com.chk.mines.CustomService.ServerConnectService;
import com.chk.mines.Interfaces.GameState;
import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.Utils.Constant;
import com.chk.mines.Utils.GsonUtil;
import com.chk.mines.Views.CustomMineView;
import com.chk.mines.Views.MineView;
import com.chk.mines.Views.TimeTextView;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.chk.mines.ChooseGameTypeActivity.CLIENT;
import static com.chk.mines.ChooseGameTypeActivity.SERVER;

public class CooperateGameActivityWithThread extends AppCompatActivity implements GameState,View.OnClickListener{

    private final static String TAG = CooperateGameActivityWithThread.class.getSimpleName();

    ExecutorService executorService;
    Runnable gameRunnable;
    boolean isRunning = true;

    int mServerOrClient;
    int mChooseGameType;
    Handler mGameHandler;

    LinearLayout mMineViewContainer;
    ImageView mShovel;
    ImageView mFlag;
    ImageView mFlagConfused;
    TimeTextView mTimeView;
    ImageView mRestart;
    ImageView mStartAndPaused;
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

    int mCurrentType = Constant.DRAG;   //默认是这个

    ServerConnectService mServerConnectService;
    ServiceConnection mServerConnection;
    ClientConnectService mClientConnectService;
    ServiceConnection mClientConnection;

    LocalBroadcastReceiver mLocalBroadcastReceiver;
    IntentFilter mIntentFilter;
    boolean mSocketDisconnected;

    WaitingForSyncDialog syncDialog;
    DisconnectDialog disconnectDialog;
    RestartDialog restartDialog;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_second);
        mGameHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constant.GAME_INIT:
                        gameInit();
                        break;
                    case Constant.GAME_PAUSED:
                        curGameState = Constant.GAME_PAUSED;
                        break;
                    case Constant.GAME_START:
                        curGameState = Constant.GAME_START;
                        break;
                    case Constant.GAME_SUCCESS:
                        curGameState = Constant.GAME_SUCCESS;
                        break;
                    case Constant.GAME_OVER:
                        curGameState = Constant.GAME_OVER;
                        break;
                    case Constant.PointDown:
                        pointDownCube(msg.arg1,msg.arg2);
                        break;
                    case Constant.BIND_SERVICE: //对方已经绑定好Service，走到这里说明我们自己也已经是绑定了服务，走到这里就说明其实双方都已经是绑定服务了
                        serviceBound();
                        break;
                    case Constant.SOCKET_DISCONNECTED:
                        break;
                    case Constant.TIME_CHANGED:
                        break;
                    case Constant.RECEIVED_MESSAGE_FROM_SERVER:
                        receivedMessageFromServer((CommunicateData) msg.obj);
                        break;
                    case Constant.RECEIVED_MESSAGE_FROM_CLIENT:
                        receivedMessageFromClient((CommunicateData) msg.obj);
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
        serviceInit();

        showSyncDialog();
    }

    void viewInit() {
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

        mShovel.setOnClickListener(this);
        mFlag.setOnClickListener(this);
        mFlagConfused.setOnClickListener(this);
        mRestart.setOnClickListener(this);
        mStartAndPaused.setOnClickListener(this);
    }

    void dataInit() {
        executorService = Executors.newCachedThreadPool();
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"GameThread is Running");
                while (isRunning) {
                    if (curGameState != preGameState) { //状态发生改变的时候
                        mGameHandler.sendEmptyMessage(curGameState);    //通知主线程状态发生改变
                        preGameState = curGameState;    //更新至当前的状态
                    }
                }
            }
        };
        executorService.execute(gameRunnable);  //启动线程

        Intent intent = getIntent();
        int gameType = intent.getIntExtra(Constant.GAME_TYPE,-1);
        mServerOrClient = intent.getIntExtra(Constant.SERVER_OR_CLIENT,-1);
        mChooseGameType = gameType & (Constant.TYPE_1 | Constant.TYPE_2 | Constant.TYPE_3 | Constant.TYPE_4);

        mLocalBroadcastReceiver = new LocalBroadcastReceiver();
        mIntentFilter = new IntentFilter(Constant.SOCKET_DISCONNECTED_BROADCAST_ACTION);
        registerReceiver(mLocalBroadcastReceiver,mIntentFilter);
    }

    void minesInit() {
        switch (mChooseGameType) {
            case Constant.TYPE_1:
                rows = 8;
                columns = 8;
                mMineCount = 10;
                break;
            case Constant.TYPE_2:
                rows = 16;
                columns = 16;
                mMineCount = 40;
                break;
            case Constant.TYPE_3:
                rows = 16;
                columns = 30;
                mMineCount = 99;
                break;
            case Constant.TYPE_4:
                rows = -1;
                columns = -1;
                mMineCount = -1;
                break;
        }

        mMineView = new CustomMineView(this,rows,columns);

        switch (mServerOrClient) {
            case Constant.SERVER:
                resetMines();
                break;
            case Constant.CLIENT:
                break;
        }
    }

    void serviceInit() {
        switch (mServerOrClient) {
            case Constant.SERVER:
                startBindServerService();
                break;
            case Constant.CLIENT:
                startBindClientService();
                break;
        }
    }

    /**
     * 重置雷的数据
     */
    void resetMines() {
        Random random = new Random(System.currentTimeMillis());
        int createdMines = 0;
        mines = new Mine[rows][columns];
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
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

        mMinesString = GsonUtil.minesToString(mines);   //将雷的数组转化为Json数据

        for (int i=0; i<rows; i++) {    //打印雷的数据
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

    /**
     * 通过Socket传输的数据来初始化雷的数据
     * @param minesData
     */
    void resetMinesFromSocket(CommunicateData minesData) {
        mines = new Mine[rows][columns];
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                mines[i][j] = new Mine();
            }
        }

        String arrayJson = minesData.getMessage();
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

        //这边可以发送Init消息??
//        mGameHandler.sendEmptyMessage(Constant.GAME_INIT);

        curGameState = Constant.GAME_INIT;

        //还需要给对方发送一个接收到消息的信息
        CommunicateData communicateData = new CommunicateData();
        communicateData.setType(CommunicateData.GAME_STATE);
        communicateData.setGame_state(CommunicateData.RECEIVED_MINES_DATA);
        switch (mServerOrClient) {
            case Constant.SERVER:
                mServerConnectService.sendMessage(communicateData);
                break;
            case Constant.CLIENT:
                mClientConnectService.sendMessage(communicateData);
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flagButton:
//                mMineView.setCurrentType();
                break;
            case R.id.restart:
                gameRestart();
                break;
            case R.id.showDialog:
                break;
            case R.id.flag:     //这里对按钮背景或则资源进行设置
            case R.id.flag_confused:
            case R.id.shovel:
//                setBackgroundOrSrc(v.getId());
                break;
            case R.id.startAndPaused:
//                startOrPauseGame();
//                sendGameState();
                break;
        }
    }

    @Override
    public void gameInit() {
        mMineView.setMines(mines,mMineCount);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mMineViewContainer.addView(mMineView,lp);
        dismissSyncDialog();
        Log.i(TAG,"GAME_INIT");
    }

    @Override
    public void gameStart() {
        Log.i(TAG,"GAME_START");
    }

    @Override
    public void gamePause() {
        Log.i(TAG,"GAME_PAUSE");
    }

    @Override
    public void gameOver() {
        Log.i(TAG,"GAME_OVER");
    }

    @Override
    public void gameRestart() {
        Log.i(TAG,"GAME_RESTART");
    }

    @Override
    public void gameSuccess() {
        Log.i(TAG,"GAME_SUCCESS");
    }

    /**
     * 处理客户端传来的消息
     * @param message
     */
    private void receivedMessageFromClient(CommunicateData message) {
        CommunicateData communicateData = message;
        switch (communicateData.getType()) {
            case CommunicateData.USER_OPERATION:    //用户点击方块的操作
//                pointDownCubeFromNetWork(communicateData);
                break;
            case CommunicateData.GAME_STATE:
                switch (communicateData.getGame_state()) {
//                    case CommunicateData.CLIENT_SERVICE_BIND:   //客户端服务已经绑定,我们可以开始传输数据
//                        mMinesString = GsonUtil.minesToString(mines);
//                        CommunicateData cd1 = new CommunicateData();
//                        cd1.setType(CommunicateData.GAME_STATE);
//                        cd1.setGame_state(CommunicateData.GAME_INIT);
//                        cd1.setMessage(mMinesString);
//                        mServerConnectService.sendMessage(cd1);
//                        break;
                    case CommunicateData.GAME_INIT:
                        break;
                    case CommunicateData.GAME_START:
//                        startOrPauseGame();
                        break;
                    case CommunicateData.GAME_PAUSE:
//                        startOrPauseGame();
                        break;
                    case CommunicateData.SEND_MINES_DATA:   //客户端发送雷数据
                        resetMinesFromSocket(communicateData);
                        break;
                    case CommunicateData.RECEIVED_MINES_DATA:   //对方接收到我们的雷的数据
                        mGameHandler.sendEmptyMessage(Constant.GAME_INIT);
                        break;
                }
            case CommunicateData.CLIENT_RECEIVED_MESSAGE:   //客户端已经接收到消息，已经可以准备开始游戏了
                //这里应该得是有一个dialog关闭消失的操作
//                dismissSyncDialog();
                Toast.makeText(CooperateGameActivityWithThread.this, "客户端已接受我们服务端发出的消息", Toast.LENGTH_SHORT).show();
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
//                pointDownCubeFromNetWork(communicateData);
                break;
            case CommunicateData.GAME_STATE:    //游戏状态改变
                switch (communicateData.getGame_state()) {
                    case CommunicateData.GAME_INIT:
//                        String arrayJson = communicateData.getMessage();
//                        Mine[][] tempMines = GsonUtil.stringToMines(arrayJson);
//                        for (int i=0; i<rows; i++) {
//                            String string = "";
//                            for (int j=0; j<columns; j++) {
//                                mines[i][j] = tempMines[i][j];
//                                if (mines[i][j].getNum() == -1)
//                                    string += "*" + " ";
//                                else
//                                    string += mines[i][j].getNum() + " ";
//                            }
//                            Log.i("GameActivity",string);
//                        }
////                        mHandler.sendEmptyMessage(GAME_INIT);   //通知服务端客户端已经接收到消息
//
//                        CommunicateData cd1 = new CommunicateData();
//                        cd1.setType(CommunicateData.GAME_STATE);
//                        cd1.setGame_state(CommunicateData.CLIENT_RECEIVED_MESSAGE);
//                        mClientConnectService.sendMessage(cd1);
//
//                        dismissSyncDialog();
//                        mHandler.postDelayed(new Runnable() {   //客户端延迟发送等待服务端服务开启
//                            @Override
//                            public void run() {
//                                CommunicateData cd1 = new CommunicateData();
//                                cd1.setType(CommunicateData.GAME_STATE);
//                                cd1.setGame_state(CommunicateData.CLIENT_RECEIVED_MESSAGE);
//                                mClientConnectService.sendMessage(cd1);
//
//                                dismissSyncDialog();
//                            }
//                        },2000);
                        break;
                    case CommunicateData.GAME_START:
//                        mStartAndPaused.callOnClick();
//                        startOrPauseGame();
                        break;
                    case CommunicateData.GAME_PAUSE:
//                        mStartAndPaused.callOnClick();
//                        startOrPauseGame();
                        break;
                    case CommunicateData.SEND_MINES_DATA:   //服务端发送雷数据过来
                        resetMinesFromSocket(communicateData);
                        break;
                    case CommunicateData.RECEIVED_MINES_DATA:   //对方接收到我们的雷的数据
                        curGameState = Constant.GAME_INIT;  //说明初始化成功
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

//                mMineView.setMines(mines, mMineCount);
//                mMineView.setHandler(mHandler);
//                mHandler.sendEmptyMessage(GAME_INIT);
                break;
        }
    }

    void pointDownCube(int row,int column) {
        switch (curGameState) {
            case Constant.GAME_INIT:
                mGameHandler.sendEmptyMessage(Constant.GAME_START);
                break;
            case Constant.GAME_PAUSED:
            case Constant.GAME_OVER:
            case Constant.GAME_SUCCESS:
                return;
        }

        switch (mCurrentType) {
            case Constant.DRAG:
                if (mines[row][column].isFlaged())  //flag状态下也不可点击
                    return;
                else
                    openCube(row,column);
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

        if (mSocketDisconnected)    //如果socket已经断开连接了,那么直接返回即可
            return;

        //在这里也许应该加一个网络通信的东西，没错，我们准备加入了
        CommunicateData communicateData = new CommunicateData();
        communicateData.setType(CommunicateData.USER_OPERATION);
        communicateData.setUser_operation(mCurrentType);
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
            curGameState = Constant.GAME_SUCCESS;    //通知GameActivity
        mRemainMines.setText("Mines:"+flagMines+"/"+mMineCount);
    }

    void setGameOver() {
        curGameState = Constant.GAME_OVER;
        mMineView.setGameOver();
        Log.i("MineView","GameOver");
    }


    void showSyncDialog() {
        if (syncDialog == null)
            syncDialog = new WaitingForSyncDialog(this,R.style.Custom_Dialog_Style);
        syncDialog.show();
    }

    void dismissSyncDialog() {
        if (syncDialog != null) {
            syncDialog.dismiss();
        }
    }

    void showDisconnectDialog() {
        if (disconnectDialog == null) {
            disconnectDialog = new DisconnectDialog(this,R.style.Custom_Dialog_Style);
            disconnectDialog.setOnDialogButtonClickListener(new OnDialogButtonClickListener() {
                @Override
                public void onLeftClickListener() {     //返回
                    dismissDisconnectDialog();
                    finish();
                }

                @Override
                public void onRightClickListener() {    //继续
//                    mHandler.sendEmptyMessage(GAME_START);
//                    dismissDisconnectDialog();
                }
            });
        }
        disconnectDialog.show();
    }

    void dismissDisconnectDialog() {
        if (disconnectDialog != null)
            disconnectDialog.dismiss();
    }

    void showRestartDialog() {
        if (restartDialog == null) {
            restartDialog = new RestartDialog(this,R.style.Custom_Dialog_Style);
            restartDialog.setOnDialogButtonClickListener(new OnDialogButtonClickListener() {
                @Override
                public void onLeftClickListener() {     //返回
//                    dismissDisconnectDialog();
//                    finish();
                }

                @Override
                public void onRightClickListener() {    //继续
//                    mHandler.sendEmptyMessage(GAME_START);
//                    dismissDisconnectDialog();
                }
            });
        }
        restartDialog.show();
    }

    void dismissRestartDialog() {
        if (restartDialog != null)
            restartDialog.dismiss();
    }

    /**
     * 双方服务绑定之后
     */
    void serviceBound() {
        switch (mServerOrClient) {
            case Constant.SERVER:   //服务端应该发送雷的数据
                CommunicateData cd1 = new CommunicateData();
                cd1.setType(CommunicateData.GAME_STATE);
                cd1.setGame_state(CommunicateData.SEND_MINES_DATA);
                cd1.setMessage(mMinesString);
                mServerConnectService.sendMessage(cd1);
                break;
            case Constant.CLIENT:
                break;
        }
    }

    void startBindServerService() {
        Intent serverIntent = new Intent(this,ServerConnectService.class);
        mServerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Toast.makeText(CooperateGameActivityWithThread.this, "ServerService has Started", Toast.LENGTH_SHORT).show();
                ServerConnectService.LocalBinder binder = (ServerConnectService.LocalBinder) service;
                mServerConnectService = binder.getService();
                mServerConnectService.setGameActivityHandler(mGameHandler);
                Log.i(TAG,"The ServerService has started!!");

                CommunicateData communicateData = new CommunicateData();    //通知客户端服务端已经绑定服务
                communicateData.setType(CommunicateData.BIND_SERVICE);
                mServerConnectService.sendMessage(communicateData);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServerConnectService = null;
                Toast.makeText(CooperateGameActivityWithThread.this, "the ServerService has Stopped", Toast.LENGTH_SHORT).show();
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
                ClientConnectService.LocalBinder binder = (ClientConnectService.LocalBinder) service;
                mClientConnectService = binder.getService();
                mClientConnectService.setGameActivityHandler(mGameHandler);
                Log.i(TAG,"The ClientService has started!!");

                CommunicateData communicateData = new CommunicateData();    //通知服务端客户端已经绑定服务
                communicateData.setType(CommunicateData.BIND_SERVICE);
                mClientConnectService.sendMessage(communicateData);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClientConnectService = null;
                Toast.makeText(CooperateGameActivityWithThread.this, "the ClientService has Stopped", Toast.LENGTH_SHORT).show();
                Log.i(TAG,"The ClientService has Stopped!");
            }
        };
        bindService(clientIntent,mClientConnection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLocalBroadcastReceiver);
        if (syncDialog != null) {
            syncDialog.dismiss();
            syncDialog = null;
        }
        if (mClientConnection != null)
            unbindService(mClientConnection);
        if (mServerConnection != null)
            unbindService(mServerConnection);
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(context, "received the Broadcast", Toast.LENGTH_SHORT).show();
//            Log.i(TAG,"broadcast received");
            mSocketDisconnected = true; //设置socket断开标志位
            showDisconnectDialog();
        }
    }
}
