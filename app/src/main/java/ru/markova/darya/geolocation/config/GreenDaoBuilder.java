package ru.markova.darya.geolocation.config;

import android.content.Context;
import org.greenrobot.greendao.database.Database;
import ru.markova.darya.geolocation.entity.DaoMaster;
import ru.markova.darya.geolocation.entity.DaoSession;

public class GreenDaoBuilder {

    private static DaoSession daoSession;
    private static DaoMaster.DevOpenHelper helper;

    public static DaoSession getDaoSession(Context context){

        helper = new DaoMaster.DevOpenHelper(context, "tryasometr_local_storage");
        Database db = helper.getWritableDb();//вот здесь обратить внимание на метод(?)
        daoSession = new DaoMaster(db).newSession();
        return daoSession;
    }
    public static void closeSession(){
        if (helper != null){
            helper.close();
        }
    }

}
