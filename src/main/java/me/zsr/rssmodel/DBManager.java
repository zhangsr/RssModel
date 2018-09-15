package me.zsr.rssmodel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
    private static final String DB_NAME = "rss";
    private static DaoSession sDaoSession;

    public static void init(Context context) {
        DaoMaster.OpenHelper helper = new DaoMaster.OpenHelper(context, DB_NAME, null){};
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
