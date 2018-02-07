package com.chk.mines;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TableLayout;

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

    View mCurrentLayout;
    View mPreLayout;

    @BindView(R.id.tableLayout)
    TableLayout mTableLayout;

    @BindView(R.id.gridView)
    GridLayout mGridLayout;

    @BindView(R.id.connectType)
    TableLayout mConnectType;

    @BindView(R.id.doublePlayer)
    Button doublePlayer;

    @BindView(R.id.singlePlayer)
    Button singlePlayer;

    @BindView(R.id.type1)
    Button mType1;

    @BindView(R.id.type2)
    Button mType2;

    @BindView(R.id.type3)
    Button mType3;

    @BindView(R.id.type4)
    Button mType4;

    @BindView(R.id.wifiConnector)
    Button mWifiConnector;

    @BindView(R.id.bluetoothConnector)
    Button mBlueConnector;

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
        InitBindView.init(this);
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

        }
    }

    void startGameActivity() {
        mChoosedGameType = mChoosedGameType | FLAG_IS_SINGLE;

        Intent intent = new Intent(this,GameActivity.class);
        intent.putExtra(GAME_TYPE,mChoosedGameType);
        startActivity(intent);
    }

    void startConnectActivity() {
        Intent intent  = new Intent(this,ConnectActivity.class);
        intent.putExtra("ConnectType",mConnectorType);
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
}
