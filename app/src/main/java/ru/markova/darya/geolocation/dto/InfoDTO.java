package ru.markova.darya.geolocation.dto;


import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ru.markova.darya.geolocation.entity.InfoTableEntity;

public class InfoDTO {
    private String dataTime;
    private String type;
    private DateFormat dateFormat;

    public InfoDTO(String dataTime, String type) {
        this.dataTime = dataTime;
        this.type = type;
    }

    public InfoDTO(InfoTableEntity entity) {
        this.type = entity.getType();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.dataTime = dateFormat.format(entity.getDataTime());
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
