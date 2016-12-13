package ru.markova.darya.geolocation.service;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import ru.markova.darya.geolocation.entity.AccelerationTableEntity;
import ru.markova.darya.geolocation.entity.GeoTableEntity;
import ru.markova.darya.geolocation.dto.ResponseEntity;


public interface RetrofitDataSendService {


    @POST("save_location/")
    Call<ResponseEntity> sendLocations(@Body List<GeoTableEntity> locations);

    /*@Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })*/
    @POST("save_acceleration/")
    Call<ResponseEntity> sendAccelerations(@Body List<AccelerationTableEntity> accelerations);
}
