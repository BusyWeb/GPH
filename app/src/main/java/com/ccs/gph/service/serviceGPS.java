package com.ccs.gph.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ccs.gph.R;
import com.ccs.gph.activity.GphActivity;
import com.ccs.gph.activity.MainActivity;
import com.ccs.gph.util.AppShared;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.Random;

/**
 * Created by okmac on 4/15/17.
 */

public class serviceGPS extends Service {

    IBinder mBinder = null; // interface for clients that bind
    boolean mAllowRebind = false; // indicates whether onRebind should be used

    private static Context mContext;
    private static Activity mActivity;
    private static Resources mResources;

    private static NotificationManager mNM = null;
    private static Notification mNotification;
    private int NOTIFICATION = 1111;
    private PowerManager mPM;
    private static PowerManager.WakeLock mWakeLock;

    public enum SensorDataMode {
        MarryGoAround,
        RollerCoast,
        Jumping,
        Running,
        Walking,
        Sleep,
        Stop,
        Start,
        Manual
    }
    private static int mSensorModeStep = 0;
    private static double mInterval = AppShared.PrefLocationChangeInterval;
    private static float mMovementAngle;
    private static float mMovementMagnitude;

    private static SensorDataMode mSensorDataMode = SensorDataMode.Sleep;
    public static void SetSensorDaraMode(SensorDataMode mode) {
        mSensorDataMode = mode;
    }
    public static void SetSensorModeManual(float angle, float pitch, float roll) {
        mSensorDataMode = SensorDataMode.Manual;
        mMovementAngle = angle;

        if (pitch > 20 || roll > 20) {
            mMovementMagnitude = 2;
        } else {
            mMovementMagnitude = 1;
        }
    }
    public static SensorDataMode GetSensorDataMode() { return mSensorDataMode; }

    private static LatLng mModeOrigin = null;
    public static void AdjustLocationOrigin(double amountLatitude, double amountLongitude) {
        mModeOrigin = new LatLng(mModeOrigin.latitude + amountLatitude, mModeOrigin.longitude + amountLongitude);
    }



    public static boolean ServiceStarted() {
        return locationMockStarted;
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        prepareStart();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the old onStart method that will be called on the pre-2.0
    // platform. On 2.0 or later we override onStartCommand() so this
    // method will not be called.
    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        prepareStop();
    }

