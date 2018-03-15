package com.chk.mines;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;

import com.chk.mines.CustomDialogs.RecordDialog;
import com.chk.mines.CustomDialogs.RestartDialog;
import com.chk.mines.CustomServices.ServerConnectService;
import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.Utils.BindView;
import com.chk.mines.Utils.InitBindView;

import static com.chk.mines.ConnectActivity.BLUETOOTH;
import static com.chk.mines.ConnectActivity.WIFI;
import static com.chk.mines.GameActivity.FLAG_IS_SINGLE;
import static com.chk.mines.GameActivity.GAME_TYPE;
import static com.chk.mines.GameActivity.TYPE_1;
import static com.chk.mines.GameActivity.TYPE_2;
import static com.chk.mines.GameActivity.TYPE_3;
import static com.chk.mines.GameActivity.TYPE_4;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    RestartDialog restartDialog;
    RecordDialog recordDialog;

    View mCurrentLayout;
    View mPreLayout;

    TableLayout mTableLayout;
    GridLayout mGridLayout;
    TableLayout mConnectType;
    Button doublePlayer;
    Button singlePlayer;
    Button mType1;
    Button mType2;
    Button mType3;
    Button mType4;
    Button mWifiConnector;
    Button mBlueConnector;
    Button mAbout;
    Button mRecord;

    int mConnectorType = -1;

    int mChoosedGameType;
    boolean isSingleGame = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    void init() {
//        InitBindView.init(this);
        mTableLayout = findViewById(R.id.tableLayout);
        mGridLayout = findViewById(R.id.gridView);
        mConnectType = findViewById(R.id.connectType);
        doublePlayer = findViewById(R.id.doublePlayer);
        singlePlayer = findViewById(R.id.singlePlayer);
        mRecord = findViewById(R.id.record);
        mAbout = findViewById(R.id.about);
        mType1 = findViewById(R.id.type1);
        mType2 = findViewById(R.id.type2);
        mType3 = findViewById(R.id.type3);
        mType4 = findViewById(R.id.type4);
        mWifiConnector = findViewById(R.id.wifiConnector);
        mBlueConnector = findViewById(R.id.bluetoothConnector);

        mCurrentLayout = mTableLayout;
        mPreLayout = mTableLayout;

        singlePlayer.setOnClickListener(this);
        doublePlayer.setOnClickListener(this);
        mType1.setOnClickListener(this);
        mType2.setOnClickListener(this);
        mType3.setOnClickListener(this);
        mType4.setOnClickListener(this);
        mWifiConnector.setOnClickListener(this);
        mBlueConnector.setOnClickListener(this);
        mAbout.setOnClickListener(this);
        mRecord.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.singlePlayer:
                openNewLayout(mGridLayout);
                break;
            case R.id.doublePlayer:
                openNewLayout(mConnectType);
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
            case R.id.wifiConnector:
                mConnectorType = WIFI;
                startConnectActivity();
                break;
            case R.id.bluetoothConnector:
                mConnectorType = BLUETOOTH;
                startConnectActivity();
                break;
            case R.id.about:
                showRestartDialog();
                break;
            case R.id.record:
                showRecordDialog();
                break;

        }
    }

    void startGameActivity() {
        mChoosedGameType = mChoosedGameType | FLAG_IS_SINGLE;

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GAME_TYPE, mChoosedGameType);
        startActivity(intent);
    }

    void startConnectActivity() {
        Intent intent = new Intent(this, ConnectActivity.class);
        intent.putExtra("ConnectType", mConnectorType);
        startActivity(intent);
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
        if (mCurrentLayout != mPreLayout)
            backLayout();
        else
            super.onBackPressed();
    }

    void showRestartDialog() {  //测试
        if (restartDialog == null) {
            restartDialog = new RestartDialog(this, R.style.Theme_AppCompat_Dialog);
            restartDialog.setOnDialogButtonClickListener(new OnDialogButtonClickListener() {
                @Override
                public void onLeftClick() {     //返回
                    Toast.makeText(MainActivity.this, "CLick the left", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRightClick() {    //继续
                }
            });
        }
        restartDialog.show();
    }

    void showRecordDialog() {
        if (recordDialog == null) {
            recordDialog = new RecordDialog(this,R.style.Theme_AppCompat_Dialog);
        }
        recordDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, ServerConnectService.class);
        stopService(intent);
    }
}