package ru.markova.darya.geolocation.dto;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import ru.markova.darya.geolocation.entity.GeoTableEntity;

//объект для передачи данных о локации
public class LocationDTO {
    private Double lon;
    private Double lat;
    private String deviceImei;
    private String dataTime;
    private DateFormat dateFormat;

    public LocationDTO(GeoTableEntity geo) {
        this.lon = geo.getLon();
        this.lat = geo.getLat();
        this.deviceImei = geo.getDeviceImei();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.dataTime = dateFormat.format(geo.getDataTime());
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

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public LocationDTO(){}

}
