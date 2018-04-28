package com.chk.mines;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.chk.mines.Beans.Record;
import com.chk.mines.Beans.RecordList;
import com.chk.mines.CustomDialogs.AcceptOrRejectDialog;
import com.chk.mines.CustomDialogs.LeaveForDialog;
import com.chk.mines.CustomDialogs.NewRecordDialog;
import com.chk.mines.CustomDialogs.WaitingForDialog;
import com.chk.mines.DataBase.RecordDao;
import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.Utils.Constant;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by chk on 18-3-8.
 * 提供Activity的一些公共类型的方法
 */

public class BaseActivity extends AppCompatActivity {

    final static String TAG = BaseActivity.class.getSimpleName();

    public static ArrayList<Record> mTypeOneList = new ArrayList<>(5);
    public static ArrayList<Record> mTypeTwoList = new ArrayList<>(5);
    public static ArrayList<Record> mTypeThreeList = new ArrayList<>(5);

    public static RecordList mListOne;
    public static RecordList mListTwo;
    public static RecordList mListThree;

    public static ArrayList<AppCompatActivity> mActivityList = new ArrayList<>();
    private static AppCompatActivity mCurResumeActivity; //当前Resume的Activity;

    private AcceptOrRejectDialog acceptOrRejectDialog;
    private WaitingForDialog waitingForNewGameDialog;
    private LeaveForDialog leaveForCurGameDialog;   //离开当前游戏
    private LeaveForDialog leaveForMultipleGameDialog;   //离开多人游戏

    private NewRecordDialog newRecordDialog;

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

    /**
     * 显示新纪录的dialog
     */
    public void showNewRecordDialog(final Record record) {
        if (newRecordDialog == null) {
            newRecordDialog = new NewRecordDialog(this,R.style.Custom_Dialog_Style,record.getGameTime()) {
                @Override
                public void onLeftClick() {
                    setInputNameText("");
                    dismiss();
                }

                @Override
                public void onRightClick() {
                    record.setGamePlayer(newRecordDialog.getInputNameText());
                    startInsert(record);
                    setInputNameText("");
                    dismiss();
                }
            };
        }
        newRecordDialog.show();
        newRecordDialog.setTimeText(record.getGameTime());
    }

    /**
     * 查询数据库
     */
    public void startQuery() {
        RecordDao recordDao = new RecordDao(this);
        Cursor cursor1 = recordDao.queryRecord(Constant.TYPE_1);
        Cursor cursor2 = recordDao.queryRecord(Constant.TYPE_2);
        Cursor cursor3 = recordDao.queryRecord(Constant.TYPE_3);
        parseCursor(cursor1,mTypeOneList);
        parseCursor(cursor2,mTypeTwoList);
        parseCursor(cursor3,mTypeThreeList);
        mListOne = new RecordList(mTypeOneList);
        mListTwo = new RecordList(mTypeTwoList);
        mListThree = new RecordList(mTypeThreeList);
        Log.i(TAG,"mListOne:");
        mListOne.printAllNode();
        Log.i(TAG,"mListTwo:");
        mListTwo.printAllNode();
        Log.i(TAG,"mListThree:");
        mListThree.printAllNode();
    }

    public void parseCursor(Cursor cursor,ArrayList<Record> arrayList) {
//        while (cursor.moveToNext()) {
//            int gameType = cursor.getInt(cursor.getColumnIndex("game_type"));
//                int gameTime = cursor.getInt(cursor.getColumnIndex("game_time"));
//                String gamePlayer = cursor.getString(cursor.getColumnIndex("game_player"));
//                String gameData = cursor.getString(cursor.getColumnIndex("game_data"));
//                Record record = new Record();
//                record.setGameType(gameType);
//                record.setGameTime(gameTime);
//                record.setGamePlayer(gamePlayer);
//                record.setGameData(gameData);
//                arrayList.add(record);
//                Log.i(TAG,"parseCursor:     GameType:"+gameType+"  gameTime:"+gameTime+" game_player:"+gamePlayer+" game_data:"+gameData);
//        }
        if (cursor.moveToFirst()) {
            do {
                int gameType = cursor.getInt(cursor.getColumnIndex("game_type"));
                int gameTime = cursor.getInt(cursor.getColumnIndex("game_time"));
                String gamePlayer = cursor.getString(cursor.getColumnIndex("game_player"));
                String gameData = cursor.getString(cursor.getColumnIndex("game_data"));
                Record record = new Record();
                record.setGameType(gameType);
                record.setGameTime(gameTime);
                record.setGamePlayer(gamePlayer);
                record.setGameData(gameData);
                arrayList.add(record);
                Log.i(TAG,"parseCursor:     GameType:"+gameType+"  gameTime:"+gameTime+" game_player:"+gamePlayer+" game_data:"+gameData);
            } while (cursor.moveToNext());
        }
    }

    /**
     * 插入数据到数据库中去
     * @param record
     */
    public void startInsert(Record record) {
//        ArrayList<Record> recordList = null;
//        switch (record.getGameType()) {
//            case Constant.TYPE_1:
//                recordList = mTypeOneList;
//                break;
//            case Constant.TYPE_2:
//                recordList = mTypeTwoList;
//                break;
//            case Constant.TYPE_3:
//                recordList = mTypeThreeList;
//                break;
//            case Constant.TYPE_4:
//                break;
//        }
//        if (recordList.size() == 0) {
//            recordList.add(record);
//        } else {
//            for (int i=0; i<recordList.size(); i++) {
//                if (recordList.get(i).getGameTime() > record.getGameTime()) {  //如果找到大于我们当前的时间
//
//                }
//            }
//        }
        RecordDao recordDao = new RecordDao(this);
        recordDao.insertRecord(record);

        switch (record.getGameType()) {
            case Constant.TYPE_1:
                mListOne.insert(record);
                break;
            case Constant.TYPE_2:
                mListTwo.insert(record);
                break;
            case Constant.TYPE_3:
                mListThree.insert(record);
                break;
        }
    }

    /**
     * 判断是不是新纪录
     * @param time 完成时间
     * @param type 游戏类型
     * @return
     */
    boolean isNewRecord(int time, int type) {
        ArrayList<Record> recordList = null;
        switch (type) {
            case Constant.TYPE_1:
                recordList = mTypeOneList;
                break;
            case Constant.TYPE_2:
                recordList = mTypeTwoList;
                break;
            case Constant.TYPE_3:
                recordList = mTypeThreeList;
                break;
            case Constant.TYPE_4:   //自定义游戏不做记录
                return false;
        }
        if (recordList.size() < 5)
            return true;
        else if (time < recordList.get(4).getGameTime())
            return true;
        return false;
    }

    void checkSize() {
        Log.i("tag",mTypeOneList.size()+"");
    }
}