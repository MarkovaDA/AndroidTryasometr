package ru.markova.darya.geolocation.service;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import ru.markova.darya.geolocation.dto.LocationDTO;

public interface DataSendService {
    //@FormUrlEncoded
    @POST("/state/geo")
    Call<Object> sendCoordinate(@Body LocationDTO location);
}