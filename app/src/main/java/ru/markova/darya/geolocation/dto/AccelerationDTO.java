package ru.markova.darya.geolocation.dto;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.markova.darya.geolocation.entity.AccelerationTableEntity;

//объект для передачи ускорения
public class AccelerationDTO {

    private float accelX;
    private float accelY;
    private float accelZ;
    private String deviceImei;
    private String dataTime;
    private double lat;
    private double lon;
    private DateFormat dateFormat;
    public AccelerationDTO(AccelerationTableEntity entity) {

        this.accelX = entity.getAccelX();
        this.accelY = entity.getAccelY();
        this.accelZ = entity.getAccelZ();
        this.deviceImei = entity.getDeviceImei();
        this.lat = entity.getLat();
        this.lon = entity.getLon();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        this.dataTime = dateFormat.format(entity.getDataTime());
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public float getAccelX() {
        return accelX;
    }

    public void setAccelX(float accelX) {
        this.accelX = accelX;
    }

    public float getAccelY() {
        return accelY;
    }

    public void setAccelY(float accelY) {
        this.accelY = accelY;
    }

    public float getAccelZ() {
        return accelZ;
    }

    public void setAccelZ(float accelZ) {
        this.accelZ = accelZ;
    }

    public String getDeviceImei() {
        return deviceImei;
    }

    public void setDeviceImei(String deviceImei) {
        this.deviceImei = deviceImei;
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }
}
