package com.ccs.gph.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Environment;

import com.ccs.gph.gps.AppGps;
import com.ccs.gph.gps.AppLocationManager;
import com.ccs.gph.gps.LocationData;
import com.ccs.gph.mylocations.MyLocationData;

import java.util.ArrayList;

/**
 * Created by BusyWeb on 4/22/2017.
 */

public class AppShared {


    public static Activity gActivity;
    public static Context gContext;
    public static Resources gResources;

    public static String RootFolder = Environment.getExternalStorageDirectory().toString() + "/" + "gph/";
    public static final String RootFolderName = "gph";
    public static final String MyLocationsFolderName = "mylocations";
    public static final String MapImagesFolderName = "mapimages";

    public static AppLocationManager gLocationManager = null;
    public static LocationData gLocationData = null;
    public static AppGps gAppGps = null;
    public static String gGpsFilePath = "";
    public static final String DATA_FORMAT_CSV = "csv";
    public static final String DATA_FORMAT_JSON = "json";


    public static final String PREF_ADDRESS_KEY = "pref_address_key";
    public static final String PREF_ADDRESS_LATITUDE_KEY = "pref_address_latitude_key";
    public static final String PREF_ADDRESS_LONGITUDE_KEY = "pref_address_longitude_key";
    public static final String PREF_MOVEMENT_MAGNITUDE_KEY = "pref_movement_magnitude_key";

    public static String PrefAddress = "";
    public static double PrefAddressLatitude = 0d;
    public static double PrefAddressLongitude = 0d;
    public static String PrefMovementMagnitude = "min";

    public static final String PREF_RANDOM_MOVEMENT_KEY = "pref_random_movement";
    public static final String PREF_LOCATION_CHANGE_INTERVAL = "pref_location_change_interval";
    public static final String PREF_LOCATION_CHANGE_DISTANCE = "pref_location_change_distance";
    public static final String PREF_MAP_APP_CLOSE = "pref_map_app_close";
    public static final String PREF_MAP_SHOW_MOVEMENT = "pref_map_show_movement";
    public static final String PREF_SHOW_NOTIFICATION = "pref_show_notification";

    public static boolean PrefRandomMovement = true;
    public static long PrefLocationChangeInterval = 1000;
    public static double PrefLocationChangeDistance = 0.0001;
    public static boolean PrefMapAppClose = true;
    public static boolean PrefMapShowMovement = true;
    public static boolean PrefShowNotification = true;

    public static boolean LocationMockStarted = false;

    public static ArrayList<MyLocationData> MyLocations = new ArrayList<MyLocationData>();
}
