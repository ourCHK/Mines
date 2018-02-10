package com.chk.mines;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chk.mines.CustomService.ClientConnectService;
import com.chk.mines.CustomService.ServerConnectService;

import static com.chk.mines.GameActivity.TYPE_1;
import static com.chk.mines.GameActivity.TYPE_2;
import static com.chk.mines.GameActivity.TYPE_3;
import static com.chk.mines.GameActivity.TYPE_4;

/**
 * 选择游戏类型和对战类型
 */
public class ChooseGameTypeActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = ChooseGameTypeActivity.class.getSimpleName();

    public static final int COOPERATOR = 1;
    public static final int FIGHTER = 2;
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
    int mCooperatorOrFight; //判断是合作还是对战

    int mChoosedGameType;
    boolean isDoublePlayer = true;

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

    void init() {
        viewInit();
        Intent intent = getIntent();
        mServerOrClient = intent.getIntExtra("ServerOrClient",-1);
        switch (mServerOrClient) {
            case CLIENT:
                mWaitingForStart.setVisibility(View.VISIBLE);
                mCurrentLayout.setVisibility(View.GONE);
                startBindClientService();
                break;
            case SERVER:
                startBindServerService();
                break;
            default:
                break;
        }
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
                mChoosedGameType = TYPE_1;
                startGameActivity();
                break;
            case R.id.type2:
                mChoosedGameType = TYPE_2;
                startGameActivity();
                break;
            case R.id.type3:
                mChoosedGameType = TYPE_3;
                startGameActivity();
                break;
            case R.id.type4:
                mChoosedGameType = TYPE_4;
                startGameActivity();
                break;
        }
    }

    void startGameActivity() {

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
        if (mWaitingForStart.isShown()) //客户端等待界面不给按返回键
            return;
        if (mCurrentLayout != mPreLayout)
            backLayout();
        else
            super.onBackPressed();
    }

    void startBindServerService() {
        Intent serverIntent = new Intent(this,ServerConnectService.class);
        mServerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Toast.makeText(ChooseGameTypeActivity.this, "ClientService has Started", Toast.LENGTH_SHORT).show();
                ServerConnectService.LocalBinder binder = (ServerConnectService.LocalBinder) service;
                mServerConnectService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServerConnectService = null;
                Toast.makeText(ChooseGameTypeActivity.this, "the ServiceService has Stopped", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ChooseGameTypeActivity.this, "ServiceService has Started", Toast.LENGTH_SHORT).show();
                ClientConnectService.LocalBinder binder = (ClientConnectService.LocalBinder) service;
                mClientConnectService = binder.getService();
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
}
