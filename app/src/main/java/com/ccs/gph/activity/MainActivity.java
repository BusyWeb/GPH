package com.ccs.gph.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.gph.R;
import com.ccs.gph.gps.AppGps;
import com.ccs.gph.gps.AppLocationManager;
import com.ccs.gph.service.serviceGPS;
import com.ccs.gph.singleton.ApplicationSingletonGPS;
import com.ccs.gph.util.AppShared;
import com.ccs.gph.util.GeneralHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    Button btnStart, btnStop;

    ApplicationSingletonGPS mApp;

    double speed = 4;

    LocationManager locationMgr = null;
    ActionBarActivity activity = null;

    //baidu api
    //WalkingRouteLine route = null;
    //CoordinateConverter converter = new CoordinateConverter();
    List listPoints = new ArrayList();
    List listPointsRevers = new ArrayList();
    double totalDistance = 0;
    int currIndex = 0;
    int totalCount = 0;
    double curDistance = 0;

    //timer handle
    final Handler taskHandle = new Handler();

    String strProvider = LocationManager.GPS_PROVIDER;

    private static final int PERMISSION_REQUEST_APP = 1;
    private static final String TAG = "MainActivity";
    private static Activity mActivity;
    private static Context mContext;

    private static TextView latitude, longitude;
    private static Switch switchMethod;

    private static EditText editLatitude, editLongitude;
    private static Button buttonSetLocation;
    private static double mLatitude, mLongitude;
    private static boolean mHasMockLocation = false;
    private static EditText editTextChangeAmount;
    private static CheckBox checkBoxChangeAmount;

    private static EditText editTextAddress;
    private static Button buttonSetAddress;
    private static RadioGroup radioGroupMovement;
    private static RadioButton radioMin, radioDefault, radioMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;
        mContext = this;
        AppShared.gContext = this;
        AppShared.gActivity = this;

        GeneralHelper.LoadPreferences(this);

        mApp = (ApplicationSingletonGPS)getApplicationContext();
        //mApp = ApplicationSingletonGPS.getInstance();

        latitude = (TextView) findViewById(R.id.textViewLatitude);
        longitude = (TextView) findViewById(R.id.textViewLongitude);
        switchMethod = (Switch) findViewById(R.id.switchMethod);

        btnStart = (Button)findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //android.os.Process.killProcess(android.os.Process.myPid());
                //System.exit(1);
                //mApp.mockLocation();

                checkLocationValues();

                // check developer mock settings
                boolean isMockOn = ApplicationSingletonGPS.isMockSettingsON(mActivity);
                boolean isMockPermission = ApplicationSingletonGPS.areThereMockPermissionApps(mActivity);

                if (!isMockOn) {
                    // Developer settings
                    Intent intent = new Intent();
                    intent.setClassName("com.android.settings", "com.android.settings.DevelopmentSettings");
                    mActivity.startActivity(intent);
                    return;
                }
                if (!isMockPermission) {
                    // not showing the mock_location permision
                }



                if (switchMethod.isChecked()) {
                    startLocationMock();
                } else {
                    startMock(true);
                }

                switchMethod.setEnabled(false);
            }
        });

        btnStop = (Button)findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //mApp.mockLocationRemoval();

                if (switchMethod.isChecked()) {
                    stopLocationMock();
                } else {
                    stopMock();
                }

                switchMethod.setEnabled(true);
            }
        });

        editLatitude = (EditText) findViewById(R.id.editTextLatitude);
        editLongitude = (EditText) findViewById(R.id.editTextLongitude);
        buttonSetLocation = (Button) findViewById(R.id.buttonSetLocation);
        buttonSetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationValues();

                if (mHasMockLocation) {
                    Toast.makeText(mContext, "Using provided location...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Using random location values...", Toast.LENGTH_LONG).show();
                }
            }
        });

        locationMgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        editTextChangeAmount = (EditText) findViewById(R.id.editTextChangeAmount);
        checkBoxChangeAmount = (CheckBox) findViewById(R.id.checkBoxEnableChangeAmount);


        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        buttonSetAddress = (Button) findViewById(R.id.buttonSetAddress);
        buttonSetAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = editTextAddress.getText().toString();
                if (address == null || address.length() < 1) {
                    Toast.makeText(mContext, "Address required!", Toast.LENGTH_LONG).show();
                    return;
                }
                new SaveAddressLocationTask().execute(address);
            }
        });

        if (AppShared.PrefAddress != null && AppShared.PrefAddress.length() > 1) {
            editTextAddress.setText(AppShared.PrefAddress);
            editLatitude.setText(String.valueOf(AppShared.PrefAddressLatitude));
            editLongitude.setText(String.valueOf(AppShared.PrefAddressLongitude));
        }

        radioGroupMovement = (RadioGroup) findViewById(R.id.radioGroupMovement);
        radioMin = (RadioButton) findViewById(R.id.radioButtonMin);
        radioMax = (RadioButton) findViewById(R.id.radioButtonMax);
        radioDefault = (RadioButton) findViewById(R.id.radioButtonDefault);

        int radioId = radioDefault.getId();
        if (AppShared.PrefMovementMagnitude.equalsIgnoreCase("min")) {
            radioId = radioMin.getId();
            editTextChangeAmount.setText("0.0001");
        } else if (AppShared.PrefMovementMagnitude.equalsIgnoreCase("max")) {
            radioId = radioMax.getId();
            editTextChangeAmount.setText("0.0005");
        } else if (AppShared.PrefMovementMagnitude.equalsIgnoreCase("default")) {
            radioId = radioDefault.getId();
            editTextChangeAmount.setText("0.00025");
        }
        radioGroupMovement.check(radioId);
        radioGroupMovement.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                try {
                    String value = "default";

                    if (checkedId == radioMin.getId()) {
                        value = "min";
                        editTextChangeAmount.setText("0.0001");
                    } else if (checkedId == radioMax.getId()) {
                        value = "max";
                        editTextChangeAmount.setText("0.0005");
                    } else {
                        editTextChangeAmount.setText("0.00025");
                    }

                    GeneralHelper.SavePreference(mContext, AppShared.PREF_MOVEMENT_MAGNITUDE_KEY, value);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //checkLocationValues();

        checkPermissions();
    }

    private class SaveAddressLocationTask extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog mProgress = null;
        private String mAddress = "";
        //private double mLat = 0d;
        //private double mLng = 0d;

        @Override
        protected void onPreExecute() {
            try {
                mProgress = new ProgressDialog(mContext);
                mProgress.setMessage("Loading location...");
                mProgress.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;

            try {
                mAddress = params[0];

                Geocoder geocoder = new Geocoder(mContext);
                List<Address> addresses = geocoder.getFromLocationName(mAddress, 5);
                Address location = addresses.get(0);
                if (location != null) {
                    mLatitude = location.getLatitude();
                    mLongitude = location.getLongitude();
                    success = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            try {
                if (mProgress != null && mProgress.isShowing()) {
                    mProgress.dismiss();
                }

                mProgress = null;
                if (result) {
                    GeneralHelper.SavePreference(mContext, AppShared.PREF_ADDRESS_KEY, mAddress);
                    GeneralHelper.SavePreference(mContext, AppShared.PREF_ADDRESS_LATITUDE_KEY, String.valueOf(mLatitude));
                    GeneralHelper.SavePreference(mContext, AppShared.PREF_ADDRESS_LONGITUDE_KEY, String.valueOf(mLongitude));


                    editLatitude.setText(String.valueOf(mLatitude));
                    editLongitude.setText(String.valueOf(mLongitude));
                } else {
                    Toast.makeText(mContext, "Failed to get location from address...", Toast.LENGTH_LONG).show();
                }

                checkLocationValues();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkLocationValues() {
        try {

            String lat = editLatitude.getText().toString();
            String lng = editLongitude.getText().toString();

            if (lat == null || lng == null || lat.length() < 1 || lng.length() < 1) {
                mHasMockLocation = false;
                mLatitude = 0.0d;
                mLongitude = 0.0d;
                return;
            }
            mHasMockLocation = true;
            mLatitude = Double.parseDouble(lat);
            mLongitude = Double.parseDouble(lng);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPermissions() {
        try {

            boolean isMockOn = ApplicationSingletonGPS.isMockSettingsON(this);
            boolean isMockPermission = ApplicationSingletonGPS.areThereMockPermissionApps(this);

            if (!isMockOn) {
                // Developer settings
            }
            if (!isMockPermission) {
                // not showing the mock_location permision
            }

            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    + ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                    + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    + + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permission == PackageManager.PERMISSION_GRANTED) {

                prepareApp();

            } else {
                requestPermissions();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    private void requestPermissions() {
        try {
            final Activity thisActivity = this;

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(thisActivity, permissions, PERMISSION_REQUEST_APP);
                }
            };

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                Snackbar.make(this.getWindow().getDecorView(), "Storage, internet, and location permission required.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", listener)
                        .show();

            } else {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_APP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_APP) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0) {
            boolean storage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean internet = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            boolean locationFine = grantResults[2] == PackageManager.PERMISSION_GRANTED;
            boolean locationCoarse = grantResults[3] == PackageManager.PERMISSION_GRANTED;

            if (locationFine) {
                Log.d(TAG, "Storage write permission granted.");

                prepareApp();

                return;
            }
            if (!internet) {
                Log.d(TAG, "Internet access is not granted");
            }
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPH")
                .setMessage("Required permissions missing.")
                .setPositiveButton("OK", listener)
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();
        try {


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        try {
            prepareStop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareApp() {
        try {

            if (useSampleGpsData) {
                appGpsSample = GeneralHelper.LoadSampleGpsData(this);
            }

            boolean gpsEnabled = GeneralHelper.IsGPSEnabled(mContext);

            AppShared.gLocationManager.StartLocationManager(
                    GpsAvailabilityListener,
                    GpsFixedListener,
                    NewLocationListener
            );

            if (locationMockStarted) {
                startLocationMock();
            } else {
                stopLocationMock();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareStop() {
        try {
            if (AppShared.gLocationManager != null) {
                AppShared.gLocationManager.Close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLocationMock() {
        try {
            locationMockStarted = true;
            updateUi();

            if (mCheckLocationRun != null) {
                CheckLastLocationHandler.sendEmptyMessage(-1);
            }
            mCheckLocationRun = new CheckLastLocationRun();
            CheckLastLocationHandler.postDelayed(mCheckLocationRun, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopLocationMock() {
        try {
            locationMockStarted = false;
            updateUi();

            if (mCheckLocationRun != null) {
                CheckLastLocationHandler.sendEmptyMessage(-1);
            }

            if (mMockLocationRun != null) {
                MockLocationHandler.sendEmptyMessage(-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AppLocationManager.OnGpsAvailabilityChangedListener GpsAvailabilityListener = new AppLocationManager.OnGpsAvailabilityChangedListener() {
        @Override
        public void onGpsAvailabilityChanged(final boolean gpsAvailable) {


            // debug
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!gpsAvailable) {
                        //textViewGpsStatus.setText(GeneralHelper.GetResourcesString(R.string.text_gps_not_available));
                        //buttonGps.setImageResource(R.drawable.ic_action_gps_not_fixed);
                    } else {
                        //textViewGpsStatus.setText(GeneralHelper.GetResourcesString(R.string.text_gps_available));
                        //buttonGps.setImageResource(R.drawable.ic_action_gps);
                    }
                }
            });
        }
    };
    private AppLocationManager.OnNewLocationListener NewLocationListener = new AppLocationManager.OnNewLocationListener() {
        @Override
        public void onNewLocation(final Location newLocation) {
            AppShared.gLocationData.NewLocation = newLocation;

            mLastLocationTime = new Date().getTime();
            String provider = newLocation.getProvider();
            if (provider.equalsIgnoreCase("network")) {
                return;
            }

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //textViewLatitude.setText(String.format("%.10f", newLocation.getLatitude()));
                    //textViewLongitude.setText(String.format("%.10f", newLocation.getLongitude()));
                }
            });

        }
    };

    private static boolean mGpsFixed = false;
    private AppLocationManager.OnGpsFixedListener GpsFixedListener = new AppLocationManager.OnGpsFixedListener() {
        @Override
        public void onGpsFixedListener(final boolean fixed) {
            mGpsFixed = fixed;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (fixed) {
                        //textViewGpsStatus.setText(GeneralHelper.GetResourcesString(R.string.text_gps_fixed));
                        //buttonGps.setImageResource(R.drawable.ic_action_gps);
                    } else {
                        //buttonGps.setImageResource(R.drawable.ic_action_gps_not_fixed);
                    }
                }
            });

        }
    };

    private void updateUi() {
        try {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (locationMockStarted) {
                        btnStart.setEnabled(false);
                        btnStop.setEnabled(true);
                        buttonSetLocation.setEnabled(false);

                        editLatitude.setEnabled(false);
                        editLongitude.setEnabled(false);
                        //editTextChangeAmount.setEnabled(false);
                        //checkBoxChangeAmount.setEnabled(false);

                        editTextAddress.setEnabled(false);
                        buttonSetAddress.setEnabled(false);
                        radioGroupMovement.setEnabled(false);
                        radioMin.setEnabled(false);
                        radioMax.setEnabled(false);
                        radioDefault.setEnabled(false);
                    } else {
                        btnStart.setEnabled(true);
                        btnStop.setEnabled(false);
                        buttonSetLocation.setEnabled(true);
                        editLatitude.setEnabled(true);
                        editLongitude.setEnabled(true);
                        //editTextChangeAmount.setEnabled(true);
                        //checkBoxChangeAmount.setEnabled(true);
                        radioGroupMovement.setEnabled(true);
                        radioMin.setEnabled(true);
                        radioMax.setEnabled(true);
                        radioDefault.setEnabled(true);
                        if (mHasMockLocation) {
                            latitude.setText(String.valueOf(mLatitude));
                            longitude.setText(String.valueOf(mLongitude));
                        } else {
                            latitude.setText("0.0");
                            longitude.setText("0.0");
                        }

                        editTextAddress.setEnabled(true);
                        buttonSetAddress.setEnabled(true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateLocationInfo() {
        try {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (mLocation == null) {
                        latitude.setText("0.0");
                        longitude.setText("0.0");
                        return;
                    }
                    latitude.setText(String.format("%.6f", mLocation.getLatitude()));
                    longitude.setText(String.format("%.6f", mLocation.getLongitude()));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CheckLastLocationRun mCheckLocationRun = null;
    private static Long mLastLocationTime = 0L;
    private static Long mLastLocationCheckTime = 0L;
    private static Location mLocation, mLocationLast;
    private static boolean useSampleGpsData = true;
    static AppGps appGpsSample;
    static int sampleGpsDataId = 1;
    static boolean locationMockStarted = false;

    public static class CheckLastLocationRun implements Runnable {

        @Override
        public void run() {
            mLastLocationCheckTime = new Date().getTime();

            CheckLastLocationHandler.sendEmptyMessage(0);
        }
    }
    public static final Handler CheckLastLocationHandler = new Handler() {

        @Override
        public void handleMessage(Message message) {
            int what = message.what;

            if (what == -1) {
                // stop...
                if (mCheckLocationRun != null) {
                    this.removeCallbacks(mCheckLocationRun);
                }
                mCheckLocationRun = null;
                return;
            } else {

                if (mCheckLocationRun != null) {
                    this.removeCallbacks(mCheckLocationRun);
                }
                if (!locationMockStarted) {
                    return;
                }

                Location newLocation = AppShared.gLocationManager.RequestLastKnownLocation();

                try {
                    String provider = newLocation.getProvider();
                    if (!provider.equalsIgnoreCase("network")) {

                        processManualLocationData(newLocation);

                    }
                } catch (Exception e) {

                }

                int nextCheck = 0;
                nextCheck = 1000 - (int) (new Date().getTime() - mLastLocationCheckTime);
                if (nextCheck < 0) {
                    nextCheck = 0;
                }

                mCheckLocationRun = new CheckLastLocationRun();
                this.postDelayed(mCheckLocationRun, nextCheck);
            }
        }
    };

    private static void processManualLocationData(Location newLocation) {
        try {
            mLastLocationTime = new Date().getTime();

            mLocationLast = mLocation;
            mLocation = newLocation;

            if (useSampleGpsData) {
                sampleGpsDataId += 2;
                if (sampleGpsDataId > 1850) {
                    sampleGpsDataId = 1;
                    //ClearMap();
                }
                AppGps.Gps gpsData = appGpsSample.GetGpsEntry(sampleGpsDataId);
                GeneralHelper.SetLocationDataFromGpsData(mLocation, gpsData);
            }

            if (newLocation != null) {
                int longTemp = (int) (newLocation.getLongitude() * 1E6);
                int latTemp = (int) (newLocation.getLatitude() * 1E6);
            }

            updateLocationInfo();

//            Handler h = new Handler();
//            h.post(new UpdateMap());

//            AnalyzeLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }












    // Original app codes: Mock Location usage

    public void mockLocation(Location loc) {
        locationMgr.setTestProviderEnabled(strProvider, true);
        locationMgr.setTestProviderStatus(strProvider, LocationProvider.AVAILABLE,
                null, System.currentTimeMillis());
        //set location at last
        locationMgr.setTestProviderLocation(strProvider, loc);
    }
/*

    @SuppressLint("NewApi")
    public void mockLocation(LatLng point) {

        Location newLocation = new Location(strProvider);

        //convert baidu location to gsp location
        LatLng dstPoint = convertBaiduToGps(point);

        newLocation.setLatitude(dstPoint.latitude);
        newLocation.setLongitude(dstPoint.longitude);
        newLocation.setAccuracy(500);
        newLocation.setTime(System.currentTimeMillis());
        newLocation.setElapsedRealtimeNanos(9);

        mockLocation(newLocation);
    }
*/

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
            Location mockLocation = new Location(strProvider); // a string

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

            updateMockLocationInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMockLocationInfo() {
        try {
            latitude.setText(String.format("%.6f", mLatitude));
            longitude.setText(String.format("%.6f", mLongitude));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static MockLocationRun mMockLocationRun = null;

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

                mMockLocationRun = new MockLocationRun();
                this.postDelayed(mMockLocationRun, nextCheck);
            }
        }
    };

    public void startMock(boolean foreverFlag) {
        try {
            // using service
            Intent service = new Intent(mContext, serviceGPS.class);
            service.putExtra("start", 1);
            service.putExtra("hasMockLocation", mHasMockLocation);
            service.putExtra("latitude", editLatitude.getText().toString());
            service.putExtra("longitude", editLongitude.getText().toString());
            service.putExtra("enableAmount", checkBoxChangeAmount.isChecked());
            service.putExtra("amount", editTextChangeAmount.getText().toString());
            startService(service);

            locationMockStarted = true;
            updateUi();

            if (true) {
                return;
            }

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {

            } else {
                ActivityCompat.requestPermissions(this, new String[] {
                                // Manifest.permission.ACCESS_MOCK_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION },
                        0); //TAG_CODE_PERMISSION_LOCATION);
            }

            if (locationMgr == null) {
                locationMgr = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
            }

            //if has , remove first
//            if (locationMgr.getProvider(strProvider) != null) {
//
//                locationMgr.removeTestProvider(strProvider);
//            }

/*
            if (listPoints.size()<=0) {
                //无路径，不走路
                callback.updateLatLngInfo(null, "无路径，不走路");
                return;
            }

            //赋值，是不是一直走下去
            mockForever = foreverFlag;
*/

//            locationMgr.addTestProvider
//                    (
//                            strProvider,
//                            "requiresNetwork" == "",
//                            "requiresSatellite" == "",
//                            "requiresCell" == "",
//                            "hasMonetaryCost" == "",
//                            "supportsAltitude" == "",
//                            "supportsSpeed" == "",
//                            "supportsBearing" == "",
//                            android.location.Criteria.POWER_LOW,
//                            android.location.Criteria.ACCURACY_FINE
//                    );
            locationMgr.addTestProvider
                    (
                            strProvider,
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

            locationMgr.setTestProviderEnabled(strProvider, true);

            Toast.makeText(getApplicationContext(), "OK loc prov found!", Toast.LENGTH_SHORT).show();

//            Location mockLocation = new Location(strProvider); // a string
//
//            if (mHasMockLocation) {
//                setRandomLocationValue(mockLocation);
//                mLatitude = mockLocation.getLatitude();
//                mLongitude = mockLocation.getLongitude();
//            } else {
//                mockLocation.setLatitude(mLatitude);  // double
//                mockLocation.setLongitude(mLongitude);
//            }
//
//            mockLocation.setAltitude(0.0);
//            mockLocation.setTime(System.currentTimeMillis());
//            mockLocation.setElapsedRealtimeNanos(1000);
//            mockLocation.setAccuracy(Criteria.ACCURACY_HIGH);

            updateMockLocation();

            //lm.setTestProviderLocation( mocLocationProvider, mockLocation);
            Toast.makeText(getApplicationContext(), "Working " + strProvider, Toast.LENGTH_SHORT).show();

            //stopFlag = Boolean.FALSE;
            //start handle runable
            //taskHandle.postDelayed(this, 2);

            locationMockStarted = true;
            updateUi();

            if (mMockLocationRun != null) {
                MockLocationHandler.sendEmptyMessage(-1);
            }
            mMockLocationRun = new MockLocationRun();
            MockLocationHandler.postDelayed(mMockLocationRun, 1000);

        } catch (Exception ex) {
//            if (locationMgr.getProvider(strProvider) != null) {
//                locationMgr.removeTestProvider(strProvider);
//            }
            Log.e("exception", ex.getMessage());
            //this.callback.updateLocationInfo(null, "ERROR: " + ex.getMessage());
            //stopFlag = Boolean.TRUE;
        }
    }

    public void stopMock() {
        try {
            // service method
            Intent service = new Intent(mContext, serviceGPS.class);
            service.putExtra("stop", 1);
            startService(service);

            locationMockStarted = false;
            updateUi();

            if (true) {
                return;
            }



            //stopFlag = Boolean.TRUE;

            // callback.updateLatLngInfo(null, "stop");
            if (locationMgr.getProvider(strProvider) != null) {

                //taskHandle.removeCallbacks(this);

                locationMgr.clearTestProviderLocation(strProvider);
                locationMgr.clearTestProviderEnabled(strProvider);
                locationMgr.removeTestProvider(strProvider);

                //     this.callback.updateLocationInfo(null, "Stop mocking location!");


            }

            locationMgr = null;

            locationMockStarted = false;
            updateUi();

            if (mMockLocationRun != null) {
                MockLocationHandler.sendEmptyMessage(-1);
            }

        } catch (Exception ex) {
            Log.e("exception", ex.getMessage());
            //    this.callback.updateLocationInfo(null, "ERROR:" + ex.getMessage());
            //    stopFlag = Boolean.TRUE;
        }
    }


    public Location getLocation() {
        try {
            if (locationMgr != null &&
                    locationMgr.getProvider(strProvider) != null) {
                return locationMgr.getLastKnownLocation(strProvider);
            }
        } catch (SecurityException exsec) {

            Log.e("ex sec", exsec.getMessage());
        } catch (Exception ex) {
            Log.e("exception", ex.getMessage());
            //    this.callback.updateLocationInfo(null, "ERROR:" + ex.getMessage());
            return null;
        }
        return null;
    }


}
