package ru.markova.darya.geolocation.service;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.greendao.query.Query;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.markova.darya.geolocation.MainActivity;
import ru.markova.darya.geolocation.config.GreenDaoBuilder;
import ru.markova.darya.geolocation.config.RetrofitBuilder;
import ru.markova.darya.geolocation.dto.LocationDTO;
import ru.markova.darya.geolocation.entity.DaoSession;
import ru.markova.darya.geolocation.entity.GeoTableEntity;

public class SendDataFromDBService extends Service{

    final String LOG_TAG = "SendDataFromDBService";
    private  final static  Long CHECK_INTERVAL = 5 * 1000L; //интервал запуска

    private Handler checkAndSendHandler = null;

    //сервис для отправки запросов на сервер
    private  RetrofitDataSendService dataSendService = RetrofitBuilder.getDataSendService();

    //сервис для работы с базой данных - сейчас требует контекст, потому не работает
    private   DaoSession daoSession =  GreenDaoBuilder.getDaoSession(getApplicationContext());

    //получаем старые координаты
    private List<LocationDTO> getSavedLocations(){
        Query query = daoSession.queryBuilder(GeoTableEntity.class).build();
        List<LocationDTO> locations = query.list();
        return locations;
    }

    //очистка координат
    private void clearLocalStorage(){
        daoSession.deleteAll(GeoTableEntity.class);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate(){
        super.onCreate();
        checkAndSendHandler = new Handler();
        System.out.println("SERVICE CREATED");
    }

    public void onDestroy(){
        super.onDestroy();
        if (checkAndSendHandler != null) {
            checkAndSendHandler.removeCallbacksAndMessages(null);
            checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("SERVICE START COMMAND");
        Toast.makeText(this, "Служба запущена", Toast.LENGTH_SHORT).show();
        checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
        return super.onStartCommand(intent, flags, startId);
    }

    private Runnable dataSendRunnable = new Runnable() {
        @Override
        public void run() {
            checkAndSendHandler.removeCallbacksAndMessages(null);
            //засекать текущее время и выбирать из базы данных записи раньше этого времени
            List<LocationDTO> data = getSavedLocations();
            //Log.d(LOG_TAG, "TASK IS RUNNING...");
            System.out.println("TASK IS RUNNING");

            Call<Object> call = dataSendService.sendLocations(data);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    //успешная отправка
                    Log.d(LOG_TAG, "SENDING DATA SUCCESS...");
                    checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    //неуспешная отправка
                    //Log.d(LOG_TAG, "SENDING DATA FAILURE....");
                    System.out.println("SENDING DATA FAILURE....");
                    checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
                }
            });
        }
    };
}
