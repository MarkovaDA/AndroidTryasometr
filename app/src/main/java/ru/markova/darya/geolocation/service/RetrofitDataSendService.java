package ru.markova.darya.geolocation.service;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ru.markova.darya.geolocation.dto.AccelerationDTO;
import ru.markova.darya.geolocation.dto.InfoDTO;
import ru.markova.darya.geolocation.dto.LocationDTO;
import ru.markova.darya.geolocation.dto.ResponseEntityDTO;

/**
 * API для отправки данных на сервер
 */
public interface RetrofitDataSendService {

    /*@POST("save_location/")
    Call<ResponseEntityDTO> sendLocations(@Body List<LocationDTO> locations);*/

    @POST("save_acceleration/")
    Call<ResponseEntityDTO> sendAccelerations(@Body List<AccelerationDTO> accelerations);

    /*@POST("save_useful/")
    Call<ResponseEntityDTO> sendUseFulInfo(@Body InfoDTO info);*/

    @POST("save_info_objects/")
    Call<ResponseEntityDTO> sendInfoObjects(@Body List<InfoDTO> info);
}
