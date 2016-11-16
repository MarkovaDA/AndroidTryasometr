package ru.markova.darya.geolocation.dto;


public class LocationDTO {
    private Double longitude;
    private Double latitude;
    private String IMEI;

    public LocationDTO(){}

    public LocationDTO(Double longitude, Double latitude, String IMEI) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.IMEI = IMEI;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }
}
