package ru.markova.darya.geolocation.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.markova.darya.geolocation.service.RetrofitDataSendService;

/**
 * конфигуратор HTTP-соединения
 */
public class RetrofitBuilder {

    private RetrofitBuilder(){}

    private static Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    private static final String URL = "http://192.168.0.100:8084/tryasometr_data_transfer/";
//    private static final String URL = "http://vps1.vistar.su:8080/tryasometr_v2/";
//    private static final String URL = "http://vps1.vistar.su:8080/TryasometrDataTransfer-1.0-SNAPSHOT/";

    private static Retrofit retrofit;

    private static RetrofitDataSendService dataSendService;

    public static RetrofitDataSendService getDataSendService(){

        if (dataSendService == null){
            gson = new GsonBuilder().create();
            retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .baseUrl(URL)
                    .build();
            dataSendService = retrofit.create(RetrofitDataSendService.class);
        }
        return dataSendService;
    }
}
