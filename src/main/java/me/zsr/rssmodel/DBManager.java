package me.zsr.rssmodel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import me.zsr.rssbean.ArticleDao;
import me.zsr.rssbean.DaoMaster;
import me.zsr.rssbean.DaoSession;
import me.zsr.rssbean.DiscoverDao;
import me.zsr.rssbean.SubscriptionDao;

public class DBManager {
    // TODO: 2018/9/17 move biz to app
    private static final String DB_NAME = "feeder";
    private static DaoSession sDaoSession;

    public static void init(Context context) {
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context, DB_NAME, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        sDaoSession = daoMaster.newSession();
    }

    public static DiscoverDao getDiscoverDao() {
        return sDaoSession.getDiscoverDao();
    }

    public static SubscriptionDao getSubscriptionDao() {
        return sDaoSession.getSubscriptionDao();
    }

    public static ArticleDao getArticleDao() {
        return sDaoSession.getArticleDao();
    }
}
