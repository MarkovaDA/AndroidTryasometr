package ru.markova.darya.geolocation.dto;


import java.util.Date;

public class LocationDTO {
    private Double lon;
    private Double lat;
    private String deviceImei;
    private Date dataTime;

    public LocationDTO(Double lon, Double lat, String deviceImei, Date dataTime) {
        this.lon = lon;
        this.lat = lat;
        this.deviceImei = deviceImei;
        this.dataTime = dataTime;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
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

    public LocationDTO(){}

}
