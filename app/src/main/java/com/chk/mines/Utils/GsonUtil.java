package com.chk.mines.Utils;

import com.chk.mines.Beans.CommunicateData;
import com.chk.mines.Beans.Mine;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

    public static CommunicateData stringToCommunicateData(String communicateDataJson) {
        CommunicateData communicateData = gson.fromJson(communicateDataJson,CommunicateData.class);
        return communicateData;
    }

    public static String minesToString(Mine[][] mines) {
        String minesString = gson.toJson(mines);
        return minesString;
    }

    public static Mine[][] stringToMines(String minesJson) {
        Mine[][] mines = gson.fromJson(minesJson,new TypeToken<Mine[][]>(){}.getType());
        return mines;
    }



}
