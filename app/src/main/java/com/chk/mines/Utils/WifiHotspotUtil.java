package com.chk.mines.Utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by chk on 18-2-6.
 * 创建wifi热点的工具
 */

public class WifiHotspotUtil {

    Context mContext;
    WifiManager mWifiManager;

    public WifiHotspotUtil(Context context) {
        mContext = context;
        mWifiManager = (WifiManager)mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public boolean setWifiApEnabled() {
        boolean result  = false;
        if (mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(false);
        try {
            WifiConfiguration apConfig = new WifiConfiguration();
            apConfig.SSID = "Mines";
            apConfig.preSharedKey = "";
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled",WifiConfiguration.class, boolean.class);
            result = (Boolean) method.invoke(mWifiManager,apConfig,true);
            if (!result) {
                Toast.makeText(mContext,"热点创建失败",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(mContext,"热点创建失败by catch",Toast.LENGTH_SHORT).show();
            Log.i("Mines",e.toString());
        }
        return result;
    }

}
