package com.ccs.gph.util;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;

import com.ccs.gph.gps.AppGps;
import com.ccs.gph.gps.AppLocationManager;
import com.ccs.gph.gps.LocationData;

/**
 * Created by BusyWeb on 4/22/2017.
 */

public class AppShared {


    public static Activity gActivity;
    public static Context gContext;

    public static AppLocationManager gLocationManager = null;
    public static LocationData gLocationData = null;
    public static AppGps gAppGps = null;
    public static String gGpsFilePath = "";
    public static final String DATA_FORMAT_CSV = "csv";
    public static final String DATA_FORMAT_JSON = "json";

}