    private void prepareStart() {
        try {
            mContext = AppShared.gContext;

            mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mPM = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (mWakeLock == null) {
                mWakeLock = mPM.newWakeLock(PowerManager.FULL_WAKE_LOCK, "serviceGPS");
            }
            mWakeLock.acquire();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareStop() {
        try {

            if (mNM != null) {
                mNM.cancel(NOTIFICATION);
            }
            stopMockService();

            if (mWakeLock != null) {
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleCommand(Intent intent) {
        try {
            int start = intent.getIntExtra("start", 0);
            if (start > 0) {
                mHasMockLocation = intent.getBooleanExtra("hasMockLocation", false);

                mChangeAmountEnabled = intent.getBooleanExtra("enableAmount", false);
                String lat = intent.getStringExtra("latitude");
                String lng = intent.getStringExtra("longitude");
                String amount = intent.getStringExtra("amount");

                if (mHasMockLocation) {
                    mLatitudeLast = (lat == null || lat.equalsIgnoreCase("")) ? 0d : Double.parseDouble(lat);
                    mLongitudeLast = (lng == null || lng.equalsIgnoreCase("")) ? 0d : Double.parseDouble(lng);
                } else {
                    mLatitudeLast = 0d;
                    mLongitudeLast = 0d;
                }

                if (mChangeAmountEnabled) {
                    mChangeAmount = (amount == null || amount.equalsIgnoreCase("")) ? 0d : Double.parseDouble(amount);
                } else {
                    mChangeAmount = 0d;
                }

                startMockService();
                return;
            }

            int stop = intent.getIntExtra("stop", 0);
            if (stop > 0) {
                prepareStop();

                stopMockService();
                stopSelf();
            }
        } catch (Exception e) {

        }
    }

    private void startMockService() {
        try {

            prepareLocationManager();

            locationMockStarted = true;
            mInterval = AppShared.PrefLocationChangeInterval;

            SetSensorDaraMode(SensorDataMode.Walking);
            mModeOrigin = new LatLng(mLatitudeLast, mLongitudeLast);

            updateMockLocation();

            if (mMockLocationRun != null) {
                MockLocationHandler.sendEmptyMessage(-1);
            }
            mMockLocationRun = new MockLocationRun();
            MockLocationHandler.postDelayed(mMockLocationRun, 100);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMockService() {
        try {
            stopLocationManager();

            locationMockStarted = false;

            if (mMockLocationRun != null) {
                MockLocationHandler.sendEmptyMessage(-1);
            }
            mModeOrigin = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareLocationManager() {
        try {
            if (mLocationManager == null) {
                mLocationManager = (LocationManager) AppShared.gActivity.getSystemService(Context.LOCATION_SERVICE);
            }

            mLocationManager.addTestProvider
                    (
                            LocationManager.GPS_PROVIDER,
                            false,
                            false,
                            false,
                            false,
                            false,
                            false,
                            false,
                            Criteria.POWER_LOW,
                            Criteria.ACCURACY_FINE
                    );
            mLocationManager.addTestProvider
                    (
                            LocationManager.NETWORK_PROVIDER,
                            false,
                            false,
                            false,
                            false,
                            false,
                            false,
                            false,
                            Criteria.POWER_LOW,
                            Criteria.ACCURACY_COARSE
                    );
//            mLocationManager.addTestProvider
//                    (
//                            LocationManager.PASSIVE_PROVIDER,
//                            false,
//                            false,
//                            false,
//                            false,
//                            false,
//                            false,
//                            false,
//                            android.location.Criteria.POWER_LOW,
//                            Criteria.ACCURACY_HIGH
//                    );


            mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            mLocationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
            //mLocationManager.setTestProviderEnabled(LocationManager.PASSIVE_PROVIDER, true);
            mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
            mLocationManager.setTestProviderStatus(LocationManager.NETWORK_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());

            mMockLocation = new Location(LocationManager.GPS_PROVIDER);
            mMockLocation2 = new Location(LocationManager.NETWORK_PROVIDER);

            mMockLocation.setLatitude(mLatitudeLast);
            mMockLocation.setLongitude(mLongitudeLast);
            mMockLocation.setAccuracy(1f);
            mMockLocation.setTime(System.currentTimeMillis());
            mMockLocation.setElapsedRealtimeNanos(System.nanoTime());

            mMockLocation2.setLatitude(mLatitudeLast);
            mMockLocation2.setLongitude(mLongitudeLast);
            mMockLocation2.setAccuracy(1f);
            mMockLocation2.setTime(System.currentTimeMillis());
            mMockLocation2.setElapsedRealtimeNanos(System.nanoTime());

            mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mMockLocation);
            mLocationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, mMockLocation2);

            //setSecureSetting();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopLocationManager() {
        try {
            if (mLocationManager == null) {
                return;
            }
            if (mLocationManager.getProvider(mProvider) != null) {
                mLocationManager.clearTestProviderLocation(mProvider);
                mLocationManager.clearTestProviderEnabled(mProvider);
                mLocationManager.removeTestProvider(mProvider);
            }
            if (mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
                mLocationManager.clearTestProviderLocation(LocationManager.NETWORK_PROVIDER);
                mLocationManager.clearTestProviderEnabled(LocationManager.NETWORK_PROVIDER);
                mLocationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
            }

            mLocationManager = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double mLatitude, mLongitude, mLatitudeLast, mLongitudeLast;
    private static boolean mHasMockLocation = false;
    private static Long mLastLocationCheckTime = 0L;
    private static boolean locationMockStarted = false;
    private static MockLocationRun mMockLocationRun = null;
    private final static String mProvider = LocationManager.GPS_PROVIDER;   //"gps";
    private static LocationManager mLocationManager = null;
    private static double mChangeAmount;
    private static boolean mChangeAmountEnabled = false;
    private static Location mMockLocation = null;
    private static Location mMockLocation2 = null;

    public static Location GetMockLocation() {
        return mMockLocation;
    }

    public interface IMockLocationEvent {
        public void MockLocationChanged(Location location);
    }

    private static IMockLocationEvent mMockLocationEvent;

    public static void SetMockLocatoinEvent(IMockLocationEvent mockLocationEvent) {
        mMockLocationEvent = mockLocationEvent;
    }

    public class MockLocationRun implements Runnable {

        @Override
        public void run() {
            mLastLocationCheckTime = new Date().getTime();

            MockLocationHandler.sendEmptyMessage(0);

            updateMockLocation();
        }
    }
    public final Handler MockLocationHandler = new Handler() {

        @Override
        public void handleMessage(Message message) {
            int what = message.what;

            if (what == -1) {
                // stop...
                if (mMockLocationRun != null) {
                    this.removeCallbacks(mMockLocationRun);
                }
                mMockLocationRun = null;
                return;
            } else {

                if (mMockLocationRun != null) {
                    this.removeCallbacks(mMockLocationRun);
                }
                if (!locationMockStarted) {
                    return;
                }


                int nextCheck = 0;
                nextCheck = (int)mInterval - (int) (new Date().getTime() - mLastLocationCheckTime);
                if (nextCheck < 0) {
                    nextCheck = 0;
                }

                mMockLocationRun = new serviceGPS.MockLocationRun();
                this.postDelayed(mMockLocationRun, nextCheck);
            }
        }
    };

    private void setSecureSetting() {
        try {
            int mock = Settings.Secure.getInt(mContext.getContentResolver(), "mock_location");
            if (true) {
                //Settings.Secure.putInt(mContext.getContentResolver(), "mock_location", 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setRandomLocationValue(Location location) {
        try {

            double random = 1.0d - (2.0d * Math.random());
            double sqrt = (Math.random() > 0.499d ? 1.0d : -1.0d) * Math.sqrt(1.0d - (random * random));

            // check manual position change amount
            if (mChangeAmountEnabled) {
                if (mLatitudeLast == 0) {
                    mLatitudeLast = (random * (1) + location.getLongitude());
                }
                if (mLongitudeLast == 0) {
                    mLongitudeLast = (sqrt * ((1) + location.getLatitude()));
                }
                Random rad = new Random();
                int direction = rad.nextInt(8);
                if (direction == 0) {
                    mLongitudeLast += mChangeAmount;
                } else if (direction == 1) {
                    mLatitudeLast += mChangeAmount;
                } else if (direction == 2) {
                    mLongitudeLast -= mChangeAmount;
                } else if (direction == 3) {
                    mLatitudeLast -= mChangeAmount;
                } else if (direction == 4) {
                    mLongitudeLast += mChangeAmount;
                    mLatitudeLast += mChangeAmount;
                } else if (direction == 5) {
                    mLongitudeLast -= mChangeAmount;
                    mLatitudeLast += mChangeAmount;
                } else if (direction == 6) {
                    mLongitudeLast -= mChangeAmount;
                    mLatitudeLast -= mChangeAmount;
                } else if (direction == 7) {
                    mLongitudeLast += mChangeAmount;
                    mLatitudeLast -= mChangeAmount;
                }

                location.setLongitude(mLongitudeLast);
                location.setLatitude(mLatitudeLast);


            } else {

                if (mHasMockLocation) {
                    location.setLongitude((random +  + (int)mLongitudeLast + location.getLongitude()));
                    location.setLatitude((sqrt + (int)mLatitudeLast + location.getLatitude()));
                } else {
                    location.setLongitude((random * (1) + location.getLongitude()));
                    location.setLatitude((sqrt * ((1) + location.getLatitude())));
                }
            }

//            location.setTime(System.currentTimeMillis());
//            location.setElapsedRealtimeNanos(System.nanoTime());
//            location.setAccuracy(10f);
//            location.setAltitude(100f);

//            mLocationManager.setTestProviderEnabled(mProvider, true);
//
//            mLocationManager.setTestProviderStatus(mProvider, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
//            mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
//
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMockLocation() {
        try {
            if (mLocationManager == null) {
                return;
            }

            mMockLocation = new Location(LocationManager.GPS_PROVIDER);
            mMockLocation2 = new Location(LocationManager.NETWORK_PROVIDER);

            mLatitude = mMockLocation.getLatitude();
            mLongitude = mMockLocation.getLongitude();

            if ((Math.abs(mLongitude - mLongitudeLast) != 0)
                    || Math.abs(mLatitude - mLatitudeLast) != 0) {
                mMockLocation.setLongitude(mLongitudeLast);
                mMockLocation.setLatitude(mLatitudeLast);
            }
            //setRandomLocationValue(mMockLocation);

            double random = 1.0d - (2.0d * Math.random());
            double sqrt = (Math.random() > 0.499d ? 1.0d : -1.0d) * Math.sqrt(1.0d - (random * random));

            // debug for new coordinates by distance
            //x2=x1+d×cosθ,y2=y1+d×sinθ,
//            double x = mMockLocation.getLatitude();
//            double y = mMockLocation.getLongitude();
//            double distance = 0.0001;   // meters
//            double x2 = x + distance;   // * Math.cos(90);
//            double y2 = y + distance;   // * Math.sin(90);
//
//            float[] d = new float[3];
//            Location.distanceBetween(x, y, x2, y2, d);
//
//            Log.i("DBG", String.valueOf(x) + ", " + String.valueOf(y) + " >> " + String.valueOf(x2) + ", " + String.valueOf(y2)
//                    + " :: " + String.valueOf(d[0]));
            // Earth's equatorial radius equals 6,378,137 m
            // Earth's average meridional radius is 6,367,449 m
            // one latitudinal second measures 30.715 metres, one latitudinal minute is 1843 metres and one latitudinal degree is 110.6 kilometres
            // one longitudinal second measures 30.92 metres, a longitudinal minute is 1855 metres and a longitudinal degree is 111.3 kilometres
            // Longitudinal length equivalents at selected latitudes
            //            Latitude	        City	            Degree	    Minute	    Second	    ±0.0001°
            //            60°	            Saint Petersburg	55.80 km	0.930 km	15.50 m	    5.58 m
            //            51° 28′ 38″ N	    Greenwich	        69.47 km	1.158 km	19.30 m	    6.95 m
            //            45°	            Bordeaux	        78.85 km	1.31 km	    21.90 m	    7.89 m
            //            30°	            New Orleans	        96.49 km	1.61 km	    26.80 m	    9.65 m
            //            0°	            Quito	            111.3 km	1.855 km	30.92 m	    11.13 m


            // check manual position change amount
            if (mChangeAmountEnabled) {

                if (mLatitudeLast == 0) {
                    mLatitudeLast = (random * (1) + mMockLocation.getLongitude());
                }
                if (mLongitudeLast == 0) {
                    mLongitudeLast = (sqrt * ((1) + mMockLocation.getLatitude()));
                }

                if (AppShared.PrefSensorMode) {
                    //mSensorModeStep = 0;
                    if (mSensorDataMode == SensorDataMode.Sleep || mSensorDataMode == SensorDataMode.Stop) {

                        //runMarryGoAround();
                        runSleepMode();

                    } else if (mSensorDataMode == SensorDataMode.Manual || mSensorDataMode == SensorDataMode.Start) {
                        mSensorDataMode = SensorDataMode.Manual;
                        runManualMode();

                    } else {

                        runWalkingMode();

                    }

                } else {

                    runWalkingMode();

                }

            } else {

                    runSleepMode();

            }

            mMockLocation.setAccuracy(1f);
//            mMockLocation.setAltitude(10f);
//            mMockLocation.setBearing(1f);
//            mMockLocation.setSpeed(10f);
            mMockLocation.setTime(System.currentTimeMillis());
            mMockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

            mMockLocation2.setAccuracy(1f);
//            mMockLocation2.setAltitude(10f);
//            mMockLocation2.setBearing(1f);
//            mMockLocation2.setSpeed(10f);
            mMockLocation2.setTime(System.currentTimeMillis());
            mMockLocation2.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            mMockLocation2.setLongitude(mMockLocation.getLongitude());
            mMockLocation2.setLatitude(mMockLocation.getLatitude());

            mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            mLocationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
            //mLocationManager.setTestProviderEnabled(LocationManager.PASSIVE_PROVIDER, true);

            mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mMockLocation);
            mLocationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, mMockLocation2);
            //mLocationManager.setTestProviderLocation(LocationManager.PASSIVE_PROVIDER, mMockLocation);

            mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
            mLocationManager.setTestProviderStatus(LocationManager.NETWORK_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
            //mLocationManager.setTestProviderStatus(LocationManager.PASSIVE_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());

            if (mMockLocationEvent != null) {
                mMockLocationEvent.MockLocationChanged(mMockLocation);
            }

            updateMockLocationInfo(mMockLocation);

            setSecureSetting();

        } catch (Exception e) {
             e.printStackTrace();
        }
    }

    private void runSleepMode() {
        try {
            mMockLocation.setLongitude(mLongitudeLast );
            mMockLocation.setLatitude(mLatitudeLast);

            mMockLocation2.setLongitude(mMockLocation.getLongitude());
            mMockLocation2.setLatitude(mMockLocation.getLatitude());

            mModeOrigin = new LatLng(mLatitudeLast, mLongitudeLast);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runManualMode() {
        try {
            // need to have correct calculation ???
            LatLng center = new LatLng(mModeOrigin.latitude, mModeOrigin.longitude);

//            mLatitudeLast = center.latitude + (mChangeAmount * Math.sin(mMovementAngle));
//            mLongitudeLast = center.longitude + (mChangeAmount * Math.cos(mMovementAngle));

            double diagonal = Math.abs(mChangeAmount * Math.sin(45)) * mMovementMagnitude;

            if (mMovementAngle == 0) {
                mLatitudeLast += mChangeAmount * mMovementMagnitude;
                mMockLocation.setBearing(0);
            } else if (mMovementAngle == 90) {
                mLongitudeLast += mChangeAmount * mMovementMagnitude;
                mMockLocation.setBearing(90);
            } else if (mMovementAngle == 180) {
                mLatitudeLast -= mChangeAmount * mMovementMagnitude;
                mMockLocation.setBearing(180);
            } else if (mMovementAngle == 270) {
                mLongitudeLast -= mChangeAmount * mMovementMagnitude;
                mMockLocation.setBearing(270);
            } else if (mMovementAngle == 45) {
                mLatitudeLast += diagonal;
                mLongitudeLast += diagonal;
                mMockLocation.setBearing(45);
            } else if (mMovementAngle == 135) {
                mLatitudeLast -= diagonal;
                mLongitudeLast += diagonal;
                mMockLocation.setBearing(135);
            } else if (mMovementAngle == 225) {
                mLongitudeLast -= diagonal;
                mLatitudeLast -= diagonal;
                mMockLocation.setBearing(225);
            } else if (mMovementAngle == 315) {
                mLatitudeLast += diagonal;
                mLongitudeLast -= diagonal;
                mMockLocation.setBearing(315);
            }

            mMockLocation.setBearing(mMovementAngle);

            mMockLocation.setLongitude(mLongitudeLast);
            mMockLocation.setLatitude(mLatitudeLast);

            mMockLocation2.setLongitude(mLongitudeLast);
            mMockLocation2.setLatitude(mLatitudeLast);
            mMockLocation2.setBearing(mMockLocation.getBearing());

            mModeOrigin = new LatLng(mLatitudeLast, mLongitudeLast);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runWalkingMode() {
        try {
            Random rad = new Random();
            int direction = rad.nextInt(32);
            direction /= 4;
            double diagonal = Math.abs(mChangeAmount * Math.sin(45));

            if (direction == 0) {
                mLongitudeLast += mChangeAmount;
                mMockLocation.setBearing(0);
            } else if (direction == 1) {
                mLatitudeLast += mChangeAmount;
                mMockLocation.setBearing(90);
            } else if (direction == 2) {
                mLongitudeLast -= mChangeAmount;
                mMockLocation.setBearing(180);
            } else if (direction == 3) {
                mLatitudeLast -= mChangeAmount;
                mMockLocation.setBearing(270);
            } else if (direction == 4) {
                mLongitudeLast += diagonal;
                mLatitudeLast += diagonal;
                mMockLocation.setBearing(45);
            } else if (direction == 5) {
                mLongitudeLast -= diagonal;
                mLatitudeLast += diagonal;
                mMockLocation.setBearing(135);
            } else if (direction == 6) {
                mLongitudeLast -= diagonal;
                mLatitudeLast -= diagonal;
                mMockLocation.setBearing(225);
            } else if (direction == 7) {
                mLongitudeLast += diagonal;
                mLatitudeLast -= diagonal;
                mMockLocation.setBearing(315);
            }

            mMockLocation.setLongitude(mLongitudeLast);
            mMockLocation.setLatitude(mLatitudeLast);

            mMockLocation2.setLongitude(mLongitudeLast);
            mMockLocation2.setLatitude(mLatitudeLast);
            mMockLocation2.setBearing(mMockLocation.getBearing());

            mModeOrigin = new LatLng(mLatitudeLast, mLongitudeLast);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runRunningMode() {
        mChangeAmount *= 2;
        runWalkingMode();
    }

    private void runMarryGoAround() {
        try {
            LatLng center = new LatLng(mModeOrigin.latitude, mModeOrigin.longitude);

            if (mSensorModeStep == 0) {
                mLatitudeLast = center.latitude + (mChangeAmount * Math.sin(0));
                mLongitudeLast = center.longitude + (mChangeAmount * Math.cos(0));
                mMockLocation.setBearing(0);
            } else if (mSensorModeStep == 1) {
                mLatitudeLast = center.latitude + (mChangeAmount * Math.sin(45));
                mLongitudeLast = center.longitude + (mChangeAmount * Math.cos(45));
                mMockLocation.setBearing(45);
            } else if (mSensorModeStep == 2) {
                mLatitudeLast = center.latitude + (mChangeAmount * Math.sin(90));
                mLongitudeLast = center.longitude + (mChangeAmount * Math.cos(90));
                mMockLocation.setBearing(90);
            } else if (mSensorModeStep == 3) {
                mLatitudeLast = center.latitude + (mChangeAmount * Math.sin(135));
                mLongitudeLast = center.longitude + (mChangeAmount * Math.cos(135));
                mMockLocation.setBearing(135);
            } else if (mSensorModeStep == 4) {
                mLatitudeLast = center.latitude + (mChangeAmount * Math.sin(180));
                mLongitudeLast = center.longitude + (mChangeAmount * Math.cos(180));
                mMockLocation.setBearing(180);
            } else if (mSensorModeStep == 5) {
                mLatitudeLast = center.latitude + (mChangeAmount * Math.sin(225));
                mLongitudeLast = center.longitude + (mChangeAmount * Math.cos(225));
                mMockLocation.setBearing(225);
            } else if (mSensorModeStep == 6) {
                mLatitudeLast = center.latitude + (mChangeAmount * Math.sin(270));
                mLongitudeLast = center.longitude + (mChangeAmount * Math.cos(270));
                mMockLocation.setBearing(270);
            } else if (mSensorModeStep == 7) {
                mLatitudeLast = center.latitude + (mChangeAmount * Math.sin(315));
                mLongitudeLast = center.longitude + (mChangeAmount * Math.cos(315));
                mMockLocation.setBearing(315);
            }
            mSensorModeStep += 1;
            if (mSensorModeStep > 7) {
                mSensorModeStep = 0;
                mModeOrigin = new LatLng(mLatitudeLast, mLongitudeLast);
            }

            mMockLocation.setLongitude(mLongitudeLast);
            mMockLocation.setLatitude(mLatitudeLast);

            mMockLocation2.setLongitude(mLongitudeLast);
            mMockLocation2.setLatitude(mLatitudeLast);
            mMockLocation2.setBearing(mMockLocation.getBearing());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int randomDirection = 0;
    private void runRollerCoast() {
        try {
            Random rad = new Random();

            double diagonal = Math.abs(mChangeAmount * Math.sin(45));
            if (mSensorModeStep < 4) {
                mLatitudeLast = mLatitudeLast + (randomDirection == 0 ? mChangeAmount : -mChangeAmount);
                mLongitudeLast = mLongitudeLast + (randomDirection == 0 ? diagonal : -diagonal);
                mMockLocation.setBearing(0);
            } else {
                mLatitudeLast = mLatitudeLast + (randomDirection == 0 ? mChangeAmount : -mChangeAmount);
                mLongitudeLast = mLongitudeLast - (randomDirection == 0 ? diagonal : -diagonal);
                mMockLocation.setBearing(0);
            }
            mSensorModeStep += 1;
            if (mSensorModeStep > 7) {
                mSensorModeStep = 0;
                if (randomDirection == 0) {
                    randomDirection = rad.nextInt(1);
                }
                mModeOrigin = new LatLng(mLatitudeLast, mLongitudeLast);
            }

            mMockLocation.setLongitude(mLongitudeLast);
            mMockLocation.setLatitude(mLatitudeLast);

            mMockLocation2.setLongitude(mLongitudeLast);
            mMockLocation2.setLatitude(mLatitudeLast);
            mMockLocation2.setBearing(mMockLocation.getBearing());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runJumping() {
        try {
            Random rad = new Random();

            double diagonal = Math.abs(mChangeAmount * Math.sin(45));
            if (mSensorModeStep < 4) {
                //mLatitudeLast = mLatitudeLast
                mLongitudeLast = mLongitudeLast + (randomDirection == 0 ? mChangeAmount : -mChangeAmount);;
                mMockLocation.setBearing(0);
            } else if (mSensorModeStep == 4) {
                mLatitudeLast = mLatitudeLast + (randomDirection == 0 ? mChangeAmount/4/4 : -mChangeAmount);
                mLongitudeLast = mModeOrigin.longitude + (randomDirection == 0 ? mChangeAmount/4/4 : -mChangeAmount);
            } else {
                //mLatitudeLast = mLatitudeLast + (randomDirection == 0 ? mChangeAmount : -mChangeAmount);
                mLongitudeLast = mLongitudeLast - (randomDirection == 0 ? diagonal : -diagonal);
                mMockLocation.setBearing(0);
            }
            mSensorModeStep += 1;
            if (mSensorModeStep > 7) {
                mSensorModeStep = 0;
                if (randomDirection == 0) {
                    randomDirection = rad.nextInt(1);
                }
                mModeOrigin = new LatLng(mLatitudeLast, mLongitudeLast);
            }

            mMockLocation.setLongitude(mLongitudeLast);
            mMockLocation.setLatitude(mLatitudeLast);

            mMockLocation2.setLongitude(mLongitudeLast);
            mMockLocation2.setLatitude(mLatitudeLast);
            mMockLocation2.setBearing(mMockLocation.getBearing());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMockLocationInfo(Location location) {
        try {
            if (!AppShared.PrefShowNotification) {
                return;
            }

            // handle location data

            String msg = String.format("Latitude: %.6f, Longitude: %.6f", location.getLatitude(), location.getLongitude());

            // using notification
            // Set the icon, scrolling text and timestamp
            if (mNotification == null) {
                mNotification = new Notification(R.mipmap.ic_launcher,
                        msg, System.currentTimeMillis());
                mNotification.flags |= Notification.FLAG_NO_CLEAR;
            }

            // The PendingIntent to launch our activity if the user selects this
            // notification
            Intent notificationIntent = new Intent(mContext, GphActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |  Intent.FLAG_ACTIVITY_SINGLE_TOP);

            String title = mContext.getString(R.string.app_name);
            //int notificationID = 1234;

            PendingIntent intent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
            mNotification = builder.setContentIntent(intent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("GPH Location Service")//.setWhen(time)
                    .setAutoCancel(true).setContentTitle(title)
                    .setContentText(msg).build();
            mNM.notify(NOTIFICATION, mNotification);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMockLocationTesting() {
        try {
            if (mLocationManager == null) {
                return;
            }

            mMockLocation = new Location(LocationManager.GPS_PROVIDER);
            mMockLocation2 = new Location(LocationManager.NETWORK_PROVIDER);

            mLatitude = mMockLocation.getLatitude();
            mLongitude = mMockLocation.getLongitude();

            if ((Math.abs(mLongitude - mLongitudeLast) != 0)
                    || Math.abs(mLatitude - mLatitudeLast) != 0) {
                mMockLocation.setLongitude(mLongitudeLast);
                mMockLocation.setLatitude(mLatitudeLast);
            }
            //setRandomLocationValue(mMockLocation);




            double random = 1.0d - (2.0d * Math.random());
            double sqrt = (Math.random() > 0.499d ? 1.0d : -1.0d) * Math.sqrt(1.0d - (random * random));


            // debug for new coordinates by distance
            //x2=x1+d×cosθ,y2=y1+d×sinθ,
//            double x = mMockLocation.getLatitude();
//            double y = mMockLocation.getLongitude();
//            double distance = 0.0001;   // meters
//            double x2 = x + distance;   // * Math.cos(90);
//            double y2 = y + distance;   // * Math.sin(90);
//
//            float[] d = new float[3];
//            Location.distanceBetween(x, y, x2, y2, d);
//
//            Log.i("DBG", String.valueOf(x) + ", " + String.valueOf(y) + " >> " + String.valueOf(x2) + ", " + String.valueOf(y2)
//                    + " :: " + String.valueOf(d[0]));
            // Earth's equatorial radius equals 6,378,137 m
            // Earth's average meridional radius is 6,367,449 m
            // one latitudinal second measures 30.715 metres, one latitudinal minute is 1843 metres and one latitudinal degree is 110.6 kilometres
            // one longitudinal second measures 30.92 metres, a longitudinal minute is 1855 metres and a longitudinal degree is 111.3 kilometres
            // Longitudinal length equivalents at selected latitudes
            //            Latitude	        City	            Degree	    Minute	    Second	    ±0.0001°
            //            60°	            Saint Petersburg	55.80 km	0.930 km	15.50 m	    5.58 m
            //            51° 28′ 38″ N	    Greenwich	        69.47 km	1.158 km	19.30 m	    6.95 m
            //            45°	            Bordeaux	        78.85 km	1.31 km	    21.90 m	    7.89 m
            //            30°	            New Orleans	        96.49 km	1.61 km	    26.80 m	    9.65 m
            //            0°	            Quito	            111.3 km	1.855 km	30.92 m	    11.13 m


            // check manual position change amount
            if (mChangeAmountEnabled) {

                if (mLatitudeLast == 0) {
                    mLatitudeLast = (random * (1) + mMockLocation.getLongitude());
                }
                if (mLongitudeLast == 0) {
                    mLongitudeLast = (sqrt * ((1) + mMockLocation.getLatitude()));
                }

                if (AppShared.PrefSensorMode) {
                    //mSensorModeStep = 0;
                    if (mSensorDataMode == SensorDataMode.Sleep) {
                        mInterval = AppShared.PrefLocationChangeInterval * 1.5;
                        runWalkingMode();
                    } else if (mSensorDataMode == SensorDataMode.Walking) {
                        mInterval = AppShared.PrefLocationChangeInterval;
                        runWalkingMode();
                    } else if (mSensorDataMode == SensorDataMode.Running) {
                        mInterval = AppShared.PrefLocationChangeInterval / 2;
                        runRunningMode();
                    } else if (mSensorDataMode == SensorDataMode.MarryGoAround) {
                        mInterval = AppShared.PrefLocationChangeInterval * 1.5;
                        runMarryGoAround();
                    } else if (mSensorDataMode == SensorDataMode.RollerCoast) {
                        mInterval = AppShared.PrefLocationChangeInterval;
                        runRollerCoast();
                    } else if (mSensorDataMode == SensorDataMode.Jumping) {
                        mInterval = AppShared.PrefLocationChangeInterval / 2;
                        runJumping();
                    } else {
                        runSleepMode();
                    }

                } else {

                    runWalkingMode();

                }

            } else {

//                if (mHasMockLocation) {
//                    mMockLocation.setLongitude((random +  + (int)mLongitudeLast + mMockLocation.getLongitude()));
//                    mMockLocation.setLatitude((sqrt + (int)mLatitudeLast + mMockLocation.getLatitude()));
//                } else {
//                    mMockLocation.setLongitude((random * (1) + mMockLocation.getLongitude()));
//                    mMockLocation.setLatitude((sqrt * ((1) + mMockLocation.getLatitude())));
//                }

                runSleepMode();

            }

            mMockLocation.setAccuracy(1f);
//            mMockLocation.setAltitude(10f);
//            mMockLocation.setBearing(1f);
//            mMockLocation.setSpeed(10f);
            mMockLocation.setTime(System.currentTimeMillis());
            mMockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

            mMockLocation2.setAccuracy(1f);
//            mMockLocation2.setAltitude(10f);
//            mMockLocation2.setBearing(1f);
//            mMockLocation2.setSpeed(10f);
            mMockLocation2.setTime(System.currentTimeMillis());
            mMockLocation2.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            mMockLocation2.setLongitude(mMockLocation.getLongitude());
            mMockLocation2.setLatitude(mMockLocation.getLatitude());

            mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            mLocationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
            //mLocationManager.setTestProviderEnabled(LocationManager.PASSIVE_PROVIDER, true);

            mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mMockLocation);
            mLocationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, mMockLocation2);
            //mLocationManager.setTestProviderLocation(LocationManager.PASSIVE_PROVIDER, mMockLocation);

            mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
            mLocationManager.setTestProviderStatus(LocationManager.NETWORK_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
            //mLocationManager.setTestProviderStatus(LocationManager.PASSIVE_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());

            if (mMockLocationEvent != null) {
                mMockLocationEvent.MockLocationChanged(mMockLocation);
            }

            updateMockLocationInfo(mMockLocation);

            setSecureSetting();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
