package com.ccs.gph.gps;

import android.location.Location;

/**
 * Created by BusyWeb on 1/15/2017.
 */

public class LocationData {

    public Location NewLocation;
    public Location LastLocation;
    public boolean GpsFixed = false;
    public int GpsCount = 0;

    public long UpdateInterval = 1000;
    public boolean FineLocAvailable = false;
    public boolean CoarseLocAvailable = false;

}
