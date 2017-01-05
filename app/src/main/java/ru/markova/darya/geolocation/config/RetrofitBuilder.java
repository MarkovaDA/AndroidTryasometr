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
    //192.168.43.7 192.168.15.226
    private static final String URL = "http://192.168.43.7:8080/tryasometr/";

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
