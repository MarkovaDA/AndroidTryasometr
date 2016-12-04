package ru.markova.darya.geolocation.service;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


public class ShakeEventListener implements SensorEventListener{

    private String textInfo = "";
    private Float ax;
    private Float ay;
    private Float az;

    public Float getAx() {
        return ax;
    }

    public Float getAy() {
        return ay;
    }

    public Float getAz() {
        return az;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        ax = sensorEvent.values[0];
        ay = sensorEvent.values[1];
        az = sensorEvent.values[2];
        textInfo = String.format("%1$.1f\t\t%2$.1f\t\t%3$.1f", ax, ay, az);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //обработка ускорений
    }

    public interface OnShakeListener{
        void onShake();
    }

    private OnShakeListener mShakeListener;

    public void setOnShakeListener(OnShakeListener listener) {
        mShakeListener = listener;
    }

    public String getTextInfo() {
        return textInfo;
    }
}
