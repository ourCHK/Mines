package com.chk.mines.Utils;

import com.chk.mines.Beans.CommunicateData;
import com.google.gson.Gson;

/**
 * Created by chk on 18-2-10.
 */

public class GsonUtil {
    static Gson gson = new Gson();
    private GsonUtil() {
    }
    public static String communicateDataToString(CommunicateData communicateData) {
        String communicateDataString = gson.toJson(communicateData);
        return communicateDataString;
    }

    public static CommunicateData stringToCommunicateData(String communicateDataString) {
        CommunicateData communicateData = gson.fromJson(communicateDataString,CommunicateData.class);
        return communicateData;
    }
}
