package ru.markova.darya.geolocation.dto;

/**
 * Отрезок ямы и относительная оценка
 */

public class PitDTO {
    private Double lon1;
    private Double lat1;
    private Double lon2;
    private Double lat2;
    private Double value;//оценка

    public Double getLon1() {
        return lon1;
    }

    public void setLon1(Double lon1) {
        this.lon1 = lon1;
    }

    public Double getLat1() {
        return lat1;
    }

    public void setLat1(Double lat1) {
        this.lat1 = lat1;
    }

    public Double getLon2() {
        return lon2;
    }

    public void setLon2(Double lon2) {
        this.lon2 = lon2;
    }

    public Double getLat2() {
        return lat2;
    }

    public void setLat2(Double lat2) {
        this.lat2 = lat2;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
