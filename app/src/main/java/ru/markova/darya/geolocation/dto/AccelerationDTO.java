package ru.markova.darya.geolocation.dto;

import java.util.Date;

/**
 * Created by darya on 07.12.16.
 */

public class AccelerationDTO {

    private Long id;
    private float accelX;
    private float accelY;
    private float accelZ;
    private String deviceImei;
    private Date dataTime;

    public AccelerationDTO(Long id, float accelX, float accelY, float accelZ, String deviceImei, Date dataTime) {
        this.id = id;
        this.accelX = accelX;
        this.accelY = accelY;
        this.accelZ = accelZ;
        this.deviceImei = deviceImei;
        this.dataTime = dataTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getDataTime() {
        return dataTime;
    }

    public void setDataTime(Date dataTime) {
        this.dataTime = dataTime;
    }
}
