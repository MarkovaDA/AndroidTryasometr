package ru.markova.darya.geolocation.service;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ru.markova.darya.geolocation.dto.AccelerationDTO;
import ru.markova.darya.geolocation.dto.LocationDTO;
import ru.markova.darya.geolocation.dto.ResponseEntityDTO;

/*отправка данных на сервер*/
public interface RetrofitDataSendService {

    @POST("save_location/")
    Call<ResponseEntityDTO> sendLocations(@Body List<LocationDTO> locations);


    @POST("save_acceleration/")
    Call<ResponseEntityDTO> sendAccelerations(@Body List<AccelerationDTO> accelerations);
}
