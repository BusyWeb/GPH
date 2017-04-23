package com.ccs.gph.singleton;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.content.PermissionChecker;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by okmac on 4/15/17.
 */

public class ApplicationSingletonGPS extends Application implements LocationListener {
    String TAG = "GPH Info.";
    LocationManager lm;
    Location loc;
    LocationListener locationListenerNET, locationListenerGPS;
    Double longitudeGPS, latitudeGPS, longitudeNET, latitudeNET;
    boolean gpsComSignal, netComSignal;

    private static ApplicationSingletonGPS sInstance;
    //private SessionHandler sessionHandler;
    //


    @Override
    public void onCreate() {
        super.onCreate();
        //sInstance = this;
        //sInstance.initializeInstance();


        locationListenerGPS = new LocationListener() {
            // @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO locationListenerGPS onStatusChanged
                Log.d(TAG, "Provedor trocado");
            }

            // @Override
            public void onProviderEnabled(String provider) {
                Log.w(TAG, "PROVEDOR " + provider + " HABILITADO!");
            }

            // @Override
            public void onProviderDisabled(String provider) {
                Log.w(TAG, "PROVEDOR " + provider + " DESABILITADO!");
            }

            // @Override
            public void onLocationChanged(Location location) {

                longitudeGPS = location.getLongitude();
                latitudeGPS = location.getLatitude();
                Log.d(TAG,"LocationChangedGPS LAT: "+latitudeGPS+" longi: "+longitudeGPS);
                gpsComSignal = true;
            }
        };

        locationListenerNET = new LocationListener() {

            // @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO locationListenerNET onStatusChanged
                Log.d("Contele", "Provedor foi mudado");
            }

            // @Override
            public void onProviderEnabled(String provider) {
                Log.i(TAG, "PROVEDOR " + provider + " HABILITADO!");
            }

            // @Override
            public void onProviderDisabled(String provider) {
                Log.i(TAG, "PROVEDOR " + provider + " DESABILITADO!");
            }

            @Override
            public void onLocationChanged(Location location) {
                longitudeNET = location.getLongitude();
                latitudeNET = location.getLatitude();
                Log.d(TAG,"LocationChangedNET LAT: "+latitudeNET+" longi: "+longitudeNET);
                netComSignal = true;
            }
        };



    }

    public static ApplicationSingletonGPS getInstance() {
        return sInstance;
    }


    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO locationListenerGPS onStatusChanged
        Log.d(TAG, "Provedor trocado");
    }

    // @Override
    public void onProviderEnabled(String provider) {
        Log.w(TAG, "PROVEDOR " + provider + " HABILITADO!");
    }

    // @Override
    public void onProviderDisabled(String provider) {
        Log.w(TAG, "PROVEDOR " + provider + " DESABILITADO!");
    }

    // @Override
    public void onLocationChanged(Location location) {

        longitudeGPS = location.getLongitude();
        latitudeGPS = location.getLatitude();
        Log.d(TAG,"LocationChangedGPS LAT: "+latitudeGPS+" longi: "+longitudeGPS);
        gpsComSignal = true;
    }




    public static boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        if (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else
            return true;
    }

    public static boolean areThereMockPermissionApps(Context context) {
        int count = 0;

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i]
                                .equals("android.permission.ACCESS_MOCK_LOCATION")
                                && !applicationInfo.packageName.equals(context.getPackageName())) {
                            count++;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Got exception ", e.getMessage());
            }
        }

        if (count > 0)
            return true;
        return false;
    }


    public void mockLocation(){
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy( Criteria.NO_REQUIREMENT );
        String mocLocationProvider = lm.getBestProvider( criteria, false );

        if ( mocLocationProvider == null ) {
            Toast.makeText(getApplicationContext(), "No location provider found!", Toast.LENGTH_SHORT).show();
            //return;

            criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
            String mocLocationProvider2 = lm.getBestProvider(criteria, false);
            if ( mocLocationProvider2 == "") {
                Toast.makeText(getApplicationContext(), "2nd No loc prov found!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Provider Found: " + mocLocationProvider2, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Provider Found: " + mocLocationProvider, Toast.LENGTH_SHORT).show();

        }
        Toast.makeText(getApplicationContext(), "Provider Found: " + mocLocationProvider, Toast.LENGTH_SHORT).show();

        //lm.addTestProvider(mocLocationProvider, false, false,
        //        false, false, true, true, true, 0, 5);
        //lm.setTestProviderEnabled(mocLocationProvider, true);

        loc = new Location(mocLocationProvider);

        Location mockLocation = new Location(mocLocationProvider); // a string
        mockLocation.setLatitude(-26.902038);  // double
        mockLocation.setLongitude(-48.671337);
        mockLocation.setAltitude(loc.getAltitude());
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setElapsedRealtimeNanos(1000);
        mockLocation.setAccuracy(Criteria.ACCURACY_HIGH);
        //lm.setTestProviderLocation( mocLocationProvider, mockLocation);
        Toast.makeText(getApplicationContext(), "Working " + mocLocationProvider, Toast.LENGTH_SHORT).show();


        /*
                // every time you mock location, you should use these code
        int value = setMockLocationSettings();//toggle ALLOW_MOCK_LOCATION on
        try {
            lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, fake_location);
        } catch (SecurityException e) {
            e.printStackTrace();
        } finally {
            restoreMockLocationSettings(value);//toggle ALLOW_MOCK_LOCATION off
        }



        */



    }

    public void mockLocationRemoval() {
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            Log.d(TAG,"Removing Test providers");
            lm.removeTestProvider(LocationManager.GPS_PROVIDER);

            if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

                //ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  }
                //        ); //ApplicationSingletonGPS.MY_PERMISSION_ACCESS_COURSE_LOCATION );
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        } catch (IllegalArgumentException error) {
            Log.d(TAG,"Got exception in removing test  provider");
        } catch (SecurityException e) {
            Log.e(TAG, "Ex: " + e.getMessage());
        }


    }

    public boolean isMockLocationCurrently() {
        boolean isMock = false;
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            isMock = loc.isFromMockProvider();
        } else {
            isMock = !Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
        }
        return isMock;
    }
    private int setMockLocationSettings() {
        int value = 1;
        try {
            value = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private void restoreMockLocationSettings(int restore_value) {
        try {
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION, restore_value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getLocation() {
        //TextView tv = (TextView) findViewById(R.id.gps_coord_view);
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return ("Latitude: " + loc.getLatitude() + ", Longitude: " + loc.getLongitude());
        } catch (SecurityException esec) {
            Log.e(TAG + " SecEx", esec.getMessage());
            return "error";
        }
    }
}
