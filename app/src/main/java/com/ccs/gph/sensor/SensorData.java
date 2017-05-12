package com.ccs.gph.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.util.Log;
import android.view.Surface;

import com.ccs.gph.service.serviceGPS;
import com.ccs.gph.util.AppShared;
import com.ccs.gph.util.GeneralHelper;
import com.google.android.gms.maps.model.UrlTileProvider;

import org.json.JSONObject;
import java.security.spec.ECField;
import java.util.Random;

/**
 * Created by BusyWeb on 1/14/2017.
 */

public class SensorData implements Cloneable {

    public int id = 0;

    public SensorEvent CurrentEvent;
    private float[] currentValues;

    private boolean mOrientation, mAccelerometer, mMagnetic, mLight, mProximity, mHumidity, mTemperature;
    public void SetOrientationSensorExists(boolean exists) {
        mOrientation = exists;
    }
    public boolean OrientationSensorExists() {
        return mOrientation;
    }
    public void SetAccelerometerSensorExists(boolean exists) {
        mAccelerometer = exists;
    }
    public boolean AccelerometerSensorExists() {
        return mAccelerometer;
    }
    public void SetMagneticSensorExists(boolean exists) {
        mMagnetic = exists;
    }
    public boolean MagneticSensorExists() {
        return mMagnetic;
    }
    public void SetLightSensorExists(boolean exists) {
        mLight = exists;
    }
    public boolean LightSensorExists() {
        return mLight;
    }
    public void SetProximitySensorExists(boolean exists) {
        mProximity = exists;
    }
    public boolean ProximitySensorExists() {
        return mProximity;
    }
    public void SetHumiditySensorExists(boolean exists) {
        mHumidity = exists;
    }
    public boolean HumiditySensorExists() {
        return mHumidity;
    }
    public void SetTemperatureSensorExists(boolean exists) {
        mTemperature = exists;
    }
    public boolean TemperatureSensorExists() {
        return mTemperature;
    }

    // accelerometer
    public float AccelerometerX = 0f;
    public float AccelerometerY = 0f;
    public float AccelerometerZ = 0f;
    public float AccelerometerMaxX = 0f;
    public float AccelerometerMaxY = 0f;
    public float AccelerometerMaxZ = 0f;
    public double AccelerometerG = 0;
    //public double AccelerometerEvent = 0;

    private float AccelerometerOffsetX = 0f;
    private float AccelerometerOffsetY = 0f;
    private float AccelerometerOffsetZ = 0f;
    public double AccelerometerCalibratedG = 0;
    public float AccelerometerCalibratedX = 0f;
    public float AccelerometerCalibratedY = 0f;
    public float AccelerometerCalibratedZ = 0f;

    public static float AccelerometerGThreshold = 1.5f;   // Sensor G value
    public boolean AccelerometerTriggered = false;
    public long AccelerometerTimestamp = 0;
    public long AccelerometerTimestampLast = 0;
    public double AccelerometerCalibratedGLast = 0;
    public float[] mGravityValues;

    // orientation
    public int OrientationAngle = 0;
    public int OrientationPitch = 0;    // rotation x-axis
    public int OrientationRoll = 0;     // rotation y-axis
    public long OrientationTimestamp = 0;
    public int OrientationPitchOffset = 0;
    public int OrientationRollOffset = 0;
    public int OrientationPitchCalibrated = 0;
    public int OrientationRollCalibrated = 0;
    public long OrientationTimestampLast = 0;
    public int OrientationAngleLast = 0;
    public int OrientationPitchLast = 0;    // rotation x-axis
    public int OrientationRollLast = 0;     // rotation y-axis


    // magnetic field
    public float MagneticX = 0f;
    public float MagneticY = 0f;
    public float MagneticZ = 0f;
    public long MagneticTimestamp = 0;
    public long MagneticTimestampLast = 0;
    public float MagneticXLast = 0f;
    public float MagneticYLast = 0f;
    public float MagneticZLast = 0f;
    public float[] mMagneticValues;


    // light
    public float Light = 0f;
    public long LightTimestamp = 0;
    public float LightLast = 0f;
    public long LightTimestampLast = 0;

    // proximity
    public float Proximity = 0f;
    public long ProximityTimestamp = 0;
    public float ProximityLast = 0f;
    public long ProximityTimestampLast = 0;

