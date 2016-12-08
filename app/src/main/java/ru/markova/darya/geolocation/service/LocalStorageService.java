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
import ru.markova.darya.geolocation.entity.DaoMaster;
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
    public List<GeoTableEntity> getSavedLocations(Date date){
        QueryBuilder queryBuilder = daoSession.queryBuilder(GeoTableEntity.class).
                where(GeoTableEntityDao.Properties.DataTime.lt(date));
        Query query = queryBuilder.build();
        List<GeoTableEntity> list = query.list();
        queryBuilder.buildDelete().executeDeleteWithoutDetachingEntities();
        return list;
    }

    public List<AccelerationTableEntity> getSavedAccelerations(Date date){
        QueryBuilder queryBuilder = daoSession.queryBuilder(AccelerationTableEntity.class).
                where(AccelerationTableEntityDao.Properties.DataTime.lt(date));
        Query query = queryBuilder.build();
        List<AccelerationTableEntity> list = query.list();
        queryBuilder.buildDelete().executeDeleteWithoutDetachingEntities();
        return list;
    }
    public void insertLocation(GeoTableEntity entity){
        //daoSession.getGeoTableEntityDao().insert(entity);
        daoSession.insert(entity);
    }

    public void insertAcceleration(AccelerationTableEntity entity){
        //daoSession.getAccelerationTableEntityDao().insert(entity);
        daoSession.insert(entity);
    }

    public void insertLocationsBack(List<GeoTableEntity> list){
        daoSession.getGeoTableEntityDao().insertInTx(list);
    }
    public void insertAccelerationsBack(List<AccelerationTableEntity> list){
        daoSession.getAccelerationTableEntityDao().insertInTx(list);
    }
    public void destroy(){
        GreenDaoBuilder.closeSession();
    }

}
