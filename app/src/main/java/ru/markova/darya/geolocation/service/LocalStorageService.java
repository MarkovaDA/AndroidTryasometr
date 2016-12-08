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

    public List<GeoTableEntity> getSavedLocations(Date date){
        Query query= daoSession.queryBuilder(GeoTableEntity.class).
                where(GeoTableEntityDao.Properties.DataTime.lt(date)).build();
        return query.list();
    }

    public List<AccelerationTableEntity> getSavedAccelerations(Date date){
        Query query = daoSession.queryBuilder(AccelerationTableEntity.class).
                where(AccelerationTableEntityDao.Properties.DataTime.lt(date)).build();
        return query.list();
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
    public void deleteAccelerations(Date date){
        daoSession.queryBuilder(AccelerationTableEntity.class).
                where(AccelerationTableEntityDao.Properties.DataTime.lt(date))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }
    public void deleteLocations(Date date){
        daoSession.queryBuilder(GeoTableEntity.class).
                where(GeoTableEntityDao.Properties.DataTime.lt(date))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void destroy(){
        GreenDaoBuilder.closeSession();
    }

}
