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
import ru.markova.darya.geolocation.entity.InfoTableEntity;
import ru.markova.darya.geolocation.entity.InfoTableEntityDao;

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
    //извлечь список локально сохраненных локаций
    public List<GeoTableEntity> getSavedLocations(Date date){
        Query query= daoSession.queryBuilder(GeoTableEntity.class).
                where(GeoTableEntityDao.Properties.DataTime.le(date)).build();
        return query.list();
    }
    //извлечь список локально сохраненных ускорений - ограничение 50 отсчетов
    public List<AccelerationTableEntity> getSavedAccelerations(Date date){
        Query query = daoSession.queryBuilder(AccelerationTableEntity.class).
                where(AccelerationTableEntityDao.Properties.DataTime.le(date))
                .limit(10) //потом исправить!!!
                .build();
        return query.list();
    }
    //извлечь список локально сохраненных объектов информации
    public List<InfoTableEntity> getSavedInfoObjects(Date date){
        Query query = daoSession.queryBuilder(InfoTableEntity.class)
                .where(InfoTableEntityDao.Properties.DataTime.le(date))
                .build();
        return query.list();
    }
    //сохранение локейшена
    public void insertLocation(GeoTableEntity entity){
        daoSession.insert(entity);
    }
    //сохранение информации
    public void insertInfo(InfoTableEntity entity){
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
                where(AccelerationTableEntityDao.Properties.DataTime.le(date))
                .limit(20)
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }
    //удаление объектов информации
    public void deleteInfoObjects(Date date){
        daoSession.queryBuilder(InfoTableEntity.class)
                .where(InfoTableEntityDao.Properties.DataTime.le(date))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }
    //удаление локайшенов
    public void deleteLocations(Date date){
        daoSession.queryBuilder(GeoTableEntity.class).
                where(GeoTableEntityDao.Properties.DataTime.le(date))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void destroy(){
        GreenDaoBuilder.closeSession(daoSession);
    }

}
