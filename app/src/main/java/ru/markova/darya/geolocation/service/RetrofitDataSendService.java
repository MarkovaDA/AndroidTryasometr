package ru.markova.darya.geolocation.service;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ru.markova.darya.geolocation.dto.AccelerationDTO;
import ru.markova.darya.geolocation.dto.InfoDTO;
import ru.markova.darya.geolocation.dto.LocationDTO;
import ru.markova.darya.geolocation.dto.PitDTO;
import ru.markova.darya.geolocation.dto.ResponseEntityDTO;

/**
 * API для отправки данных на сервер
 */
public interface RetrofitDataSendService {

    @POST("save_acceleration/")
    Call<ResponseEntityDTO> sendAccelerations(@Body List<AccelerationDTO> accelerations);

    @POST("save_info_objects/")
    Call<ResponseEntityDTO> sendInfoObjects(@Body List<InfoDTO> info);

    @POST("mark_pit_interval/")
    Call<ResponseEntityDTO> markPitInterval(@Body List<PitDTO> pit);
}
