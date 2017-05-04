package com.ccs.gph.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ccs.gph.BuildConfig;
import com.ccs.gph.R;
import com.ccs.gph.mylocations.MyLocationData;
import com.ccs.gph.service.serviceGPS;
import com.ccs.gph.singleton.ApplicationSingletonGPS;
import com.ccs.gph.util.AppShared;
import com.ccs.gph.util.GeneralHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class GphActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSION_REQUEST_APP = 1;
    private static final int LIST_MYLOCATIONS_REQUEST = 2;
    private static final String TAG = "GphActivity";
    private static Activity mActivity;
    private static Context mContext;
    private static Resources mResources;
    private static GoogleMap mGoogleMap;
    private static SupportMapFragment mMapFragment;
    private static Marker mMarker;
    private static float mZoom, mTilt, mBearing;

    private static String mMapAddress = "";
    private static LocationManager mLocationManager = null;

    private static double mLatitude, mLongitude;
    private static boolean mHasMockLocation = false;
//    private static boolean mLocationMockStarted = false;

    private static LinearLayout rootView;
    private static FloatingActionButton fab;
    private static EditText editLatitude, editLongitude;
    private static Button buttonSetLocation;
    private static EditText editTextAddress;
    private static Button buttonSetAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gph);

        mActivity = this;
        mContext = this;
        mResources = getResources();
        AppShared.gContext = this;
        AppShared.gActivity = this;
        AppShared.gResources = getResources();

        GeneralHelper.CheckAndCreateAppFolders();
        GeneralHelper.LoadPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (AppShared.LocationMockStarted) {
                        stopMock();

                        fab.setImageResource(R.drawable.ic_action_play);
                    } else {

                        boolean isMockOn = ApplicationSingletonGPS.isMockSettingsON(mActivity);
                        if (!isMockOn) {
                            String message = "";
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            {
                                message = "Mock Location App setting needed.";
                            }
                            else
                            {
                                message = "Mock Location permission needed.";
                            }

                            Snackbar.make(fab, message, Snackbar.LENGTH_LONG)
                                    .setAction("Go to settings", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // Developer settings
                                            Intent intent = new Intent();
                                            intent.setClassName("com.android.settings", "com.android.settings.DevelopmentSettings");
                                            mActivity.startActivity(intent);
                                        }
                                    })
                                    .show();
                            return;
                        }

                        startMock();

                        fab.setImageResource(R.drawable.ic_action_stop);

                        if (AppShared.PrefMapAppClose) {
                            finish();
                        } else {
                            if (AppShared.PrefMapShowMovement) {

                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        prepareApp();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(mContext, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(intent, LIST_MYLOCATIONS_REQUEST);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_my_locations) {

            Intent intent = new Intent(mContext, MyLocationsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(intent, LIST_MYLOCATIONS_REQUEST);

//            mMarker = null;
//            mGoogleMap.clear();
//            mGoogleMap = null;

        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(mContext, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(intent, LIST_MYLOCATIONS_REQUEST);

        } else if (id == R.id.nav_help) {
            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:\"gph\"")));
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.busywww.com")));
        } else if (id == R.id.nav_contact) {
            String uriText =
                    "mailto:busyweb@gmail.com" +
                            "?subject=" + Uri.encode("About android app GPH") +
                            "&body=" + Uri.encode("Contact the developer.");

            Uri uri = Uri.parse(uriText);

            Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
            sendIntent.setData(uri);
            startActivity(Intent.createChooser(sendIntent, "Send email"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void prepareApp() {
        try {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            rootView = (LinearLayout) findViewById(R.id.rootView);
            editLatitude = (EditText) findViewById(R.id.editTextLatitude);
            editLongitude = (EditText) findViewById(R.id.editTextLongitude);
            buttonSetLocation = (Button) findViewById(R.id.buttonSetLocation);
            buttonSetLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkLocationValues();

                }
            });
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
                    if (!GeneralHelper.IsOnline(mContext)) {
                        Toast.makeText(mContext, "Off-line, please check network.", Toast.LENGTH_LONG).show();
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

            if (AppShared.LocationMockStarted) {
                fab.setImageResource(R.drawable.ic_action_stop);
                prepareMockLocationEvent();
            } else {
                fab.setImageResource(R.drawable.ic_action_play);
            }

            checkPermissions();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean mLoadFromActivityResult = false;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            if (requestCode == LIST_MYLOCATIONS_REQUEST) {

                if (resultCode == RESULT_OK) {
                    String dataId = intent.getStringExtra("LocationId");
                    if (dataId != null && dataId.length() > 0) {
                        MyLocationData data = GeneralHelper.FindLocationDataById(dataId);
                        if (data != null) {
                            mLoadFromActivityResult = true;
                            mLatitude = data.Latitude;
                            mLongitude = data.Longitude;
                            mMapAddress = data.Address;
                            setMarker(new LatLng(data.Latitude, data.Longitude));
                            updateUi();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        try {
            mMarker = null;
            setupMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        try {
            if (mMarker != null) {
                mMarker.remove();
            }
            if (mGoogleMap != null) {
                mGoogleMap.clear();
                mGoogleMap = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        boolean gpsEnabled = GeneralHelper.IsGPSEnabled(mContext);
        if (!gpsEnabled) {
            try {
                //Toast.makeText(mContext, "GPS not available.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                    + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    + ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS);
            if (permission == PackageManager.PERMISSION_GRANTED) {

                //prepareApp();

            } else {
                requestPermissions();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final String[] permissions = new String[]
            {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_SETTINGS
            };

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
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_SETTINGS)) {

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
            boolean secureSettings = grantResults[4] == PackageManager.PERMISSION_GRANTED;

            if (!secureSettings) {
                Log.d(TAG, "Secure settings required.");
            }
            if (locationFine) {
                Log.d(TAG, "Storage write permission granted.");

                //prepareApp();

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


    private void setupMap() {
        try {
            if (mGoogleMap == null) {
                //mapView.getMapAsync(mapReadyCallback);
                FragmentManager fmanager = getSupportFragmentManager();
                mMapFragment = (SupportMapFragment) fmanager.findFragmentById(R.id.app_map_view);
                mMapFragment.getMapAsync(mapReadyCallback);
            } else {
                setMarker(new LatLng(mLatitude, mLongitude));
            }
        } catch (Exception e) {

        }
    }

    private OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            try {
                mGoogleMap = googleMap;
                //mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.setOnCameraChangeListener(mapCameraChangeListener);
                mGoogleMap.setOnInfoWindowClickListener(mapInfoWindowClickListener);
                mGoogleMap.setOnMapClickListener(mapClickListener);
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
                mGoogleMap.getUiSettings().setMapToolbarEnabled(true);

                //mZoom = 6f;

                if (AppShared.PrefAddressLatitude != 0 && AppShared.PrefAddressLongitude != 0) {
                    if (!mLoadFromActivityResult) {
                        mLatitude = AppShared.PrefAddressLatitude;
                        mLongitude = AppShared.PrefAddressLongitude;
                        setMarker(new LatLng(mLatitude, mLongitude));
                    } else {
                        setMarker(new LatLng(mLatitude, mLongitude));
                        mLoadFromActivityResult = false;
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        mLatitude = 0;
                        mLongitude = 0;
                    } else {
                        if (mLocationManager == null) {
                            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        }
                        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        mLatitude = location.getLatitude();
                        mLongitude = location.getLongitude();
                        setMarker(new LatLng(mLatitude, mLongitude));
                    }
                }

            } catch (Exception e) {

            }
        }
    };

    private static GoogleMap.OnMapClickListener mapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            mLatitude = latLng.latitude;
            mLongitude = latLng.longitude;

            setMarker(latLng);

            new LoadAddressTask().execute(latLng);
        }
    };

    private static GoogleMap.OnCameraChangeListener mapCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            mZoom = cameraPosition.zoom;
            mTilt = cameraPosition.tilt;
            mBearing = cameraPosition.bearing;
        }
    };

    public static GoogleMap.OnInfoWindowClickListener mapInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {

        @Override
        public void onInfoWindowClick(final Marker marker) {

            if (true) {

                if (mMarker != null) {
                    mMarker.hideInfoWindow();
                }

                final MyLocationData dataItem = new MyLocationData();
                dataItem.Id = UUID.randomUUID().toString();
                dataItem.Latitude = marker.getPosition().latitude;
                dataItem.Longitude = marker.getPosition().longitude;
                dataItem.CreatedDateTime = System.currentTimeMillis();
                dataItem.Address = "";

                final Dialog dialogEdit = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
                Window window = dialogEdit.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                window.setBackgroundDrawable(new BitmapDrawable());

                dialogEdit.setCancelable(true);
                dialogEdit.setCanceledOnTouchOutside(true);
                dialogEdit.setContentView(R.layout.dialog_edit_item);

                final EditText editText = (EditText) dialogEdit.findViewById(R.id.editTextNote);
                editText.setText(dataItem.Name);
                Button btnOk = (Button) dialogEdit.findViewById(R.id.btnEdit);
                btnOk.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        try {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final String name = editText.getText().toString();
                                    if (name == null || name.length() < 1) {
                                        Snackbar.make(fab, "Location name is invalid, please check and try again.", Snackbar.LENGTH_LONG).show();
                                        return;
                                    }

                                    //final ProgressDialog dialog = ProgressDialog.show(AppShared.gContext, "", "Processing...", true);
                                    final Handler handler = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            dialogEdit.cancel();
                                            dialogEdit.dismiss();
                                            //dialog.cancel();
                                            //dialog.dismiss();

                                            GeneralHelper.LoadMyLocations();
                                        }
                                    };
                                    Thread saveLocation = new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                mActivity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        dataItem.Name = name;
                                                        dataItem.Address = GeneralHelper.LoadAddress(marker.getPosition());

                                                        dataItem.SaveToFile(null);
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            handler.sendEmptyMessage(0);
                                        }
                                    };
                                    saveLocation.start();
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                if (!dialogEdit.isShowing()) {
                    dialogEdit.show();
                }

                return;
            }

            final LatLng loc = marker.getPosition();
            final String locLocation = String.format("Latitude: %.6f, Longitude: %.6f", loc.latitude, loc.longitude);
            marker.setSnippet(locLocation);
            marker.showInfoWindow();

            mLatitude = loc.latitude;
            mLongitude = loc.longitude;

            mMapAddress = "";
            final ProgressDialog dialog = ProgressDialog.show(AppShared.gContext, "", "Processing...", true);
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //marker.setSnippet(mMapAddress);
                    //marker.showInfoWindow();

                    updateUi();

                    dialog.dismiss();
                }
            };
            Thread checkUpdate = new Thread() {
                @Override
                public void run() {
                    try {
                        Geocoder geoCoder = new Geocoder(AppShared.gContext, Locale.getDefault());
                        List<Address> addresses;
                        addresses = geoCoder.getFromLocation(loc.latitude, loc.longitude, 5);

                        if (addresses == null) {
                            mMapAddress = "Address: not available.";
                        } else {
                            if (addresses.size() > 0) {
                                for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++)
                                    mMapAddress += addresses.get(0).getAddressLine(i) + " ";
                            } else {
                                mMapAddress += "Address: not available.";
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        mMapAddress = "Address: not available.";
                    }
                    handler.sendEmptyMessage(0);
                }
            };
            checkUpdate.start();
        }
    };

    private static void checkLocationValues() {
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

            LatLng latLng = new LatLng(mLatitude, mLongitude);

            setMarker(latLng);

            new LoadAddressTask().execute(latLng);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SaveAddressLocationTask extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog mProgress = null;
        private String mAddress = "";

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

                    setMarker(new LatLng(mLatitude, mLongitude));
                } else {
                    Toast.makeText(mContext, "Failed to get location from address...", Toast.LENGTH_LONG).show();
                }

                //checkLocationValues();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class LoadAddressTask extends AsyncTask<LatLng, Void, Boolean> {

        private ProgressDialog mProgress = null;
        private LatLng mLatLng = null;

        @Override
        protected void onPreExecute() {
            try {
                mProgress = new ProgressDialog(mContext);
                mProgress.setMessage("Loading address...");
                mProgress.show();

                mMapAddress = "";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(LatLng... params) {
            boolean success = false;

            try {
                mLatLng = params[0];

                Geocoder geoCoder = new Geocoder(AppShared.gContext, Locale.getDefault());
                List<Address> addresses;
                addresses = geoCoder.getFromLocation(mLatLng.latitude, mLatLng.longitude, 2);

                if (addresses == null) {
                    mMapAddress = "Address: not available.";
                } else {
                    if (addresses.size() > 0) {
                        for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++)
                            mMapAddress += addresses.get(0).getAddressLine(i) + " ";
                        success = true;
                    } else {
                        mMapAddress += "Address: not available.";
                    }
                }
                return success;
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
                    updateUi();
                } else {
                    updateUi();
                    Toast.makeText(mContext, "Failed to get address...", Toast.LENGTH_LONG).show();
                }

                //checkLocationValues();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void setMarker(LatLng latLng) {
        try {
            if (mGoogleMap == null) {
                return;
            }
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latLng.latitude, latLng.longitude))
                    .bearing(mBearing).tilt(mTilt).zoom(mZoom).build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            String locLocation = String.format("Latitude: %.6f, Longitude: %.6f", latLng.latitude, latLng.longitude);
            if (mMarker != null) {
                mMarker.setPosition(latLng);
                mMarker.setTitle("Add to favorite.");
                mMarker.setSnippet("( click here )");
                if (!AppShared.LocationMockStarted) {
                    mMarker.showInfoWindow();
                }
            } else {
                Bitmap bitmap = GeneralHelper.GetMarkerBitmap();

                mMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Add to favorite.").snippet("( click here )")
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                if (!AppShared.LocationMockStarted) {
                    mMarker.showInfoWindow();
                }
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!AppShared.LocationMockStarted) {
                        if (mMarker != null) {
                            mMarker.hideInfoWindow();
                        }
                    } else {
                        if (mMarker != null) {
                            if (mMarker.isInfoWindowShown()) {
                                mMarker.hideInfoWindow();
                            }
                        }
                    }
                }
            }, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateUi() {
        try {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    editLatitude.setText(String.valueOf(mLatitude));
                    editLongitude.setText(String.valueOf(mLongitude));
                    editTextAddress.setText(mMapAddress);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private serviceGPS.IMockLocationEvent mMockLocationEvent;
    private void prepareMockLocationEvent() {
        try {
            if (AppShared.PrefMapShowMovement) {
                mMockLocationEvent = new serviceGPS.IMockLocationEvent() {
                    @Override
                    public void MockLocationChanged(Location location) {
                        try {
                            mLatitude = location.getLatitude();
                            mLongitude = location.getLongitude();
                            setMarker(new LatLng(location.getLatitude(), location.getLongitude()));
                            updateUi();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                serviceGPS.SetMockLocatoinEvent(mMockLocationEvent);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startMock() {
        try {

            GeneralHelper.LoadPreferences(mContext);

            prepareMockLocationEvent();

            // using service
            Intent service = new Intent(mContext, serviceGPS.class);
            service.putExtra("start", 1);
            service.putExtra("hasMockLocation", true);
            service.putExtra("latitude", String.valueOf(mLatitude));
            service.putExtra("longitude", String.valueOf(mLongitude));
            service.putExtra("enableAmount", AppShared.PrefRandomMovement);
            service.putExtra("amount", String.valueOf(AppShared.PrefLocationChangeDistance));
            startService(service);

            AppShared.LocationMockStarted = true;
        } catch (Exception ex) {
           Log.e("exception", ex.getMessage());
        }
    }

    public void stopMock() {
        try {
            // service method
            Intent service = new Intent(mContext, serviceGPS.class);
            service.putExtra("stop", 1);
            startService(service);

            AppShared.LocationMockStarted = false;
        } catch (Exception ex) {
            Log.e("exception", ex.getMessage());
        }
    }

}
