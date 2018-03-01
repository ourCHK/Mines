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
import com.chk.mines.Views.MineView;
import com.chk.mines.Views.TimeTextView;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    MineView mMineView;
    Mine[][] mines;
    int rows;
    int columns;
    int mMineCount;
    private String mMinesString;    //多人游戏时存储的雷的数据

    int curGameState;
    int preGameState;

    ServerConnectService mServerConnectService;
    ServiceConnection mServerConnection;
    ClientConnectService mClientConnectService;
    ServiceConnection mClientConnection;

    WaitingForSyncDialog syncDialog;
    DisconnectDialog disconnectDialog;
    RestartDialog restartDialog;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooperate_game_with_thread);
        mGameHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constant.GAME_INIT:
                        curGameState = Constant.GAME_INIT;
                        break;
                    case Constant.GAME_PAUSED:
                        break;
                    case Constant.GAME_START:
                        break;
                    case Constant.GAME_SUCCESS:
                        break;
                    case Constant.GAME_OVER:
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
        viewInit();
        dataInit();
        minesInit();
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
                while (isRunning) {
                    if (curGameState != preGameState) { //状态发生改变的时候
                        switch (curGameState) {

                        }
                        preGameState = curGameState;    //更新至当前的状态
                    }
                }
            }
        };

        Intent intent = getIntent();
        int gameType = intent.getIntExtra(Constant.GAME_TYPE,-1);
        mServerOrClient = intent.getIntExtra(Constant.SERVER_OR_CLIENT,-1);
        mChooseGameType = gameType & (Constant.TYPE_1 | Constant.TYPE_2 | Constant.TYPE_3 | Constant.TYPE_4);
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
        mGameHandler.sendEmptyMessage(Constant.GAME_INIT);
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

    }

    @Override
    public void gameStart() {

    }

    @Override
    public void gamePause() {

    }

    @Override
    public void gameOver() {

    }

    @Override
    public void gameRestart() {

    }

    @Override
    public void gameSuccess() {

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

                        break;
                }
            case CommunicateData.CLIENT_RECEIVED_MESSAGE:   //客户端已经接收到消息，已经可以准备开始游戏了
                //这里应该得是有一个dialog关闭消失的操作
                dismissSyncDialog();
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
//                mMineView.setHandler(mHandler);
//                mHandler.sendEmptyMessage(GAME_INIT);
                break;
        }
    }


    void showSyncDialog() {
        if (syncDialog == null)
            syncDialog = new WaitingForSyncDialog(this,R.style.Custom_Dialog_Style);
        syncDialog.show();
    }

    void dismissSyncDialog() {
        if (syncDialog != null) {
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    syncDialog.dismiss();
//                }
//            },1000);
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
}
