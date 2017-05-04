package com.ccs.gph.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ccs.gph.R;
import com.ccs.gph.mylocations.MyLocationData;
import com.ccs.gph.util.AppShared;
import com.ccs.gph.util.GeneralHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MyLocationsActivity extends AppCompatActivity {

    private static Activity mActivity;
    private static Context mContext;

    private static File mMyLocationFolder;
    private static Toolbar mToolbar;

    RecyclerView recyclerView;
    SwipeRefreshLayout refresher;
    LinearLayoutManager layoutManager;

    LocationsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_locations);

        mActivity = this;
        mContext = this;
        AppShared.gContext = this;
        AppShared.gActivity = this;
        AppShared.gResources = getResources();

        mMyLocationFolder = new File(AppShared.RootFolder + AppShared.MyLocationsFolderName);

        GeneralHelper.CheckAndCreateAppFolders();
        GeneralHelper.LoadPreferences(this);

        //mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("My Locations");

        prepareApp();
    }

    private void prepareApp() {
        try {
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);

            refresher = (SwipeRefreshLayout) findViewById(R.id.refresher);
            refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadLocations();
                }
            });

            loadLocations();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void finish() {
        try {
            finalize();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            super.finish();
        }
    }

    ProgressDialog mProgress;

    private class loadLocationsTask extends AsyncTask<Void, Void, Boolean> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            //progressDialog = ProgressDialog.show(mContext, "Loading", "Please wait...", true);
            refresher.setRefreshing(true);
         }

        @Override
        protected Boolean doInBackground(Void...params) {
            try {

                loadLocations();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            try {
                mAdapter = new LocationsAdapter(AppShared.MyLocations);
                recyclerView.setAdapter(mAdapter);

                refresher.setRefreshing(false);

                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }

                //updateTitle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void loadLocations() {
        try {
            GeneralHelper.LoadMyLocations();

            refresher = (SwipeRefreshLayout) findViewById(R.id.refresher);
            refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadLocations();
                }
            });

            mAdapter = new LocationsAdapter(AppShared.MyLocations);
            recyclerView.setAdapter(mAdapter);
            refresher.setRefreshing(false);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public class  LocationViewHolder extends RecyclerView.ViewHolder {

        public int Position;
        public TextView Title;
        public TextView FileDow;
        public TextView FileDate;
        public ImageView MapImage;
        public ProgressBar Loading;
        public ImageView DeleteButton;
        public Button DetailsButton;

        public LocationViewHolder(View itemView) {
            super(itemView);

            Title = (TextView) itemView.findViewById(R.id.textViewItemTitle);
            FileDate = (TextView) itemView.findViewById(R.id.textViewItemDateTime);
            FileDow = (TextView) itemView.findViewById(R.id.textViewDow);
            MapImage = (ImageView) itemView.findViewById(R.id.myImageView);
            DeleteButton = (ImageView) itemView.findViewById(R.id.imageViewDelete);
            Loading = (ProgressBar) itemView.findViewById(R.id.progressBarLoading);
            DetailsButton = (Button) itemView.findViewById(R.id.buttonDetails);
        }

        public void SetPosition(int position) {
            this.Position = position;
        }
    }

    public class LocationsAdapter extends RecyclerView.Adapter<LocationViewHolder> {

        ArrayList<MyLocationData> mLocationList;

        public LocationsAdapter(ArrayList<MyLocationData> locations) {
            mLocationList = locations;
        }


        @Override
        public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LocationViewHolder viewHolder = null;

            try {
                View view = LayoutInflater.from(mContext).inflate(R.layout.cardview_record_list_item, parent, false);
                viewHolder = new LocationViewHolder(view);
            } catch (Exception e) {
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(LocationViewHolder holder, final int position) {
            try {
                if (holder == null) {
                    return;
                }

                holder.SetPosition(position);
                final MyLocationData data = mLocationList.get(position);

                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm a", Locale.getDefault());
                SimpleDateFormat sdf2 = new SimpleDateFormat("EEEE", Locale.getDefault());

                holder.Title.setText(data.Name);
                holder.FileDow.setText(sdf2.format(new Date(data.CreatedDateTime)));
                holder.FileDate.setText(sdf.format(new Date(data.CreatedDateTime)));

                holder.Loading.setVisibility(View.VISIBLE);
                holder.DeleteButton.setTag(data);
                holder.DeleteButton.setOnClickListener(deleteRecordClickListener);

                if (GeneralHelper.IsMapImageExist(data)) {
                    holder.MapImage.setImageBitmap(GeneralHelper.LoadLocationMapImage(data));
                    holder.Loading.setVisibility(View.GONE);
                    holder.MapImage.setVisibility(View.VISIBLE);
                } else {
                    GeneralHelper.LoadAndSaveMapImage(data, holder);
                }

                holder.DetailsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, GphActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("LocationId", data.Id);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mLocationList == null) {
                return 0;
            } else {
                return mLocationList.size();
            }
        }
    }

    View.OnClickListener deleteRecordClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                final MyLocationData data = (MyLocationData)v.getTag();

                if (data == null) {
                    return;
                }

                Snackbar.make(recyclerView,
                        "Delete Location: " + data.Name,
                        Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.WHITE)
                        .setAction("DELETE", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    if (data == null) {
                                        return;
                                    }
                                    File file = new File(AppShared.RootFolder + AppShared.MyLocationsFolderName + "/" + data.Id + ".txt");
                                    if (!file.exists()) {
                                        return;
                                    }
                                    boolean success = file.delete();
                                    File map = new File(AppShared.RootFolder + AppShared.MapImagesFolderName + "/" + data.Id + ".png");
                                    if (map.exists()) {
                                        map.delete();
                                    }
                                    if (success) {
                                        GeneralHelper.LoadMyLocations();
                                        loadLocations();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
