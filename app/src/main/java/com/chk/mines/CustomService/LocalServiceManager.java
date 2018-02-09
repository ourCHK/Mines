package com.chk.mines.CustomService;

import android.app.Service;

import java.util.ArrayList;

/**
 * Created by chk on 18-2-9.
 * 本地Service的管理器
 */

public class LocalServiceManager {

    public static ArrayList<Service> allServices = new ArrayList<>();

    public LocalServiceManager() {
    }

    public static void addService(Service service) {
        if (service != null)
            allServices.add(service);
    }

    public static void unbindAllServices() {
        for (Service service: allServices) {
            if (service != null) {
//                service.unbindService(service.);
            }
        }
    }

}
