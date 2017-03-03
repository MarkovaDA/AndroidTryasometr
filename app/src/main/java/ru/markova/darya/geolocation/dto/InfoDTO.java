package ru.markova.darya.geolocation.dto;



public class InfoDTO {
    private String dataTime;
    private String type;

    public InfoDTO(String dataTime, String type) {
        this.dataTime = dataTime;
        this.type = type;
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
