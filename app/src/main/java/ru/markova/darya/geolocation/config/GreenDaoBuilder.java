package ru.markova.darya.geolocation.config;

import android.content.Context;
import org.greenrobot.greendao.database.Database;
import ru.markova.darya.geolocation.entity.DaoMaster;
import ru.markova.darya.geolocation.entity.DaoSession;

/**
 * конфигуратор cоединения к локальной базе данных
 */
public class GreenDaoBuilder {

    private static DaoMaster.DevOpenHelper helper;

    public static DaoSession getDaoSession(Context context){
        if (helper == null) {
            helper = new DaoMaster.DevOpenHelper(context, "tryasometr_storage");
        }
        Database database = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(database).newSession();
        return daoSession;
    }

    public static void closeSession(DaoSession session){
        session.clear();
        //database.close();
    }
}
