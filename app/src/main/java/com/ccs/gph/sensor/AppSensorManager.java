package com.ccs.gph.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.ccs.gph.util.AppShared;

import java.util.List;


/**
 * Created by BusyWeb on 1/14/2017.
 */

public class AppSensorManager {

    public interface IOnNewSensorListener {
        void onNewSensorEvent(SensorEvent event);
    }

    private IOnNewSensorListener mNewSensorListener;

    private SensorManager mSensorManager;
    private Sensor mOrientation, mAccelerometer, mMagnetic, mLight, mProximity, mHumidity, mTemperature;
    private SensorEventListener mSensorListener;

    private SensorData mSensorData = null;

    public AppSensorManager(SensorData sensorData) {
        mSensorData = sensorData;
        RegisterSensorListener();
    }

    public SensorData GetSensorData() {
        return mSensorData;
    }

    public static void StartSensorManager(IOnNewSensorListener newSensorListener) {
        AppShared.gSensorData = new SensorData();

        AppShared.gSensorManager = new AppSensorManager(AppShared.gSensorData);
        AppShared.gSensorManager.SetNewSensorListener(newSensorListener);
    }


    public void SetNewSensorListener(IOnNewSensorListener l) {
        mNewSensorListener = l;
    }

    public void RegisterSensorListener() {
        UnRegisterSensorListeners();

        mSensorManager = (SensorManager) AppShared.gContext.getSystemService(Context.SENSOR_SERVICE);

        if (mSensorListener == null) {
            CreateSensorListeners();
        }

        registerSensor(Sensor.TYPE_ACCELEROMETER);
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensorList.size() > 0) {
            mAccelerometer = sensorList.get(0);
            mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorData.SetAccelerometerSensorExists(true);
        } else {
            mAccelerometer = null;
            mSensorData.SetAccelerometerSensorExists(false);
        }

        registerSensor(Sensor.TYPE_ORIENTATION);
        List<Sensor> sensorList2 = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (sensorList2.size() > 0) {
            mOrientation = sensorList2.get(0);
            mSensorManager.registerListener(mSensorListener, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorData.SetOrientationSensorExists(true);
        } else {
            mOrientation = null;
            mSensorData.SetOrientationSensorExists(false);
        }

        registerSensor(Sensor.TYPE_MAGNETIC_FIELD);
        List<Sensor> sensorList3 = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensorList3.size() > 0) {
            mMagnetic = sensorList3.get(0);
            mSensorManager.registerListener(mSensorListener, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorData.SetMagneticSensorExists(true);
        } else {
            mMagnetic = null;
            mSensorData.SetMagneticSensorExists(false);
        }

        registerSensor(Sensor.TYPE_LIGHT);
        List<Sensor> sensorList4 = mSensorManager.getSensorList(Sensor.TYPE_LIGHT);
        if (sensorList4.size() > 0) {
            mLight = sensorList4.get(0);
            mSensorManager.registerListener(mSensorListener, mLight, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorData.SetLightSensorExists(true);
        } else {
            mLight = null;
            mSensorData.SetLightSensorExists(false);
        }

        registerSensor(Sensor.TYPE_PROXIMITY);
        List<Sensor> sensorList5 = mSensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
        if (sensorList5.size() > 0) {
            mProximity = sensorList5.get(0);
            mSensorManager.registerListener(mSensorListener, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorData.SetProximitySensorExists(true);
        } else {
            mProximity = null;
            mSensorData.SetProximitySensorExists(false);
        }

        registerSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        List<Sensor> sensorList6 = mSensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY);
        if (sensorList6.size() > 0) {
            mHumidity = sensorList6.get(0);
            mSensorManager.registerListener(mSensorListener, mHumidity, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorData.SetHumiditySensorExists(true);
        } else {
            mHumidity = null;
            mSensorData.SetHumiditySensorExists(false);
        }

        registerSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        List<Sensor> sensorList7 = mSensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (sensorList6.size() > 0) {
            mTemperature = sensorList7.get(0);
            mSensorManager.registerListener(mSensorListener, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorData.SetTemperatureSensorExists(true);
        } else {
            mTemperature = null;
            mSensorData.SetTemperatureSensorExists(false);
        }
    }

    public void UnRegisterSensorListeners() {
        try {
            if (mSensorManager != null) {
                if (mAccelerometer != null) {
                    mSensorManager.unregisterListener(mSensorListener, mAccelerometer);
                }
                if (mOrientation != null) {
                    mSensorManager.unregisterListener(mSensorListener, mOrientation);
                }
                if (mMagnetic != null) {
                    mSensorManager.unregisterListener(mSensorListener, mMagnetic);
                }
                if (mProximity != null) {
                    mSensorManager.unregisterListener(mSensorListener, mProximity);
                }
                if (mHumidity != null) {
                    mSensorManager.unregisterListener(mSensorListener, mHumidity);
                }
                if (mTemperature != null) {
                    mSensorManager.unregisterListener(mSensorListener, mTemperature);
                }
                if (mLight != null) {
                    mSensorManager.unregisterListener(mSensorListener, mLight);
                }
            }

            mSensorListener = null;

            mSensorManager = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerSensor(int type) {
        Sensor sensor = mSensorManager.getDefaultSensor(type);
        if (sensor != null) {
            mSensorManager.registerListener(mSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void CreateSensorListeners() {

        mSensorListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                mNewSensorListener.onNewSensorEvent(event);

//                Sensor sensor = event.sensor;
//                long timestamp = event.timestamp;
//                int sensorType = sensor.getType();
//                switch (sensorType) {
//                    case Sensor.TYPE_ACCELEROMETER:
//                        // values[0]: Gx
//                        // values[1]: Gy
//                        // values[2]: Gz
//                        break;
//                    case Sensor.TYPE_ORIENTATION:
//                        // values[0]: azimuth, angle, 0-359 (0:north, 90:east, 180:south, 270:west)
//                        // values[1]: pitch, rotation x axis, -180-180
//                        // values[2]: roll, rotation y axis, -90-90
//                        break;
//                    case Sensor.TYPE_MAGNETIC_FIELD:
//                        // ambient magnetic field in X, Y, Z axis
//                        break;
//                    case Sensor.TYPE_LIGHT:
//                        // values[0]: ambient light level in SI lux units
//                        break;
//                    case Sensor.TYPE_PROXIMITY:
//                        // values[0]: distance measured in centimeters
//                        break;
//                    case Sensor.TYPE_RELATIVE_HUMIDITY:
//                        // values[0]: ambient air humidity in percent
//                        break;
//                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
//                        // values[0]: room temperature in degree Celsius
//                        break;
//                    default:
//                        break;
//                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub

            }
        };
    }

    public void Close() {
        try {
            UnRegisterSensorListeners();
        } catch (Exception e) {
        }
    }
}
