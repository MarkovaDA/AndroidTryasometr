package ru.markova.darya.geolocation.service;


import android.content.Context;
import android.location.Location;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.rx.RxTransaction;

import java.util.Date;
import java.util.List;

import ru.markova.darya.geolocation.config.GreenDaoBuilder;
import ru.markova.darya.geolocation.dto.LocationDTO;
import ru.markova.darya.geolocation.entity.AccelerationTableEntity;
import ru.markova.darya.geolocation.entity.AccelerationTableEntityDao;
import ru.markova.darya.geolocation.entity.DaoSession;
import ru.markova.darya.geolocation.entity.GeoTableEntity;
import ru.markova.darya.geolocation.entity.GeoTableEntityDao;

public class LocalStorageService {

    private Context context;
    private DaoSession daoSession;

    public LocalStorageService(Context context){
        this.context = context;
        daoSession = GreenDaoBuilder.getDaoSession(context);
    }

    //сохраняем данные о местоположении
    public void saveLocation(Location location, String deviceIMEI){
        GeoTableEntity entity = new GeoTableEntity();
        entity.setLon(location.getLongitude());
        entity.setLat(location.getLatitude());
        entity.setDataTime(DateTimeService.getDateAndTime(location));
        entity.setDeviceImei(deviceIMEI);
        daoSession.insert(entity);
    }

    //сохраняем в базу данных ускорения
    public void saveAcceleration(float[] accelerations, String deviceIMEI){
        AccelerationTableEntity entity = new AccelerationTableEntity();
        entity.setAccelX(accelerations[0]);
        entity.setAccelY(accelerations[1]);
        entity.setAccelZ(accelerations[2]);
        entity.setDeviceImei(deviceIMEI);
        entity.setDataTime(DateTimeService.getCurrentDateAndTime());
        daoSession.insert(entity);
    }
    //AccelerationTableEntityDao.Properties.DataTime.lt(date)
    //получаем старые координаты до определённого момента времени
    public List<LocationDTO> getSavedLocations(Date date){
        QueryBuilder queryBuilder = daoSession.queryBuilder(GeoTableEntity.class).
                where(GeoTableEntityDao.Properties.DataTime.lt(date));
                //.build();
        Query query = queryBuilder.build();
        List<LocationDTO> list = query.list();
        queryBuilder.buildDelete().executeDeleteWithoutDetachingEntities();
        //List<LocationDTO> _list = (queryBuilder.build()).list(); проверка удаления
        return list;
    }
    //вставить набор данных list<LocationDTO> из листа

    //извлекаем данные об ускорениях
    public void destroy(){
        GreenDaoBuilder.closeSession();
    }

}
