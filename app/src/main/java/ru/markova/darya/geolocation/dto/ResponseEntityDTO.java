package ru.markova.darya.geolocation.dto;


/**
 * формат чтения ответа от сервера
 */
public class ResponseEntityDTO {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ResponseEntityDTO(){}
}
