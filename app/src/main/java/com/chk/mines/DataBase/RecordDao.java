package com.chk.mines.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.chk.mines.Beans.Record;

/**
 * Created by chk on 18-3-23.
 */

public class RecordDao {
    final static String TAG = RecordDao.class.getSimpleName();
    Context mContext;
    RecordDBHelper mDBHelper;
    SQLiteDatabase db;

    public RecordDao(Context context) {
        this.mContext = context;
        mDBHelper = new RecordDBHelper(this.mContext);

    }

    public void insertRecord(Record record) {
        db = mDBHelper.getWritableDatabase();
//        db.beginTransaction();
        db.execSQL("insert into "+mDBHelper.TABLE_NAME+" values(?,?,?,?)",new Object[]{record.getGameType(),
                record.getGameTime(),record.getGamePlayer(),record.getGameData()});
        db.close();
//        db.endTransaction();
        Log.i(TAG,"Insert Record:Name:"+record.getGamePlayer()+" Time:"+record.getGameTime()+" Type:"+record.getGameType());
    }

    public void deleteRecord() {
    }

    public Cursor queryRecord(int game_type) {
        Log.i(TAG,"query Record");
        db = mDBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from "+RecordDBHelper.TABLE_NAME+" where game_type = "+game_type+" limit 5",null);
//        Cursor cursor = db.rawQuery("select * from records",null);
//        while (cursor.moveToNext()) {
//            int gameType = cursor.getInt(cursor.getColumnIndex("game_type"));
//            int gameTime = cursor.getInt(cursor.getColumnIndex("game_time"));
//            String gamePlayer = cursor.getString(cursor.getColumnIndex("game_player"));
//            String gameData = cursor.getString(cursor.getColumnIndex("game_data"));
//            Record record = new Record();
//            record.setGameType(gameType);
//            record.setGameTime(gameTime);
//            record.setGamePlayer(gamePlayer);
//            record.setGameData(gameData);
//            Log.i(TAG,"parseCursor:     GameType:"+gameType+"  gameTime:"+gameTime+" game_player:"+gamePlayer+" game_data:"+gameData);
//        }
        return cursor;
    }

    public void updateRecord() {

    }


}