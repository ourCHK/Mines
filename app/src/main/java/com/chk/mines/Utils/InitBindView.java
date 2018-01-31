package com.chk.mines.Utils;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by chk on 18-1-31.
 */

public class InitBindView {
    public static void init(Activity activity) {
        try {
            Class<Activity> clazz = (Class<Activity>) activity.getClass();
            Field[] fields = clazz.getFields();
            for (Field field:fields) {
                if (field.isAnnotationPresent(BindView.class)) {
                    BindView bindView = field.getAnnotation(BindView.class);
                    int id = bindView.value();
                    field.setAccessible(true);
                    field.set(activity,activity.findViewById(id));

//                    boolean click = bindView.click();
//                    field.setAccessible(true);
//                    field.set();
                }
            }
        } catch (Exception e) {
            Log.e("InitBindView","Annotation Failed");
        }
    }
}
