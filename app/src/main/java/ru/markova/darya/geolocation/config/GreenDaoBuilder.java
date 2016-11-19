package ru.markova.darya.geolocation.config;

import android.content.Context;
import org.greenrobot.greendao.database.Database;
import ru.markova.darya.geolocation.entity.DaoMaster;
import ru.markova.darya.geolocation.entity.DaoSession;

public class GreenDaoBuilder {

    private static DaoSession daoSession;
    public static DaoSession getDaoSession(Context context){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "tryasometr_local_storage");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        return daoSession;
    }

}
