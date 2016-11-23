package ru.markova.darya.geolocation.service;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ru.markova.darya.geolocation.dto.LocationDTO;

public interface RetrofitDataSendService {
    @POST("/state/geo")
    Call<Object> sendLocations(@Body List<LocationDTO> locations);
}
