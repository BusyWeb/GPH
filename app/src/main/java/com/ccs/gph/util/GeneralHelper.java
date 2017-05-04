package com.ccs.gph.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.ccs.gph.R;
import com.ccs.gph.activity.MyLocationsActivity;
import com.ccs.gph.gps.AppGps;
import com.ccs.gph.mylocations.MyLocationData;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

//            public static final String PREF_RANDOM_MOVEMENT_KEY = "pref_random_movement";
//            public static final String PREF_LOCATION_CHANGE_INTERVAL = "pref_location_change_interval";
//            public static final String PREF_LOCATION_CHANGE_DISTANCE = "pref_location_change_distance";
//            public static final String PREF_MAP_APP_CLOSE = "pref_map_app_close";
//            public static final String PREF_MAP_SHOW_MOVEMENT = "pref_map_show_movement";
//            public static final String PREF_SHOW_NOTIFICATION = "pref_show_notification";
//
//            public static boolean PrefRandomMovement = true;
//            public static long PrefLocationChangeInterval = 1000;
//            public static double PrefLocationChangeDistance = 0.0001;
//            public static boolean PrefMapAppClose = true;
//            public static boolean PrefMapShowMovement = true;
//            public static boolean PrefShowNotification = true;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            AppShared.PrefAddress = prefs.getString(AppShared.PREF_ADDRESS_KEY, "");
            AppShared.PrefAddressLatitude = Double.parseDouble(prefs.getString(AppShared.PREF_ADDRESS_LATITUDE_KEY, "0"));
            AppShared.PrefAddressLongitude = Double.parseDouble(prefs.getString(AppShared.PREF_ADDRESS_LONGITUDE_KEY, "0"));
            AppShared.PrefMovementMagnitude = prefs.getString(AppShared.PREF_MOVEMENT_MAGNITUDE_KEY, "default");

            AppShared.PrefRandomMovement = prefs.getBoolean(AppShared.PREF_RANDOM_MOVEMENT_KEY, true);
            AppShared.PrefLocationChangeInterval = Long.parseLong(prefs.getString(AppShared.PREF_LOCATION_CHANGE_INTERVAL, "1000"));
            String distance = prefs.getString(AppShared.PREF_LOCATION_CHANGE_DISTANCE, "0001");
            AppShared.PrefLocationChangeDistance = Double.parseDouble("0." + distance);
            AppShared.PrefMapAppClose = prefs.getBoolean(AppShared.PREF_MAP_APP_CLOSE, true);
            AppShared.PrefMapShowMovement = prefs.getBoolean(AppShared.PREF_MAP_SHOW_MOVEMENT, true);
            AppShared.PrefShowNotification = prefs.getBoolean(AppShared.PREF_SHOW_NOTIFICATION, true);

            LoadMyLocations();
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

    public static boolean IsOnline(Context ctx){
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            //return cm.getActiveNetworkInfo().isConnectedOrConnecting();
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnected()){
                return true;
            } else {
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
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

    public static int GetDisplayPixel(Context context, int dp){
        // Use mGestureThreshold as a distance in pixels
        //return (int)(dp * (this.getResources().getDisplayMetrics().density / 160) * dp);
        return (int)(dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static BitmapFactory.Options GetNewBitmapOptions(int scale) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (scale > 0) options.inSampleSize = scale;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[32 * 1024];
        return options;
    }

    public static Bitmap GetMarkerBitmap() {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = GetNewBitmapOptions(-1);
            Bitmap bitmap1 = BitmapFactory.decodeResource(AppShared.gResources, R.mipmap.ic_my_location);
            bitmap = Bitmap.createScaledBitmap(
                    bitmap1,
                    GetDisplayPixel(AppShared.gContext, 40),
                    GetDisplayPixel(AppShared.gContext, 40),
                    false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static boolean CheckAndCreateAppFolders() {
        boolean retValue = false;
        try {
            String rootfolder = AppShared.RootFolder;
            retValue = CheckAndCreateSubFolder(rootfolder, AppShared.MyLocationsFolderName);
            retValue = CheckAndCreateSubFolder(rootfolder, AppShared.MapImagesFolderName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retValue;
    }

    public static boolean CheckAndCreateSubFolder(String rootFolder, String folderName) {
        boolean retValue = false;
        try {
            File appFolder = new File(rootFolder);
            if (appFolder.exists() == false){
                appFolder.mkdir();
            }

            File newFolder = new File(rootFolder + folderName + "/");
            //Log.i("DBG", newFolder.getAbsolutePath());

            if (!newFolder.exists()){
                retValue = newFolder.mkdir();
                retValue = true;
            } else {
                retValue = true;
            }
            //Log.i("DBG", "Folder created: " + String.valueOf(retValue));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return retValue;
    }

    public static boolean LoadMyLocations() {
        boolean success = false;
        try {
            AppShared.MyLocations.clear();

            File dataFolder = new File(AppShared.RootFolder + AppShared.MyLocationsFolderName);
            if (dataFolder.exists()) {
                File[] list = dataFolder.listFiles();

                for (File dataFile : list) {
                    MyLocationData data = new MyLocationData(dataFile);
                    AppShared.MyLocations.add(data);
                }
            }

            if (AppShared.MyLocations.size() > 0) {
                Collections.sort(AppShared.MyLocations, new Comparator<MyLocationData>() {
                    @Override
                    public int compare(MyLocationData o1, MyLocationData o2) {
                        return o1.Name.toLowerCase().compareTo(o2.Name.toLowerCase());
                    }
                });
            }

            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
        }
        return success;
    }

    public static MyLocationData FindLocationDataById(String id) {
        MyLocationData data = null;
        try {
            for(MyLocationData item : AppShared.MyLocations) {
                if (item.Id.equalsIgnoreCase(id)) {
                    data = item;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static boolean SaveMyLocation(MyLocationData data) {
        boolean success = false;
        try {
            if (data == null) {
                return true;
            }

            File file = new File(AppShared.RootFolder + AppShared.MyLocationsFolderName + "/" + data.Id + ".txt");
            if (file.exists()) {
                file.delete();
            }
            data.SaveToFile(file);

            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    public static boolean SaveMyLocations() {
        boolean success = false;
        try {
            if (AppShared.MyLocations == null || AppShared.MyLocations.size() < 1) {
                return true;
            }

            for (MyLocationData data : AppShared.MyLocations) {
//                File file = new File(AppShared.RootFolder + AppShared.MyLocationsFolderName + "/" + data.Id + ".txt");
//                if (file.exists()) {
//                    file.delete();
//                }
//                data.SaveToFile(file);
                boolean result = SaveMyLocation(data);
            }

            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    public static boolean IsMapImageExist(MyLocationData data) {
        boolean exist = false;
        try {
            if (data == null) {
                return false;
            }

            File file = new File(AppShared.RootFolder + AppShared.MapImagesFolderName + "/" + data.Id + ".png");

            if (file.exists()) {
                exist = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            exist = false;
        }
        return exist;
    }

    public static Bitmap LoadLocationMapImage(MyLocationData data) {
        Bitmap bitmap = null;
        try {
            String imagePath = AppShared.RootFolder + AppShared.MapImagesFolderName + "/" + data.Id + ".png";
            bitmap = BitmapFactory.decodeFile(imagePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void SaveDataImage(MyLocationData data, Bitmap bitmap) {
        try {
            String imagePath = AppShared.RootFolder + AppShared.MapImagesFolderName + "/" + data.Id + ".png";
            SaveBitmapToFile(bitmap, imagePath, Bitmap.CompressFormat.PNG, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean SaveBitmapToFile(Bitmap b, String filePath, Bitmap.CompressFormat format, Integer quality) {
        boolean retValue = false;
        try {
            //String filePath = Environment.getExternalStorageDirectory() + "/dashboardcam/view.jpg";
            FileOutputStream out = new FileOutputStream(filePath);
            b.compress(format, quality, out);
            retValue = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retValue;
    }
    public static void LoadAndSaveMapImage(final MyLocationData data, final MyLocationsActivity.LocationViewHolder holder) {
        try {
            String mapUrl = GenerateMapImageUrl(data);
            Log.i("DBG", mapUrl);

            ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(AppShared.gContext), new LruBitmapCache(AppShared.gContext));
            imageLoader.get(mapUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    try {
                        Bitmap bitmap = response.getBitmap();
                        if (bitmap != null) {
                            SaveDataImage(data, bitmap);
                            holder.MapImage.setImageBitmap(bitmap);
                            holder.Loading.setVisibility(View.GONE);
                            holder.MapImage.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    holder.MapImage.setImageResource(R.drawable.ic_action_map);
                    holder.Loading.setVisibility(View.GONE);
                    holder.MapImage.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String MapImageUrlPath = "https://maps.googleapis.com/maps/api/staticmap?size=640x400";
    public static String MapKey = "&key=AIzaSyB4Ukpw4lOWM41Mgtk_pnvR7U0mQq4xiSg";
    public static String MarkerStart = "&markers=color:blue%7Clabel:S%7C";  //{0},{1}
    public static String MapPath = "&path=color:0x0000ff%7Cweight:4";       //{0}
    public static String MapPathEncoded = "&path=color:0x0000ff%7Cweight:4%7Cenc:";     //{0}

    public static String GenerateMapImageUrl(MyLocationData data) {
        String url = "";
        try {
            StringBuilder builder = new StringBuilder();

            builder.append(MapImageUrlPath);

            //String allPathEncoded = "";
            //ArrayList<LatLng> latLngs = new ArrayList<LatLng>();

            String startLoc = String.format("%.6f,%.6f", data.Latitude, data.Longitude);
            builder.append(MarkerStart + startLoc);
            builder.append(MapKey);

            url = builder.toString();
        } catch (Exception e) {
        }
        return url;
    }


    public static String LoadAddress(LatLng latLng) {
        String address = "";
        try {
            Geocoder geoCoder = new Geocoder(AppShared.gContext, Locale.getDefault());
            List<Address> addresses;
            addresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 2);

            if (addresses == null) {
                address = "Address: not available.";
            } else {
                if (addresses.size() > 0) {
                    for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++)
                        address += addresses.get(0).getAddressLine(i) + " ";
                } else {
                    address += "Address: not available.";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }
}
