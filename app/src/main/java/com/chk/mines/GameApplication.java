package com.chk.mines;

import android.app.Application;
import android.content.Context;

/**
 * Created by chk on 18-2-6.
 */

public class GameApplication extends Application{

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getContext();
    }



    public static Context getContext() {
        return mContext;
    }

}
