package ru.markova.darya.geolocation.service;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class ShakeEventListener implements SensorEventListener{

    private String textInfo = "";

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        textInfo = String.format("%1$.1f\t\t%2$.1f\t\t%3$.1f", x, y, z);
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
