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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.ccs.gph.R;
import com.ccs.gph.activity.MainActivity;
import com.ccs.gph.util.AppShared;

import java.util.Date;

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
            if (mWakeLock != null) {
                mWakeLock.release();
            }

            stopMockService();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCommand(Intent intent) {
        try {
            int start = intent.getIntExtra("start", 0);
            if (start > 0) {
                mHasMockLocation = intent.getBooleanExtra("hasMockLocation", false);
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

            if (mMockLocationRun != null) {
                MockLocationHandler.sendEmptyMessage(-1);
            }
            mMockLocationRun = new MockLocationRun();
            MockLocationHandler.postDelayed(mMockLocationRun, 1000);

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareLocationManager() {
        try {
            if (mLocationManager == null) {
                mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
            }

            mLocationManager.addTestProvider
                    (
                            mProvider,
                            false,
                            false,
                            false,
                            false,
                            false,
                            false,
                            false,
                            android.location.Criteria.POWER_LOW,
                            android.location.Criteria.ACCURACY_FINE
                    );

            mLocationManager.setTestProviderEnabled(mProvider, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopLocationManager() {
        try {
            if (mLocationManager.getProvider(mProvider) != null) {
                mLocationManager.clearTestProviderLocation(mProvider);
                mLocationManager.clearTestProviderEnabled(mProvider);
                mLocationManager.removeTestProvider(mProvider);
            }

            mLocationManager = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double mLatitude, mLongitude;
    private static boolean mHasMockLocation = false;
    private static Long mLastLocationCheckTime = 0L;
    private static boolean locationMockStarted = false;
    private static MockLocationRun mMockLocationRun = null;
    private final static String mProvider = "gps";
    private static LocationManager mLocationManager = null;

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
                nextCheck = 1000 - (int) (new Date().getTime() - mLastLocationCheckTime);
                if (nextCheck < 0) {
                    nextCheck = 0;
                }

                mMockLocationRun = new serviceGPS.MockLocationRun();
                this.postDelayed(mMockLocationRun, nextCheck);
            }
        }
    };

    private void setRandomLocationValue(Location location) {
        try {
            double random = 1.0d - (2.0d * Math.random());
            double sqrt = (Math.random() > 0.499d ? 1.0d : -1.0d) * Math.sqrt(1.0d - (random * random));
            location.setLongitude((random * (1) + location.getLongitude()));
            location.setLatitude((sqrt * ((1) + location.getLatitude())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMockLocation() {
        try {
            Location mockLocation = new Location(mProvider); // a string

            if (!mHasMockLocation) {
                setRandomLocationValue(mockLocation);
                mLatitude = mockLocation.getLatitude();
                mLongitude = mockLocation.getLongitude();
            } else {
                mockLocation.setLatitude(mLatitude);  // double
                mockLocation.setLongitude(mLongitude);
            }

            mockLocation.setAltitude(0.0);
            mockLocation.setTime(System.currentTimeMillis());
            mockLocation.setElapsedRealtimeNanos(1000);
            mockLocation.setAccuracy(Criteria.ACCURACY_HIGH);

            updateMockLocationInfo(mockLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMockLocationInfo(Location location) {
        try {
            // handle location data

            String msg = String.format("Latitude: %.6f, Longitude: %.6f", location.getLongitude(), location.getLongitude());

            // using notification
            // Set the icon, scrolling text and timestamp
            if (mNotification == null) {
                mNotification = new Notification(R.mipmap.ic_launcher,
                        msg, System.currentTimeMillis());
                mNotification.flags |= Notification.FLAG_NO_CLEAR;
            }

            // The PendingIntent to launch our activity if the user selects this
            // notification
            Intent notificationIntent = new Intent(mContext, MainActivity.class);
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



}
