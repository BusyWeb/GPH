package com.ccs.gph.gps;

import android.os.ParcelFileDescriptor;
import android.support.v4.provider.DocumentFile;

import com.ccs.gph.util.AppShared;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by BusyWeb on 1/14/2017.
 */

public class Gps {

    public File GpsFile;
    public ArrayList<GpsData> GpsEntries;

    public Gps() {
        this.GpsEntries = new ArrayList<GpsData>();
        this.GpsFile = new File(AppShared.gGpsFilePath);
    }

    public Gps(File file) {
        //this.GpsFile = file;
        loadFromFile(file);
    }

    private boolean loadFromFile(File file) {
        boolean success = false;
        FileInputStream in = null;
        BufferedReader reader = null;
        try {
            this.GpsFile = file;

            if (GpsEntries == null) {
                GpsEntries = new ArrayList<GpsData>();
            }

            Clear();

            in = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(in));
            String line;

            // read first line: file name (CSV)
            line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                if (line != null && line.length() > 0) {
                    GpsEntries.add(new GpsData(line, AppShared.DATA_FORMAT_CSV));
                }
            }

            if (reader != null) {
                reader.close();
            }
            if (in != null) {
                in.close();
            }
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
        }
        return success;
    }


    public void Clear() {
        if (GpsEntries != null) {
            GpsEntries.clear();
        }
    }

    public void AddEntry(GpsData gpsData) {
        if (GpsEntries == null) {
            GpsEntries = new ArrayList<GpsData>();
        }

        gpsData.id = GpsEntries.size() + 1;
        GpsEntries.add(gpsData);
    }

    public GpsData GetEntry(int id) {
        GpsData entry = null;
        try {
            if (GpsEntries.size() < 1) {
                return entry;
            }
            if ((id - 1) < 0 || (id - 1) >= GpsEntries.size()) {
                entry = GpsEntries.get(0);
            } else {
                entry = GpsEntries.get(id -1);
            }
            if (entry == null && id < GpsEntries.size()) {
                entry = GpsEntries.get(id);
            }
        } catch (Exception e) {

        }
        return entry;
    }


    private String GpxHeaders = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<gpx version=\"1.1\" "
            + "creator=\"Dashboard Cam Android\" "
            + "xmlns=\"http://www.topografix.com/GPX/1/1\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" "
            + ">" ;
    private String GpxFooters = "</trk></gpx>";
}
