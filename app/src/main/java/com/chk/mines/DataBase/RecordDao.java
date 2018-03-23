package com.chk.mines.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chk.mines.Beans.Record;

/**
 * Created by chk on 18-3-23.
 */

public class RecordDao {
    Context mContext;
    RecordDBHelper mDBHelper;
    SQLiteDatabase db;

    public RecordDao(Context montext) {
        mContext = montext;
        mDBHelper = new RecordDBHelper(mContext);
        db = mDBHelper.getWritableDatabase();

    }

    public void insertRecord(Record record) {
        db.beginTransaction();
        db.execSQL("insert into "+mDBHelper.TABLE_NAME+" values(?,?,?,?)",new Object[]{record.getGameType(),
                record.getGameTime(),record.getGamePlayer(),record.getGameData()});
        db.endTransaction();
    }

    public void deleteRecord() {
    }

    public Cursor queryRecord(int game_type) {
        Cursor cursor = db.rawQuery("select * from "+RecordDBHelper.TABLE_NAME+" where game_type = '?' limit 5",new String[]{game_type+""});
        return cursor;
    }

    public void updateRecord() {

    }


}
