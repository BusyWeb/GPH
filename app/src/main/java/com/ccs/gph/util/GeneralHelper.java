package com.ccs.gph.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;

import com.ccs.gph.gps.AppGps;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by BusyWeb on 4/22/2017.
 */

public class GeneralHelper {

    public static void LoadPreferences(Context context) {
        try {
//            public static final String PREF_ADDRESS_KEY = "pref_address_key";
//            public static final String PREF_ADDRESS_LATITUDE_KEY = "pref_address_latitude_key";
//            public static final String PREF_ADDRESS_LONGITUDE_KEY = "pref_address_longitude_key";
//            public static final String PREF_MOVEMENT_MAGNITUDE_KEY = "pref_movement_magnitude_key";
//
//            public static String PrefAddress = "";
//            public static double PrefAddressLatitude = 0d;
//            public static double PrefAddressLongitude = 0d;
//            public static String PrefMovementMagnitude = "min";

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            AppShared.PrefAddress = prefs.getString(AppShared.PREF_ADDRESS_KEY, "");
            AppShared.PrefAddressLatitude = Double.parseDouble(prefs.getString(AppShared.PREF_ADDRESS_LATITUDE_KEY, "0"));
            AppShared.PrefAddressLongitude = Double.parseDouble(prefs.getString(AppShared.PREF_ADDRESS_LONGITUDE_KEY, "0"));
            AppShared.PrefMovementMagnitude = prefs.getString(AppShared.PREF_MOVEMENT_MAGNITUDE_KEY, "default");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void SavePreference(Context context, String key, String value) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(key, value);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Boolean IsGPSEnabled(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
        if (!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER)) {
            return false;
        } else {
            return true;
        }
    }
    public static Boolean IsGPSAvailable(Context context){
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION);
    }

    public static boolean IsNullOrEmpty(Object object) {
        if (object == null) {
            return true;
        }
        if (object instanceof String) {
            String val = (String)object;
            if (val == null || val.equalsIgnoreCase("") || val.length() < 1) {
                return true;
            } else {
                return false;
            }
        }
        if (object instanceof Integer) {
            Integer valInt = (Integer)object;
            if (valInt == null || valInt < 0) {
                return true;
            } else {
                return false;
            }
        }
        if (object instanceof Double) {
            Double valDouble = (Double)object;
            if (valDouble == null) {
                return true;
            } else {
                return false;
            }
        }
        if (object instanceof Long) {
            Long valLong = (Long)object;
            if (valLong == null) {
                return true;
            } else {
                return false;
            }
        }
        if (object instanceof java.util.Date) {
            Date valDate = (Date)object;
            if (valDate == null || valDate.toString().length() < 1) {
                return true;
            } else {
                return false;
            }
        }
        if (object instanceof Boolean) {
            Boolean valBool = (Boolean)object;
            if (valBool == null) {
                return true;
            } else {
                return valBool;
            }
        }
        return true;
    }

    public static AppGps LoadSampleGpsData(Context context) {
        AppGps appGps = new AppGps();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("gpsdata/sample.gps.txt");

            if (appGps.GpsEntries == null) {
                appGps.GpsEntries = new ArrayList<AppGps.Gps>();
            }

            appGps.ClearGpsEntries();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line != null && line.length() > 0) {
                    //appGps.GpsEntries.add(appGps.GetGpsInstance(line));
                    appGps.AddGpsEntry(line);
                }
            }

            if (reader != null) {
                reader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }

        } catch (Exception e) {
        }
        return appGps;
    }

    public static void SetLocationDataFromGpsData(Location location, AppGps.Gps gpsData) {
        try {

//            public Integer id = 0;
//            public Long time = 0L;
//            public Long timegps = 0L;
//            public Double latitude = 0.0;
//            public Double longitude = 0.0;
//            public Double altitude = 0.0;
//            public Float speed = 0f;
//            public Float accuracy = 0f;
//            public Float bearing = 0f;
//            public Float declination = 0f;
//
//            // OBD2 Information
//            public Integer obdrpm = 0;
//            public Integer obdtemperature = 0;

            location.setTime(gpsData.timegps);
            location.setLatitude(gpsData.latitude);
            location.setLongitude(gpsData.longitude);
            location.setAltitude(gpsData.altitude);
            location.setSpeed(gpsData.speed);
            location.setAccuracy(gpsData.accuracy);
            location.setBearing(gpsData.bearing);
        } catch (Exception e) {
        }
    }
}
