package com.chk.mines;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.chk.mines.CustomDialogs.AcceptOrRejectDialog;
import com.chk.mines.CustomDialogs.LeaveForDialog;
import com.chk.mines.CustomDialogs.WaitingForDialog;
import com.chk.mines.Interfaces.OnDialogButtonClickListener;

import java.util.ArrayList;

/**
 * Created by chk on 18-3-8.
 * 提供Activity的一些公共类型的方法
 */

public class BaseActivity extends AppCompatActivity {
    public static ArrayList<AppCompatActivity> mActivityList = new ArrayList<>();
    private static AppCompatActivity mCurResumeActivity; //当前Resume的Activity;

    private AcceptOrRejectDialog acceptOrRejectDialog;
    private WaitingForDialog waitingForNewGameDialog;
    private LeaveForDialog leaveForCurGameDialog;   //离开当前游戏
    private LeaveForDialog leaveForMultipleGameDialog;   //离开多人游戏

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSelfToActivityList(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCurResumeActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitingForNewGameDialog != null) {
            waitingForNewGameDialog.dismiss();
            waitingForNewGameDialog = null;
        }

        if (acceptOrRejectDialog != null) {
            acceptOrRejectDialog.dismiss();
            acceptOrRejectDialog = null;
        }
        removeFromActivityList(this);
        if (mActivityList.size() == 0)  //如果已经没有Activity，说明准备离开BaseActivity了
            mCurResumeActivity = null;  //设置为null;防止一方退出一方接收到消息报错
    }

    /**
     * 将自己添加到ActivityList中去
     * @param activity
     */
    public void addSelfToActivityList(AppCompatActivity activity) {
        mActivityList.add(activity);
    }

    /**
     * 将自己从ActivityList中移除
     * @param activity
     */
    public void removeFromActivityList(AppCompatActivity activity) {
        mActivityList.remove(activity);
    }

    /**
     * 设置当前正在Resume状态的Activity
     * @param activity
     */
    public void setCurResumeActivity(AppCompatActivity activity) {
        mCurResumeActivity = activity;
    }

    /**
     * 获取当前正在Resume的Activity
     */
    public static AppCompatActivity getCurResumeActivity() {
        return mCurResumeActivity;
    }

    /**
     * 显示正在开始新游戏请求的Dialog
     */
    public void showStartNewGameRequestDialog(OnDialogButtonClickListener onDialogButtonClickListener) {
        acceptOrRejectDialog = new AcceptOrRejectDialog(mCurResumeActivity,R.style.Custom_Dialog_Style,"对方请求开始新游戏",10);
        if (onDialogButtonClickListener != null)
            acceptOrRejectDialog.setOnDialogButtonClickListener(onDialogButtonClickListener);
        acceptOrRejectDialog.show();
    }

    /**
     * 显示等待对方同意开始新游戏的Dialog
     */
    public void showWaitingAcceptNewGameDialog() {
        waitingForNewGameDialog = new WaitingForDialog(mCurResumeActivity,R.style.Custom_Dialog_Style,"请等待对方同意");
        waitingForNewGameDialog.show();
    }

    public void dismissWaitingAcceptNewGameDialog() {
        if (waitingForNewGameDialog != null) {
            waitingForNewGameDialog.dismiss();
        }
    }

    /**
     * 显示离开当前游戏的Dialog
     */
    public void showLeaveCurGameDialog() {
        if (leaveForCurGameDialog == null) {
            leaveForCurGameDialog = new LeaveForDialog(this,R.style.Custom_Dialog_Style,"对方已离开当前游戏");
        }
        leaveForCurGameDialog.show();
    }

    /**
     * 显示对方退出多人游戏的Dialog
     */
    public void showLeaveMultipleGameDialog() {
        if (leaveForMultipleGameDialog == null) {
            leaveForMultipleGameDialog = new LeaveForDialog(this,R.style.Custom_Dialog_Style,"对方已离开多人游戏");
        }
        leaveForMultipleGameDialog.show();
    }
}