    // humidity
    public float Humidity = 0f;
    public long HumidityTimestamp = 0;
    public float HumidityLast = 0f;
    public long HumidityTimestampLast = 0;


    // temperature
    public float Temperature = 0f;
    public long TemperatureTimestamp = 0;
    public float TemperatureLast = 0f;
    public long TemperatureTimestampLast = 0;


    // rotation: by manual check
    // 0 degree rotation (natural orientation)
    public int Rotation = Surface.ROTATION_0;
    public boolean UpsideDown = false;


    public SensorData() {}

    public void NewEvent(SensorEvent event) {
        CurrentEvent = event;
        calculateValues();
    }



    private void calculateValues() {
        try {
            currentValues = CurrentEvent.values;

            Sensor sensor = CurrentEvent.sensor;
            int sensorType = sensor.getType();
            switch (sensorType) {
                case Sensor.TYPE_ACCELEROMETER:
                    // values[0]: Gx
                    // values[1]: Gy
                    // values[2]: Gz
                    // int inclination = (int) Math.round(Math.toDegrees(Math.acos(g[2])));
                    // int rotation = (int) Math.round(Math.toDegrees(Math.atan2(g[0], g[1])));
                    mGravityValues = currentValues.clone();

                    AccelerometerCalibratedGLast = AccelerometerCalibratedG;
                    AccelerometerTimestampLast = AccelerometerTimestamp;
                    calculateAccelerometer(currentValues, CurrentEvent.timestamp);
                    break;
                case Sensor.TYPE_ORIENTATION:
                    // values[0]: azimuth, angle, 0-359 (0:north, 90:east, 180:south, 270:west)
                    // values[1]: pitch, rotation x axis, -180-180
                    // values[2]: roll, rotation y axis, -90-90

                    OrientationAngleLast = OrientationAngle;
                    OrientationPitchLast = OrientationPitch;
                    OrientationRollLast = OrientationRoll;
                    OrientationTimestampLast = OrientationTimestamp;

                    calculateOrientation(currentValues, CurrentEvent.timestamp);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    // ambient magnetic field in X, Y, Z axis
                    mMagneticValues = currentValues.clone();

                    MagneticXLast = MagneticX;
                    MagneticYLast = MagneticY;
                    MagneticZLast = MagneticZ;
                    MagneticTimestampLast = MagneticTimestamp;

                    MagneticX = currentValues[0];
                    MagneticY = currentValues[1];
                    MagneticZ = currentValues[2];
                    MagneticTimestamp = CurrentEvent.timestamp;
                    break;
                case Sensor.TYPE_LIGHT:
                    // values[0]: ambient light level in SI lux units
                    LightLast = Light;
                    LightTimestampLast = LightTimestamp;

                    Light = currentValues[0];
                    LightTimestamp = CurrentEvent.timestamp;
                    break;
                case Sensor.TYPE_PROXIMITY:
                    // values[0]: distance measured in centimeters
                    ProximityLast = Proximity;
                    ProximityTimestampLast = ProximityTimestamp;

                    Proximity = currentValues[0];
                    ProximityTimestamp = CurrentEvent.timestamp;
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    // values[0]: ambient air humidity in percent
                    HumidityLast = Humidity;
                    HumidityTimestampLast = HumidityTimestamp;

                    Humidity = currentValues[0];
                    HumidityTimestamp = CurrentEvent.timestamp;
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    // values[0]: room temperature in degree Celsius
                    TemperatureLast = Temperature;
                    TemperatureTimestampLast = TemperatureTimestamp;

                    Temperature = currentValues[0];
                    TemperatureTimestamp = CurrentEvent.timestamp;
                    break;
                default:
                    break;
            }

            getDeviceRotation();

            AnalyzeSensorData();

            logSensorData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDeviceRotation() {
        try {
            // 0: device is in default orientation (Y axis is vertical and points up)
            // 1: device is rotated 90 degrees counter-clockwise from default orientation (X axis is vertical and points up)
            // 2: device is rotated 180 degrees from default orientation (Y axis is vertical and points down)
            // 3: device is rotated 90 degrees clockwise from default orientation (X axis is vertical and points down)

            if (AppShared.gActivity == null) {
                return;
            }
            Rotation = AppShared.gActivity.getWindowManager().getDefaultDisplay().getRotation();
            if (Rotation == Surface.ROTATION_180) {
                UpsideDown = true;
            } else {
                UpsideDown = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void logSensorData() {
        try {
            //StringBuilder sb = new StringBuilder();
            String accelerometer = String.format("Accelerometer: %.3f, %.3f, %.3f, %.3f", AccelerometerCalibratedX, AccelerometerCalibratedY, AccelerometerCalibratedZ, AccelerometerCalibratedG);
            String orientation = "Orientation: " + String.valueOf(OrientationAngle) + ", " + String.valueOf(OrientationPitchCalibrated) + ", " + String.valueOf(OrientationRollCalibrated);
            String magnetic = String.format("Magnetic: %.3f, %.3f, %.3f", MagneticX, MagneticY, MagneticZ);
            String light = String.format("Light: %.3f", Light);
            String proximity = String.format("Proximity: %.3f", Proximity);
            String humidity = String.format("Humidity: %.3f", Humidity);
            String temperature = String.format("Temperature: %.3f", Temperature);
            String rotation = "Rotation: " + String.valueOf(Rotation) + ", UpsideDown: " + String.valueOf(UpsideDown);
            Log.i("DBG", accelerometer);
            Log.i("DBG", magnetic);
            Log.i("DBG", orientation);
            Log.i("DBG", String.valueOf(mAngle));
//            Log.i("DBG", light);
//            Log.i("DBG", proximity);
//            Log.i("DBG", humidity);
//            Log.i("DBG", temperature);
//            Log.i("DBG", rotation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean still, walking, running, platUp, platDown, bright, screenUp, covered;
    private long lastModeSetTimestamp = System.currentTimeMillis();
    private static float mAngle = 0f;

    public void AnalyzeSensorData() {
        try {
            if (AppShared.gSensorManager == null || AppShared.gSensorData == null) {
                if (serviceGPS.ServiceStarted()) {
                    serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Walking);
                }
                return;
            }
            // testing analyze
            still = false;
            walking = false;
            running = false;
            platUp = false;
            platDown = false;
            bright = false;
            screenUp = false;
            covered = false;

            // check g
            if (AccelerometerCalibratedG > 1.5) {
                // device move command
                // stop or start

                if (System.currentTimeMillis() - lastModeSetTimestamp > 2000) {
                    if (serviceGPS.GetSensorDataMode() == serviceGPS.SensorDataMode.Stop) {
                        serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Start);
                    }
                    lastModeSetTimestamp = System.currentTimeMillis();

                    return;
                }
            }

            // check orientation sensor first
            if (OrientationSensorExists()) {

                if (serviceGPS.GetSensorDataMode() == serviceGPS.SensorDataMode.Stop) {
                    return;
                }

                int pitch = Math.abs(OrientationPitch);
                int roll = Math.abs(OrientationRoll);
                if (roll < 5) {
                    // placed on the plat surface
                    if (pitch < 5) {
                        // screen up
                        platUp = true;
                        platDown = false;
                    } else if (pitch > 175) {
                        // screen down
                        platUp = false;
                        platDown = true;
                    }
                }

                // calculate tilt
                // negative roll: forward
                // positive roll: backward
                // negative pitch: right
                // positive pitch: left

                // manual angle settings
                // need to check this later
                //float angle = GeneralHelper.GetAngle(OrientationPitchCalibrated, OrientationRollCalibrated);
                if (Math.abs(OrientationRollCalibrated) <= 5 && Math.abs(OrientationPitchCalibrated) <= 5) {

                    mAngle = 0;
                    //serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Stop);

                } else if (Math.abs(OrientationRollCalibrated) <= 5 && Math.abs(OrientationPitchCalibrated) > 5) {

                    if (OrientationPitchCalibrated < - 5) {
                        // backward
                        mAngle = 180;
                    } else if (OrientationPitchCalibrated > 5) {
                        // forward
                        mAngle = 0;
                    }
                } else if (Math.abs(OrientationRollCalibrated) > 5 && Math.abs(OrientationPitchCalibrated) <= 5) {

                    if (OrientationRollCalibrated > 5) {
                        // left
                        mAngle = 270;
                    } else if (OrientationRollCalibrated < - 5) {
                        // right
                        mAngle = 90;
                    }

                } else {

                    if (OrientationRollCalibrated > 5) {
                        // left
                        if (OrientationPitchCalibrated < - 5) {
                            // left - backward
                            mAngle = 225;
                        } else if (OrientationPitchCalibrated > 5) {
                            // left - forward
                            mAngle = 315;
                        }

                    } else if (OrientationRollCalibrated < - 5) {
                        // right
                        if (OrientationPitchCalibrated < -5) {
                            // right - backward
                            mAngle = 135;
                        } else if (OrientationPitchCalibrated > 5) {
                            // right - forward
                            mAngle = 45;
                        }

                    } else {

                    }
                }
                serviceGPS.SetSensorModeManual(mAngle, Math.abs(OrientationPitchCalibrated), Math.abs(OrientationRollCalibrated));
                lastModeSetTimestamp = System.currentTimeMillis();

            } else {
                // use accelerometer and magnetic sensor
                if (AccelerometerSensorExists() && MagneticSensorExists()) {
                    float R[] = new float[9];
                    float I[] = new float[9];

                    boolean ok = SensorManager.getRotationMatrix(R, I, mGravityValues, mMagneticValues);
                    if (ok) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(R, orientation);

                        float pitch = orientation[1];
                        float roll = orientation[2];

                        mAngle = GeneralHelper.GetAngle(pitch, roll);

                        float[] incline = new float[3];
                        incline = mGravityValues.clone();
                        double normalize = Math.sqrt(incline[0] * incline[0] + incline[1] * incline[1] + incline[2] * incline[2]);

                        incline[0] = (float)(incline[0] / normalize);
                        incline[1] = (float)(incline[1] / normalize);
                        incline[2] = (float)(incline[2] / normalize);

                        int plat2 = (int) Math.round(Math.toDegrees(Math.acos(incline[2])));
                        platUp = (Math.abs(plat2) < 1);

                        serviceGPS.SetSensorModeManual(mAngle, Math.abs(pitch), Math.abs(roll));
                        lastModeSetTimestamp = System.currentTimeMillis();
                    }

                } else {
                    if (AccelerometerSensorExists()) {
                        // use simple methods
                        mAngle = GeneralHelper.GetAngle(AccelerometerCalibratedX, AccelerometerCalibratedY);
                        serviceGPS.SetSensorModeManual(mAngle, Math.abs(OrientationPitchCalibrated), Math.abs(OrientationRollCalibrated));
                        lastModeSetTimestamp = System.currentTimeMillis();

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (platUp || platDown) {
                // plat surface, no movements assumed
               checkPlatStatusDelay();
            }
        }
    }

    private void checkPlatStatusDelay() {
        try {
            // check plat status after a second later ... to make sure it is still in same status
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (platUp || platDown) {
                        // plat surface, no movements assumed
                        serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Stop);
                        lastModeSetTimestamp = System.currentTimeMillis();
                    }
                }
            }, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CalibrateSensors() {
        try {
            AccelerometerOffsetX = 0 - AccelerometerX;
            AccelerometerOffsetY = 0 - AccelerometerY;
            AccelerometerOffsetZ = 0 - AccelerometerZ;

            OrientationPitchOffset = 0 - OrientationPitch;
            OrientationRollOffset = 0 - OrientationRoll;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void calculateAccelerometer(float[] values, long timestamp) {
        try {
            AccelerometerMaxX = (AccelerometerX > AccelerometerMaxX ? AccelerometerX : AccelerometerMaxX);
            AccelerometerMaxY = (AccelerometerY > AccelerometerMaxY ? AccelerometerY : AccelerometerMaxY);
            AccelerometerMaxZ = (AccelerometerZ > AccelerometerMaxZ ? AccelerometerZ : AccelerometerMaxZ);

            AccelerometerX = values[0];
            AccelerometerY = values[1];
            AccelerometerZ = values[2];

            AccelerometerCalibratedX = AccelerometerX + AccelerometerOffsetX;
            AccelerometerCalibratedY = AccelerometerY + AccelerometerOffsetY;
            AccelerometerCalibratedZ = AccelerometerZ + AccelerometerOffsetZ;

            AccelerometerG = (Math.sqrt(AccelerometerX*AccelerometerX + AccelerometerY*AccelerometerY + AccelerometerZ*AccelerometerZ) / 9.81);
            AccelerometerCalibratedG = (
                    Math.sqrt(
                            AccelerometerCalibratedX*AccelerometerCalibratedX
                                    + AccelerometerCalibratedY*AccelerometerCalibratedY
                                    + AccelerometerCalibratedZ*AccelerometerCalibratedZ) / 9.81);

            if (AccelerometerG > (AccelerometerGThreshold)) {
                AccelerometerTriggered = true;
                //AccelerometerEvent = AccelerometerCalibratedG;
            } else {
                AccelerometerTriggered = false;
                //AccelerometerEvent = AccelerometerCalibratedG;
            }
            AccelerometerTimestamp = timestamp;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateOrientation(float[] values, long timestamp) {
        try {
            OrientationAngle = (int)values[0];
            OrientationPitch = (int)values[1];
            OrientationRoll = (int)values[2];
            OrientationTimestamp = timestamp;

            OrientationPitchCalibrated = OrientationPitch + OrientationPitchOffset;
            OrientationRollCalibrated = OrientationRoll + OrientationRollOffset;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public SensorData(int id) {
//        this.id = id;
//    }
//
//    public SensorData(String data, String format){
//        try {
//            if (format.equalsIgnoreCase("csv")) {
//                loadSensorDataCsv(data);
//            } else if (format.equalsIgnoreCase("json")) {
//                loadSensorDataJson(data);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public String toCsv() {
//        String data = "";
//        try {
////            data = String.format("%d,%.2f,%.2f,%.2f,%.2f",
////                    id,
////                    dX,
////                    dY,
////                    dZ,
////                    g);
//            data = String.format("%d,%.2f,%.2f,%.2f,%.2f",
//                    id,
//                    CalibratedX,
//                    CalibratedY,
//                    CalibratedZ,
//                    CalibratedG);
//        } catch (Exception e) {
//            e.printStackTrace();
//            data = String.valueOf(id) + ",0,0,0,0";
//        }
//        return data;
//    }
//
//    public String toJson() {
//        String data = "";
//        try {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("id", this.id);
//            jsonObject.put("dX", this.CalibratedX);
//            jsonObject.put("dY", this.CalibratedY);
//            jsonObject.put("dZ", this.CalibratedZ);
//            jsonObject.put("g", this.CalibratedG);
//            data = jsonObject.toString();
//        } catch (Exception e) {
//            data = "{id=-1}";
//        }
//        return data;
//    }
//
//    private void loadSensorDataCsv(String csvData) {
//        try {
//            String[] data = csvData.split(",");
//            this.id = Integer.parseInt(data[0]);
//            this.CalibratedX = Float.parseFloat(data[1]);
//            this.CalibratedY = Float.parseFloat(data[2]);
//            this.CalibratedZ = Float.parseFloat(data[3]);
//            this.CalibratedG = Double.parseDouble(data[4]);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void loadSensorDataJson(String jsonData) {
//        try {
//            JSONObject jsonObject = new JSONObject(jsonData);
//            Object objId = jsonObject.get("id");
//            Object objdX = jsonObject.get("dX");
//            Object objdY = jsonObject.get("dY");
//            Object objdZ = jsonObject.get("dZ");
//            Object objg = jsonObject.get("g");
//
//            this.id = (!GeneralHelper.IsNullOrEmpty(objId) ? (Integer)objId : -1);
//            this.CalibratedX = (!GeneralHelper.IsNullOrEmpty(objdX) ? Float.parseFloat(objdX.toString()) : 0);
//            this.CalibratedY = (!GeneralHelper.IsNullOrEmpty(objdY) ? Float.parseFloat(objdY.toString()) : 0);
//            this.CalibratedZ = (!GeneralHelper.IsNullOrEmpty(objdZ) ? Float.parseFloat(objdZ.toString()) : 0);
//            this.CalibratedG = (!GeneralHelper.IsNullOrEmpty(objg) ? (Double)objg : 0);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public SensorData clone() throws CloneNotSupportedException {
        return (SensorData) super.clone();
    }


    public void AnalyzeSensorDataTesting() {
        try {
            if (AppShared.gSensorManager == null || AppShared.gSensorData == null) {
                if (serviceGPS.ServiceStarted()) {
                    serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Walking);
                }
                return;
            }
            // testing analyze
            still = false;
            walking = false;
            running = false;
            platUp = false;
            platDown = false;
            bright = false;
            screenUp = false;
            covered = false;

            // 1. check device movement
            if (AccelerometerSensorExists()) {
                double changeA = Math.abs(AccelerometerCalibratedG - AccelerometerCalibratedGLast);
                if (changeA < 0.5) {
                    // still mode
                    still = true;
                } else if (changeA >= 0.5 && changeA <= 1.5) {
                    // walking mode
                    walking = true;
                } else if (changeA > 1.5) {
                    // running mode
                    running = true;
                }
            }

            // 2. check device placement
            if (OrientationSensorExists()) {
                double pitch = Math.abs(OrientationPitch);
                double roll = Math.abs(OrientationRoll);
                if (roll < 5) {
                    // placed on the plat surface
                    if (pitch < 5) {
                        // screen up
                        platUp = true;
                        platDown = false;
                    } else if (pitch > 175) {
                        // screen down
                        platUp = false;
                        platDown = true;
                    }
                }
            }

            // 3. check surround
            if (LightSensorExists()) {
                double light = Math.abs(Light);
                if (light >= 0 && light <= 10) {
                    // very dark
                    bright = false;
                } else if (light > 10) {
                    // bright enough
                    bright = true;
                }
            }

            // 4. check device covered
            if (ProximitySensorExists()) {
                double distance = Math.abs(Proximity);
                if (distance <= 1) {
                    // screen is covered
                    covered = true;
                } else {
                    // screen is not covered
                    covered = false;
                }
            }

            // set mode
            Random random = new Random();
            long setModeTimePassed = System.currentTimeMillis() - lastModeSetTimestamp;
            serviceGPS.SensorDataMode currentMode = serviceGPS.GetSensorDataMode();

            if (platDown || platUp) {
                // on the plat surface
                if (!bright || covered) {
                    // bright == !covered
                    serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Sleep);
                } else {
                    // possibly only still mode here
                    if (setModeTimePassed > 60000) {
                        // 1 minute passed with same mode
                        int newRandom = random.nextInt(3);
                        int direction = random.nextInt(1);
                        int newRandom2 = direction == 0 ? -newRandom : newRandom;

                        serviceGPS.AdjustLocationOrigin(newRandom2 / 10000, newRandom / 10000);
                        //serviceGPS.SetSensorDaraMode(currentMode);
                        if (still) {
                            // maybe on the pocket, stay-still
                            serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Sleep);
                        } else if (walking) {
                            serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Walking);
                        } else if (running) {
                            serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Running);
                        }

                        lastModeSetTimestamp = System.currentTimeMillis();
                    } else {
                        if (still) {
                            // maybe on the pocket, stay-still
                            serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Sleep);
                        } else if (walking) {
                            serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Walking);
                        } else if (running) {
                            serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Running);
                        }
                    }
                }
                lastModeSetTimestamp = System.currentTimeMillis();
            } else {

                if (currentMode == serviceGPS.SensorDataMode.MarryGoAround
                        || currentMode == serviceGPS.SensorDataMode.Jumping
                        || currentMode == serviceGPS.SensorDataMode.RollerCoast) {
                    // skip setting until mode processing is finished
                    if (setModeTimePassed > 60000) {
                        // 1 minute passed with same mode
                        int newRandom = random.nextInt(3);
                        int direction = random.nextInt(1);
                        int newRandom2 = direction == 0 ? -newRandom : newRandom;

                        serviceGPS.AdjustLocationOrigin(newRandom2 / 10000, newRandom / 10000);
                        lastModeSetTimestamp = System.currentTimeMillis();
                    }
                    //serviceGPS.SetSensorDaraMode(currentMode);
                    if (still) {
                        serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.MarryGoAround);
                    } else if (walking) {
                        serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Jumping);
                    } else if (running) {
                        serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.RollerCoast);
                    }
                } else {
                    // need to implementing timestamp check for randomized mode

                    if (still) {
                        serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.MarryGoAround);
                    } else if (walking) {
                        serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.Jumping);
                    } else if (running) {
                        serviceGPS.SetSensorDaraMode(serviceGPS.SensorDataMode.RollerCoast);
                    }
                    lastModeSetTimestamp = System.currentTimeMillis();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
