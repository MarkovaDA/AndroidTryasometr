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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.markova.darya.geolocation.MainActivity;
import ru.markova.darya.geolocation.config.RetrofitBuilder;
import ru.markova.darya.geolocation.dto.LocationDTO;
import ru.markova.darya.geolocation.entity.DaoMaster;
import ru.markova.darya.geolocation.entity.DaoSession;
import ru.markova.darya.geolocation.entity.GeoTableEntity;
import java.util.Date;

public class SendDataFromDBService extends Service{

    final String LOG_TAG = "SendDataFromDBService";
    private  final static  Long CHECK_INTERVAL = 5 * 1000L; //интервал запуска

    private Handler checkAndSendHandler = null;
    private DaoMaster.DevOpenHelper helper;
    //сервис для отправки запросов на сервер
    private  RetrofitDataSendService dataSendService;

    private  DaoSession daoSession;

    //получаем старые координаты
    private List<LocationDTO> getSavedLocations(){

        Query query = daoSession.queryBuilder(GeoTableEntity.class).build();
        List<LocationDTO> locations = query.list();
        return locations;
    }

    private Intent intent;

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
        intent = new Intent(MainActivity.BROADCAST_ACTION);
        //создаем новую сессию для работы с бд -
        helper = new DaoMaster.DevOpenHelper(this, "tryasometr_local_storage");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
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
        Toast.makeText(this, "Служба запущена", Toast.LENGTH_SHORT).show();
        checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
        return super.onStartCommand(intent, flags, startId);
    }

    //продумать - выполнить отправку, в случае неуспеха вернуть данные назад, при отправке подчищать
    private Runnable dataSendRunnable = new Runnable() {
        @Override
        public void run() {
            checkAndSendHandler.removeCallbacksAndMessages(null);
            //засекать текущее время и выбирать из базы данных записи раньше этого времени

            List<LocationDTO> data = getSavedLocations();
            //Log.d(LOG_TAG, "TASK IS RUNNING...");
            Call<Object> call = dataSendService.sendLocations(data);
            //выбирать еще и сохранять периодически значения ускорений
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    //успешная отправка
                    Log.d(LOG_TAG, "SENDING DATA SUCCESS...");
                    intent.putExtra(MainActivity.STATUS_SENDING_PARAM, "sending success:" + DateTimeService.getCurrentDateAndTime());
                    sendBroadcast(intent);
                    checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    //неуспешная отправка

                    Log.d(LOG_TAG, "SENDING DATA FAILURE....");
                    intent.putExtra(MainActivity.STATUS_SENDING_PARAM, "sending fail:" + DateTimeService.getCurrentDateAndTime());
                    sendBroadcast(intent);
                    checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
                    //попробовать здесь изменить статус отправки
                }
            });
        }
    };
}
