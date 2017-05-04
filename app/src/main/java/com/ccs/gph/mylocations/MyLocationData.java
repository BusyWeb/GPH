package com.ccs.gph.mylocations;

import com.ccs.gph.util.AppShared;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created by BusyWeb on 5/3/2017.
 */

public class MyLocationData {

    public MyLocationData() {}

    public String Id;
    public String Name;
    public String Address;
    public double Latitude;
    public double Longitude;
    public long CreatedDateTime;

    public MyLocationData(String id, String name, String address, double latitude, double longitude, long createdDateTime) {
        this.Id = id;
        this.Name = name;
        this.Address = address;
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.CreatedDateTime = createdDateTime;
    }

    public MyLocationData(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = reader.readLine();

            if (line != null && line.length() > 0) {
                loadValues(line);
            }

            if (reader != null) {
                reader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean SaveToFile(File file) {
        boolean success = false;
        try {
            File f = null;
            if (file == null) {
                f = new File(AppShared.RootFolder + AppShared.MyLocationsFolderName + "/" + this.Id + ".txt");
            } else {
                f = file;
            }
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(f));
            outputStream.write((this.toString() + "\n").getBytes());
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            }
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public void loadValues(String data) {
        try {
            String[] values = data.split("\\|\\|");
            this.Id = values[0];
            this.Name = values[1];
            this.Address = values[2];
            this.Latitude = Double.parseDouble(values[3]);
            this.Longitude = Double.parseDouble(values[4]);
            this.CreatedDateTime = Long.parseLong(values[5]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String toString() {
        String data = "";
        try {
            data += Id + "||";
            data += Name + "||";
            data += Address + "||";
            data += String.valueOf(Latitude) + "||";
            data += String.valueOf(Longitude) + "||";
            data += String.valueOf(CreatedDateTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
