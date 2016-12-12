package ru.markova.darya.geolocation.service;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.Query;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.markova.darya.geolocation.MainActivity;
import ru.markova.darya.geolocation.config.RetrofitBuilder;
import ru.markova.darya.geolocation.dto.LocationDTO;
import ru.markova.darya.geolocation.entity.AccelerationTableEntity;
import ru.markova.darya.geolocation.entity.DaoMaster;
import ru.markova.darya.geolocation.entity.DaoSession;
import ru.markova.darya.geolocation.entity.GeoTableEntity;

public class SendLocationToServerService extends Service{

    final String LOG_TAG = "SendDataFromDBService";
    private  final static  Long CHECK_INTERVAL = 5 * 1000L; //интервал запуска

    private Handler checkAndSendHandler = null;
    //сервис для отправки запросов на сервер
    private  RetrofitDataSendService dataSendService;
    //сервис для работы с локальной базой данных
    private LocalStorageService localStorageService;

    private Intent intent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate(){
        super.onCreate();
        intent = new Intent(MainActivity.BROADCAST_ACTION);
        localStorageService = new LocalStorageService(this);
        checkAndSendHandler = new Handler();
        dataSendService = RetrofitBuilder.getDataSendService();
    }

    public void onDestroy(){
        super.onDestroy();
        if (checkAndSendHandler != null) {
            checkAndSendHandler.removeCallbacksAndMessages(null);
            checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
        }
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Служба отправки координат запущена", Toast.LENGTH_SHORT).show();
        checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
        return super.onStartCommand(intent, flags, startId);
    }


    private Runnable dataSendRunnable = new Runnable() {
        @Override
        public void run() {
            checkAndSendHandler.removeCallbacksAndMessages(null);
            final Date currentDate = DateTimeService.getCurrentDateAndTime();
            final List<GeoTableEntity> locations = localStorageService.getSavedLocations(currentDate);
            Call<String> call = dataSendService.sendLocations(locations);

            //отправка координат на сервер
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    //успешная отправка
                    Log.d(LOG_TAG, "SENDING LOCATIONS SUCCESS...");
                    localStorageService.deleteLocations(currentDate);
                    intent.putExtra(MainActivity.STATUS_SENDING_PARAM, "locations success:" + DateTimeService.getCurrentDateAndTime());
                    sendBroadcast(intent);
                    checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    //неуспешная отправка
                    Log.d(LOG_TAG, "SENDING LOCATIONS FAILURE....");
                    intent.putExtra(MainActivity.STATUS_SENDING_PARAM, "locations fail:" + DateTimeService.getCurrentDateAndTime());
                    sendBroadcast(intent);
                    checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
                }
            });
        }
    };
}
