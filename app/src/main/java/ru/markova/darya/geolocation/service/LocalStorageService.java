package ru.markova.darya.geolocation.service;


import android.content.Context;

import org.greenrobot.greendao.query.Query;

import java.util.Date;
import java.util.List;

import ru.markova.darya.geolocation.config.GreenDaoBuilder;
import ru.markova.darya.geolocation.entity.AccelerationTableEntity;
import ru.markova.darya.geolocation.entity.AccelerationTableEntityDao;
import ru.markova.darya.geolocation.entity.DaoSession;
import ru.markova.darya.geolocation.entity.GeoTableEntity;
import ru.markova.darya.geolocation.entity.GeoTableEntityDao;

/**
 * методы для работы с базой данных
 */
public class LocalStorageService {

    private Context context;
    private DaoSession daoSession;

    public LocalStorageService(Context context){
        this.context = context;
        daoSession = GreenDaoBuilder.getDaoSession(context);
    }
    //извлечь список сохраненных локаций
    public List<GeoTableEntity> getSavedLocations(Date date){
        Query query= daoSession.queryBuilder(GeoTableEntity.class).
                where(GeoTableEntityDao.Properties.DataTime.lt(date)).build();
        return query.list();
    }
    //извлечь список сохраненных ускорений
    public List<AccelerationTableEntity> getSavedAccelerations(Date date){
        Query query = daoSession.queryBuilder(AccelerationTableEntity.class).
                where(AccelerationTableEntityDao.Properties.DataTime.lt(date)).build();
        return query.list();
    }

    //сохранение локейшена
    public void insertLocation(GeoTableEntity entity){
        //daoSession.getGeoTableEntityDao().insert(entity);
        daoSession.insert(entity);
    }
    //сохранение ускорения
    public void insertAcceleration(AccelerationTableEntity entity){
        //daoSession.getAccelerationTableEntityDao().insert(entity);
        daoSession.insert(entity);
    }
    //сохранение списка локаций
    public void insertLocationsBack(List<GeoTableEntity> list){
        daoSession.getGeoTableEntityDao().insertInTx(list);
    }
    //сохранение списка ускорений
    public void insertAccelerationsBack(List<AccelerationTableEntity> list){
        daoSession.getAccelerationTableEntityDao().insertInTx(list);
    }
    //удаление ускорений
    public void deleteAccelerations(Date date){
        daoSession.queryBuilder(AccelerationTableEntity.class).
                where(AccelerationTableEntityDao.Properties.DataTime.lt(date))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }
    //удаление локайшенов
    public void deleteLocations(Date date){
        daoSession.queryBuilder(GeoTableEntity.class).
                where(GeoTableEntityDao.Properties.DataTime.lt(date))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void destroy(){
        GreenDaoBuilder.closeSession();
    }

}
