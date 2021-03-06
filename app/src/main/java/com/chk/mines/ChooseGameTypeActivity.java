package com.chk.mines;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chk.mines.Beans.CommunicateData;
import com.chk.mines.CustomServices.ClientConnectService;
import com.chk.mines.CustomServices.ServerConnectService;
import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.Utils.Constant;

import static com.chk.mines.GameActivity.GAME_TYPE;
import static com.chk.mines.GameActivity.SERVER_OR_CLIENT;
import static com.chk.mines.GameActivity.TYPE_1;
import static com.chk.mines.GameActivity.TYPE_2;
import static com.chk.mines.GameActivity.TYPE_3;
import static com.chk.mines.GameActivity.TYPE_4;

/**
 * 选择游戏类型和对战类型
 */
public class ChooseGameTypeActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = ChooseGameTypeActivity.class.getSimpleName();

    public static final int readyForStart = 1;  //服务端类型已经选好了，准备开始;

    public static final int COOPERATOR = 1<<6;
    public static final int FIGHTER = 1<<7;
    public final static int SERVER = 4; //服务端
    public final static int CLIENT = 5; //客户端


    View mCurrentLayout;
    View mPreLayout;

    TextView mWaitingForStart;

    TableLayout mTableLayout;

    GridLayout mGridLayout;

    Button mCooperator;

    Button mFighter;

    Button mType1;

    Button mType2;

    Button mType3;

    Button mType4;

    int mServerOrClient;    //判断是服务端还是客户端
    int mCooperatorOrFight = COOPERATOR; //默认是合作，不做对战了

    int mChooseGameType;

    Handler mHandler;

    boolean isFirstOpenActivity;    //用于判断是不是第一次打开Activity

    ServerConnectService mServerConnectService;
    ServiceConnection mServerConnection;
    ClientConnectService mClientConnectService;
    ServiceConnection mClientConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_game_type);
        init();
    }

    @SuppressLint("HandlerLeak")
    void init() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case readyForStart: //客户端接收到服务端的开始命令
                        mChooseGameType = Integer.parseInt((String) msg.obj);
                        Log.i(TAG,"client received message:"+ mChooseGameType);
                        startGameActivity();
                        break;
                    case Constant.RECEIVED_MESSAGE_FROM_SERVER:
                        receivedMessageFromServer((CommunicateData)msg.obj);
                        break;
                    case Constant.RECEIVED_MESSAGE_FROM_CLIENT:
                        receivedMessageFromClient((CommunicateData)msg.obj);
                        break;
                    case Constant.ASK_FOR_NEW_GAME:
                        break;
                    case Constant.ACCEPT_NEW_GAME:
                        dismissWaitingAcceptNewGameDialog();
                        startGameActivity();
                        break;
                    case Constant.REJECT_NEW_GAME:
                        dismissWaitingAcceptNewGameDialog();
                        Toast.makeText(mServerConnectService, "对方拒绝开始新游戏", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        viewInit();
        Intent intent = getIntent();
        mServerOrClient = intent.getIntExtra("ServerOrClient",-1);
        switch (mServerOrClient) {
            case CLIENT:
                mWaitingForStart.setVisibility(View.VISIBLE);
                mGridLayout.setVisibility(View.GONE);
                startBindClientService();
                break;
            case SERVER:
                mGridLayout.setVisibility(View.VISIBLE);
                mWaitingForStart.setVisibility(View.GONE);
                startBindServerService();
                break;
            default:
                break;
        }
        isFirstOpenActivity = true;
    }

    /**
     * 返回activity的Handler
     * @return
     */
    public Handler getHandler() {
            return mHandler;
    }

    void viewInit() {
        mWaitingForStart = findViewById(R.id.waitingForStart);
        mTableLayout = findViewById(R.id.tableLayout);
        mGridLayout = findViewById(R.id.gridView);
        mCooperator = findViewById(R.id.cooperator);
        mFighter = findViewById(R.id.fighter);
        mType1 = findViewById(R.id.type1);
        mType2 = findViewById(R.id.type2);
        mType3 = findViewById(R.id.type3);
        mType4 = findViewById(R.id.type4);

        mCooperator.setOnClickListener(this);
        mFighter.setOnClickListener(this);
        mType1.setOnClickListener(this);
        mType2.setOnClickListener(this);
        mType3.setOnClickListener(this);
        mType4.setOnClickListener(this);

        mCurrentLayout = mTableLayout;
        mPreLayout = mTableLayout;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cooperator:
                mCooperatorOrFight = COOPERATOR;
                openNewLayout(mGridLayout);
                break;
            case R.id.fighter:
                mCooperatorOrFight = FIGHTER;
                openNewLayout(mGridLayout);
                break;
            case R.id.type1:
                mChooseGameType = TYPE_1 | mCooperatorOrFight;
                askForNewGame();
//                startGameActivity();
                break;
            case R.id.type2:
                mChooseGameType = TYPE_2 | mCooperatorOrFight;
                askForNewGame();
//                startGameActivity();
                break;
            case R.id.type3:
                mChooseGameType = TYPE_3 | mCooperatorOrFight;
                askForNewGame();
//                startGameActivity();
                break;
            case R.id.type4:
                mChooseGameType = TYPE_4 | mCooperatorOrFight;
                askForNewGame();
//                startGameActivity();
                break;
        }
    }

    /**
     * 处理服务端传来的消息
     *
     * @param message
     */
    private void receivedMessageFromServer(CommunicateData message) {
        CommunicateData communicateData = message;
        switch (communicateData.getType()) {
            case CommunicateData.GAME_STATE: {
                switch (communicateData.getGame_state()) {
                    case CommunicateData.ASK_FOR_NEW_GAME:
                        //显示请求开始新游戏的Dialog,左右键的操作
                        showStartNewGameRequestDialog(new OnDialogButtonClickListener() {
                            @Override
                            public void onLeftClick() {
                                CommunicateData communicateData = new CommunicateData();
                                communicateData.setType(CommunicateData.GAME_STATE);
                                communicateData.setGame_state(CommunicateData.REJECT_NEW_GAME);
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
                            public void onRightClick() {
                                CommunicateData communicateData = new CommunicateData();
                                communicateData.setType(CommunicateData.GAME_STATE);
                                communicateData.setGame_state(CommunicateData.ACCEPT_NEW_GAME);
                                switch (mServerOrClient) {
                                    case Constant.SERVER:
                                        mServerConnectService.sendMessage(communicateData);
                                        break;
                                    case Constant.CLIENT:
                                        mClientConnectService.sendMessage(communicateData);
                                        break;
                                }
                            }
                        });
                        break;
                    case CommunicateData.LEAVE_MULTIPLE_GAME:
                        showLeaveMultipleGameDialog();
                        break;
                }
            }
        }
    }

    /**
     * 处理服务端传来的消息
     *
     * @param message
     */
    private void receivedMessageFromClient(CommunicateData message) {
        CommunicateData communicateData = message;
        switch (communicateData.getType()) {
            case CommunicateData.GAME_STATE:
                switch (communicateData.getGame_state()) {
                    case CommunicateData.ACCEPT_NEW_GAME:   //对方同意开始新游戏
                        dismissWaitingAcceptNewGameDialog();
                        startGameActivity();
                        break;
                    case CommunicateData.REJECT_NEW_GAME:  //对方拒绝开始新游戏
                        dismissWaitingAcceptNewGameDialog();
                        Toast.makeText(ChooseGameTypeActivity.this, "对方拒绝开始新游戏", Toast.LENGTH_SHORT).show();
                        break;
                    case CommunicateData.LEAVE_MULTIPLE_GAME:
                        showLeaveMultipleGameDialog();
                        break;
                }
                break;
        }
    }

    void startGameActivity() {
        switch (mServerOrClient) {
            case SERVER:
                CommunicateData communicateData = new CommunicateData();
                communicateData.setType(CommunicateData.OTHER);
                communicateData.setMessage(mChooseGameType +"");
                mServerConnectService.sendMessage(communicateData); //调用服务端发送消息
                break;
            case CLIENT:
                break;
        }
        Intent intent;
        if ((mChooseGameType & COOPERATOR) != 0) { //说明是Cooperator类型的
            intent =  new Intent(this,CooperateGameActivityWithThread.class);
        } else {    //说明是Fight类型的
            intent =  new Intent(this,FightGameActivity.class);
        }
        intent.putExtra(GAME_TYPE, mChooseGameType);
        intent.putExtra(SERVER_OR_CLIENT,mServerOrClient);
        startActivity(intent);
    }

    /**
     * 请求开始新游戏
     */
    void askForNewGame() {
        //这里可以加一个判断对方是否退出游戏的判断
        showWaitingAcceptNewGameDialog();

        CommunicateData communicateData = new CommunicateData();
        communicateData.setType(CommunicateData.GAME_STATE);
        communicateData.setGame_state(CommunicateData.ASK_FOR_NEW_GAME);
        mServerConnectService.sendMessage(communicateData);
    }

    void openNewLayout(View newLayout) {
        AlphaAnimation appearAnimation = new AlphaAnimation(0, 1);
        appearAnimation.setDuration(500);
        AlphaAnimation disappearAnimation = new AlphaAnimation(1, 0);
        disappearAnimation.setDuration(500);

        mPreLayout = mCurrentLayout;
        mCurrentLayout.setAnimation(disappearAnimation);
        mCurrentLayout.setVisibility(View.GONE);
        mCurrentLayout = newLayout;
        mCurrentLayout.setAnimation(appearAnimation);
        mCurrentLayout.setVisibility(View.VISIBLE);
    }

    void backLayout() {
        AlphaAnimation appearAnimation = new AlphaAnimation(0, 1);
        appearAnimation.setDuration(500);
        AlphaAnimation disappearAnimation = new AlphaAnimation(1, 0);
        disappearAnimation.setDuration(500);

        mCurrentLayout.setAnimation(disappearAnimation);
        mCurrentLayout.setVisibility(View.GONE);
        mPreLayout.setAnimation(appearAnimation);
        mPreLayout.setVisibility(View.VISIBLE);
        mCurrentLayout = mPreLayout;
    }

    @Override
    public void onBackPressed() {
//        if (mWaitingForStart.isShown()) //客户端等待界面不给按返回键
//            return;
//        if (mCurrentLayout != mPreLayout)
//            backLayout();
//        else
        CommunicateData cd = new CommunicateData(); //通知对方已退出多人游戏
        cd.setType(CommunicateData.GAME_STATE);
        cd.setGame_state(CommunicateData.LEAVE_MULTIPLE_GAME);
        switch (mServerOrClient) {
            case SERVER:
                mServerConnectService.sendMessage(cd);
                break;
            case CLIENT:
                mClientConnectService.sendMessage(cd);
                break;
        }

        super.onBackPressed();
    }

    void startBindServerService() {
        Intent serverIntent = new Intent(this,ServerConnectService.class);
        mServerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Toast.makeText(ChooseGameTypeActivity.this, "ServerService has Started", Toast.LENGTH_SHORT).show();
                ServerConnectService.LocalBinder binder = (ServerConnectService.LocalBinder) service;
                mServerConnectService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServerConnectService = null;
                Toast.makeText(ChooseGameTypeActivity.this, "the ServerService has Stopped", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ChooseGameTypeActivity.this, "ClientService has Started", Toast.LENGTH_SHORT).show();
                ClientConnectService.LocalBinder binder = (ClientConnectService.LocalBinder) service;
                mClientConnectService = binder.getService();
                mClientConnectService.setChooseGameTypeActivityHandler(mHandler);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClientConnectService = null;
                Toast.makeText(ChooseGameTypeActivity.this, "the ClientService has Stopped", Toast.LENGTH_SHORT).show();
                Log.i(TAG,"The ClientService has Stopped!");
            }
        };
        bindService(clientIntent,mClientConnection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClientConnection != null)
            unbindService(mClientConnection);
        if (mServerConnection != null)
            unbindService(mServerConnection);
    }
}
