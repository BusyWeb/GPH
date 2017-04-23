package com.ccs.gph.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.ccs.gph.util.AppShared;
import com.ccs.gph.util.GeneralHelper;

import java.util.Iterator;

/**
 * Created by BusyWeb on 1/14/2017.
 */

public class AppLocationManager {

    public interface OnNewLocationListener {
        void onNewLocation(Location newLocation);
    }

    public interface OnGpsAvailabilityChangedListener {
        void onGpsAvailabilityChanged(boolean gpsAvailable);
    }

    public interface OnGpsFixedListener {
        void onGpsFixedListener(boolean fixed);
    }

    private OnNewLocationListener mNewLocationListener;
    private OnGpsAvailabilityChangedListener mGpsAvailabilityChangedListener;
    private OnGpsFixedListener mGpsFixedListener;

    private LocationData mLocationData = null;

    private LocationManager mLocationManager;
    private LocationListener mLocationListenerCoarse;
    private LocationListener mLocationListenerFine;
    private Criteria mCriteriaFine;
    private Criteria mCriteriaCoarse;
    private GpsStatus.Listener mGpsStatusListener;

    public AppLocationManager(LocationData locationData) {
        mLocationData = locationData;
        RegisterLocationListeners();
    }

    public static void StartLocationManager(
            OnGpsAvailabilityChangedListener gpsAvailabilityChangedListener,
            OnGpsFixedListener gpsFixedListener,
            OnNewLocationListener newLocationListener) {

        boolean gpsEnabled = GeneralHelper.IsGPSEnabled(AppShared.gContext);

        if (gpsEnabled) {

            if (AppShared.gLocationManager != null) {
                AppShared.gLocationManager.Close();
            }

            AppShared.gLocationData = new LocationData();

            AppShared.gLocationManager = new AppLocationManager(AppShared.gLocationData);
            AppShared.gLocationManager.SetGpsAvailabilityChangedListener(gpsAvailabilityChangedListener);
            AppShared.gLocationManager.SetGpsFixedListener(gpsFixedListener);
            AppShared.gLocationManager.SetNewLocationListener(newLocationListener);

        } else {
            //UtilGeneralHelper.ShowGpsDisabledPopup();
        }
    }

    public LocationData GetLocationData() {
        return mLocationData;
    }

    public void SetNewLocationListener(OnNewLocationListener l) {
        mNewLocationListener = l;
    }

    public void SetGpsAvailabilityChangedListener(OnGpsAvailabilityChangedListener l) {
        mGpsAvailabilityChangedListener = l;
    }

    public void SetGpsFixedListener(OnGpsFixedListener l) {
        mGpsFixedListener = l;
    }

