package com.ccs.gph.gps;

import com.ccs.gph.util.GeneralHelper;

import org.json.JSONObject;

/**
 * Created by BusyWeb on 1/15/2017.
 */

public class GpsData {
    public Integer id = 0;
    public Long time = 0L;
    public Long timegps = 0L;
    public Double latitude = 0.0;
    public Double longitude = 0.0;
    public Double altitude = 0.0;
    public Float speed = 0f;
    public Float accuracy = 0f;
    public Float bearing = 0f;
    public Float declination = 0f;

    public GpsData() {
    }

    public GpsData(int id) {
        this.id = id;
    }

    public GpsData(
            int id,
            Long time,
            Long timeGps,
            Double latitude,
            Double longitude,
            Float speed,
            Float accurracy,
            Double altitude,
            Float bearing,
            Float declination){
        this.id = id;
        this.time = time;
        this.timegps = timeGps;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.accuracy = accurracy;
        this.altitude = altitude;
        this.bearing = bearing;
        this.declination = declination;
    }

    public GpsData(String gpsData, String format){
        try {
            if (format.equalsIgnoreCase("csv")) {
                loadGpsDataCsv(gpsData);
            } else if (format.equalsIgnoreCase("json")) {
                loadGpsDataJson(gpsData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGpsDataCsv(String csvData) {
        try {
            String[] data = csvData.split(",");
            this.id = Integer.parseInt(data[0]);
            this.time = Long.parseLong(data[1]);
            this.timegps = Long.parseLong(data[2]);
            this.latitude = Double.parseDouble(data[3]);
            this.longitude = Double.parseDouble(data[4]);

            float s = Float.parseFloat(data[5]);
            if (Float.isNaN(s) || Float.isInfinite(s)) {
                if (this.id == 1) {
                    this.speed = 0f;
                } else {
                    this.speed = 0f;
                }
            } else {
                this.speed = s;
            }

            this.accuracy = Float.parseFloat(data[6]);
            this.altitude = Double.parseDouble(data[7]);
            this.bearing = Float.parseFloat(data[8]);
            this.declination = Float.parseFloat(data[9]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGpsDataJson(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            Object objId = jsonObject.get("id");
            Object objTime = jsonObject.get("time");
            Object objTimeGps = jsonObject.get("timegps");
            Object objLatitude = jsonObject.get("latitude");
            Object objLongitude = jsonObject.get("longitude");
            Object objSpeed = jsonObject.get("speed");
            Object objAccuracy = jsonObject.get("accuracy");
            Object objAltitude = jsonObject.get("altitude");
            Object objBearing = jsonObject.get("bearing");
            Object objDeclination = jsonObject.get("declination");


            this.id = (!GeneralHelper.IsNullOrEmpty(objId) ? (Integer)objId : -1);
            this.time = (!GeneralHelper.IsNullOrEmpty(objTime) ? (Long)objTime : 0);
            this.timegps = (!GeneralHelper.IsNullOrEmpty(objTimeGps) ? (Long)objTimeGps : 0);
            this.latitude = (!GeneralHelper.IsNullOrEmpty(objLatitude) ? (Double)objLatitude : 0);
            this.longitude = (!GeneralHelper.IsNullOrEmpty(objLongitude) ? (Double)objLongitude : 0);

            float s = (!GeneralHelper.IsNullOrEmpty(objSpeed) ? Float.parseFloat(objSpeed.toString()) : 0f);
            if (Float.isNaN(s) || Float.isInfinite(s)) {
                if (this.id == 1) {
                    this.speed = 0f;
                } else {
                    this.speed = 0f;
                }
            } else {
                this.speed = s;
            }

            this.accuracy = (!GeneralHelper.IsNullOrEmpty(objAccuracy) ? Float.parseFloat(objAccuracy.toString()) : 0f);
            this.altitude = (!GeneralHelper.IsNullOrEmpty(objAltitude) ? (Double)objAltitude : 0);
            this.bearing = (!GeneralHelper.IsNullOrEmpty(objBearing) ? Float.parseFloat(objBearing.toString()) : 0f);
            this.declination = (!GeneralHelper.IsNullOrEmpty(objDeclination) ? Float.parseFloat(objDeclination.toString()) : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString(String format) {
        if (format.equalsIgnoreCase("csv")) {
            return toCsv();
        } else if (format.equalsIgnoreCase("json")) {
            return toJson();
        }
        return ",,,,,,,,,";
    }

    private String toCsv() {
        String data = "";
        try {
            data += String.valueOf(this.id) + ",";
            data += String.valueOf(this.time) + ",";
            data += String.valueOf(this.timegps) + ",";
            data += String.valueOf(this.latitude) + ",";
            data += String.valueOf(this.longitude) + ",";
            data += String.valueOf(this.speed) + ",";
            data += String.valueOf(this.accuracy) + ",";
            data += String.valueOf(this.altitude) + ",";
            data += String.valueOf(this.bearing) + ",";
            data += String.valueOf(this.declination);
        } catch (Exception e) {
            e.printStackTrace();
            data = String.valueOf(id) + ",,,,,,,,,";
        }
        return data;
    }

    private String toJson() {
        String data = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", this.id);
            jsonObject.put("time", this.time);
            jsonObject.put("timegps", this.timegps);
            jsonObject.put("latitude", this.latitude);
            jsonObject.put("longitude", this.longitude);
            jsonObject.put("speed", this.speed);
            jsonObject.put("accuracy", this.accuracy);
            jsonObject.put("altitude", this.altitude);
            jsonObject.put("bearing", this.bearing);
            jsonObject.put("declination", this.declination);
            data = jsonObject.toString();
        } catch (Exception e) {
            data = "{id=-1}";
        }

        return data;
    }
}
