package ru.markova.darya.geolocation.service;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ru.markova.darya.geolocation.dto.LocationDTO;
import ru.markova.darya.geolocation.entity.AccelerationTableEntity;
import ru.markova.darya.geolocation.entity.GeoTableEntity;

public interface RetrofitDataSendService {
    @POST("/state/geo")
    Call<Object> sendLocations(@Body List<GeoTableEntity> locations);

    @POST("/state/accel")
    Call<Object> sendAccelerations(@Body List<AccelerationTableEntity> locations);
}