    public void RegisterLocationListeners() {
        mLocationManager = (LocationManager) AppShared.gContext.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(AppShared.gContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AppShared.gContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mCriteriaFine = new Criteria();
        mCriteriaCoarse = new Criteria();
        mCriteriaFine.setAccuracy(Criteria.ACCURACY_FINE);
        mCriteriaCoarse.setAccuracy(Criteria.ACCURACY_COARSE);

        CreateLocationListeners();

        String provider = mLocationManager.getBestProvider(mCriteriaFine, true);
        if (provider != null) {
            mLocationManager.requestLocationUpdates(provider, mLocationData.UpdateInterval, 0, mLocationListenerFine);
        }

        provider = mLocationManager.getBestProvider(mCriteriaCoarse, true);
        if (provider != null) {
            mLocationManager.requestLocationUpdates(provider, mLocationData.UpdateInterval, 0, mLocationListenerCoarse);
        }

        try {
            mLocationManager.addGpsStatusListener(mGpsStatusListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CreateLocationListeners() {

        mLocationListenerCoarse = new LocationListener() {
            public void onLocationChanged(Location location) {
                try {
                    if (!mLocationData.FineLocAvailable) {
                        if (location.hasAccuracy()) {
                            mLocationData.NewLocation = location;
                            if (mNewLocationListener != null) {
                                mNewLocationListener.onNewLocation(mLocationData.NewLocation);
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }

            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
                // locationManager.removeUpdates(this);
            }

            public void onProviderEnabled(String proiver) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
                switch (status) {
                    case LocationProvider.OUT_OF_SERVICE:
                        mLocationData.CoarseLocAvailable = false;
                        if (!mLocationData.FineLocAvailable) {
                            if (mGpsAvailabilityChangedListener != null) {
                                mGpsAvailabilityChangedListener.onGpsAvailabilityChanged(false);
                            }
                        }
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        mLocationData.CoarseLocAvailable = false;
                        if (!mLocationData.FineLocAvailable) {
                            if (mGpsAvailabilityChangedListener != null) {
                                mGpsAvailabilityChangedListener.onGpsAvailabilityChanged(false);
                            }
                        }
                        break;
                    case LocationProvider.AVAILABLE:
                        mLocationData.CoarseLocAvailable = true;
                        if (mGpsAvailabilityChangedListener != null) {
                            mGpsAvailabilityChangedListener.onGpsAvailabilityChanged(true);
                        }
                        break;
                }
                if (!extras.isEmpty()) {
                    // Log.i("DBG","Coarse STATUS CHANGED Extra Count:" +
                    // Integer.toString(extras.size()));
                }
            }
        };

        mLocationListenerFine = new LocationListener() {
            public void onLocationChanged(Location location) {
                try {
                    String provider = location.getProvider();
                    if (provider.equalsIgnoreCase("network")) {
                        return;
                    }
                    Long timeDiff = null;
                    if (mLocationData.LastLocation != null) {
                        timeDiff = Math.abs(location.getTime() - mLocationData.LastLocation.getTime());
                        //distDiff = location.distanceTo(lastLocation);
                        if (timeDiff < 400) {
                            return;
                        }
                    }

                    mLocationData.NewLocation = location;

                    if (mNewLocationListener != null) {
                        mNewLocationListener.onNewLocation(mLocationData.NewLocation);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
                // locationManager.removeUpdates(this);
            }

            public void onProviderEnabled(String proiver) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
                switch (status) {
                    case LocationProvider.OUT_OF_SERVICE:
                        mLocationData.FineLocAvailable = false;
                        if (!mLocationData.CoarseLocAvailable) {
                            if (mGpsAvailabilityChangedListener != null) {
                                mGpsAvailabilityChangedListener.onGpsAvailabilityChanged(false);
                            }
                        }
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        mLocationData.FineLocAvailable = true;
                        if (!mLocationData.CoarseLocAvailable) {
                            if (mGpsAvailabilityChangedListener != null) {
                                mGpsAvailabilityChangedListener.onGpsAvailabilityChanged(false);
                            }
                        }
                        break;
                    case LocationProvider.AVAILABLE:
                        mLocationData.FineLocAvailable = true;
                        if (mGpsAvailabilityChangedListener != null) {
                            mGpsAvailabilityChangedListener.onGpsAvailabilityChanged(true);
                        }
                        break;
                }
                if (!extras.isEmpty()) {
                    // Log.i("DBG","Coarse STATUS CHANGED Extra Count:" +
                    // Integer.toString(extras.size()));
                }
            }
        };

        mGpsStatusListener = new GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {
                if (mLocationManager == null) return;
                // GpsStatus.GPS_EVENT_STARTED:
                // GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                // GpsStatus.GPS_EVENT_FIRST_FIX:
                // GpsStatus.GPS_EVENT_STOPPED:
                if (event == GpsStatus.GPS_EVENT_STOPPED || event == GpsStatus.GPS_EVENT_STARTED) {
                    mLocationData.GpsCount = 0;
                    mLocationData.GpsFixed = false;

                    if (mGpsFixedListener != null) {
                        mGpsFixedListener.onGpsFixedListener(mLocationData.GpsFixed);
                    }
                }
                if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                    mLocationData.GpsFixed = true;
                    if (mGpsFixedListener != null) {
                        mGpsFixedListener.onGpsFixedListener(mLocationData.GpsFixed);
                    }
                }
                if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {

                    int maxSats = 0;
                    int maxFix = 0; // maximal number of sats constituting a
                    // fix. The claim for the SGS is that this
                    // is always =< 8
                    int minSnr = 100; // minimal Snr of a sat contained in a
                    // fix. The claim for the SGS is that
                    // this is always > 20 (Lets start set
                    // with an unreal high value)
                    int iGpsStatus = -1;
                    int iSats;
                    int fix;
                    int snr;

                    mLocationData.GpsCount = maxFix;

                    if (ActivityCompat.checkSelfPermission(AppShared.gContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    GpsStatus xGpsStatus = mLocationManager.getGpsStatus(null);

                    Iterable<GpsSatellite> iSatellites = xGpsStatus
                            .getSatellites();
                    Iterator<GpsSatellite> it = iSatellites.iterator();
                    iSats = 0; // Satellite Count
                    fix = 0; // Count satellites used in fix
                    StringBuilder s = new StringBuilder();
                    while (it.hasNext()) {
                        iSats++;
                        GpsSatellite oSat = (GpsSatellite) it.next();
                        s.append(oSat.getPrn());
                        s.append(": ");
                        snr = (int) oSat.getSnr();
                        s.append(snr);
                        s.append(" Snr");
                        if (oSat.usedInFix()) {
                            s.append(" (*) ");

                            fix++;
                            // if snr of this locked sat < minSnr then update
                            // minSnr
                            if (snr < minSnr) {
                                minSnr = snr;
                                // minSignalNoiseRatio.setText("Min Snr: " +
                                // minSnr);
                            }
                            // Just testing for ephemeris and almanac data. On
                            // the Galaxy S the following GpsSatelite methods
                            // always return "false". To do: formatting the
                            // output..
                        }

                        if (oSat.hasEphemeris()) {
                            s.append(" Eph ");
                        }
                        if (oSat.hasAlmanac()) {
                            s.append(" Alm ");
                        }
                        // s.append("\n");

                        // Log.i("DBG","Satellite(" + iSats + ")" +
                        // s.toString()) ;
                        // Log.i("DBG","LocationActivity - onGpsStatusChange: Satellites: "
                        // + oSat.getSnr() ) ;

                    }
                    // gpsstatus.setText("Satellites: " + iSats);
                    // gpsfix.setText("Locked: " + fix);
                    // Log.v("DBG","LocationActivity - onGpsStatusChange: Satellites: "
                    // + iSats ) ;
                    if (s.length() > 0) {
                        // gps_output.setText(s.toString());
                    } else {
                        // gps_output.setText("Waiting...");
                    }
                    if (iSats > maxSats) {
                        maxSats = iSats;
                        // maxSatellites.setText("Max Sats: " + maxSats);
                    }
                    if (fix > maxFix) {
                        maxFix = fix;
                        // maxLocked.setText("Max Locked: " + maxFix);
                    }

                    mLocationData.GpsCount = maxFix;
                    if (mLocationData.GpsCount > 2) {
                        mLocationData.GpsFixed = true;
                    } else {
                        mLocationData.GpsFixed = false;
                    }
                    if (mGpsFixedListener != null) {
                        mGpsFixedListener.onGpsFixedListener(mLocationData.GpsFixed);
                    }
                }
            }
        };
    }

    public void UnRegisterLocationListeners() {
        try {
            mLocationData.GpsFixed = false;
            mLocationData.GpsCount = 0;
            if (mLocationManager == null) return;

            if (mGpsStatusListener != null)
                mLocationManager.removeGpsStatusListener(mGpsStatusListener);
            if (mLocationListenerCoarse != null)
                mLocationManager.removeUpdates(mLocationListenerCoarse);
            if (mLocationListenerFine != null)
                mLocationManager.removeUpdates(mLocationListenerFine);

            mLocationListenerCoarse = null;
            mLocationListenerFine = null;
            mGpsStatusListener = null;

            mLocationManager = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Close() {
        try {
            UnRegisterLocationListeners();
        } catch (Exception e) {
        }
    }

    public void RequestLastKnownLocation(String locationProvider) {
        if (ActivityCompat.checkSelfPermission(AppShared.gContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AppShared.gContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationData.NewLocation = mLocationManager.getLastKnownLocation(locationProvider);
        if (mLocationData.NewLocation != null) {
            if (mNewLocationListener != null) {
                mNewLocationListener.onNewLocation(mLocationData.NewLocation);
            }
        }
    }

    public Location RequestLastKnownLocation() {
        if (!IsRunning()) {
            return null;
        }
        if (ActivityCompat.checkSelfPermission(AppShared.gContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AppShared.gContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        mLocationData.NewLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (mLocationData.NewLocation == null) {
            mLocationData.NewLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return mLocationData.NewLocation;
    }

    public boolean IsRunning() {
        return mLocationManager != null;
    }


}
