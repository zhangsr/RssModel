package me.zsr.rssmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.zsr.rssbean.Article;
import me.zsr.rssbean.ArticleDao;
import me.zsr.rssbean.Subscription;
import me.zsr.rssbean.SubscriptionDao;
import me.zsr.rsscommon.ThreadManager;

public class SubscriptionModel extends BaseModel implements ModelObserver<Article> {
    private static List<ModelObserver> mObserverList = new ArrayList<>();
    private static SubscriptionModel sInstance;

    public static SubscriptionModel getInstance() {
        if (sInstance == null) {
            sInstance = new SubscriptionModel();
            ArticleModel.getInstance().registerObserver(sInstance);
        }
        return sInstance;
    }

    public void insert(Subscription... subscriptions) {
        insert(Arrays.asList(subscriptions));
    }

    public void update(Subscription... subscriptions) {
        update(Arrays.asList(subscriptions));
    }

    public void loadAll() {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                final List<Subscription> list = DBManager.getSubscriptionDao().queryBuilder().list();
                ThreadManager.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyObservers(ModelAction.ADD, list);
                    }
                });
            }
        });
    }

    public void fetchAll() {
        final List<Subscription> list = DBManager.getSubscriptionDao().queryBuilder().list();
        for (Subscription subscription : list) {
            ArticleModel.getInstance().requestNetwork(subscription);
        }
    }

    @Override
    public List<ModelObserver> getObserverList() {
        return mObserverList;
    }

    public void insert(final List<Subscription> subscriptions) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                DBManager.getSubscriptionDao().insertOrReplaceInTx(subscriptions);
                ThreadManager.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyObservers(ModelAction.ADD, subscriptions);
                    }
                });
            }
        });
    }

    public void update(final List<Subscription> subscriptions) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                DBManager.getSubscriptionDao().updateInTx(subscriptions);
                ThreadManager.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyObservers(ModelAction.MODIFY, subscriptions);
                    }
                });
            }
        });
    }

    @Override
    public void onDataChanged(ModelAction action, List<Article> dataList) {
        switch (action) {
            // 刷新添加
            case ADD:
            // 已读修改
            case MODIFY:
                List<Long> subscriptionIdList = new ArrayList<>();
                for (Article article : dataList) {
                    long subscriptionId = article.getSubscriptionId();
                    boolean contain = false;
                    for (Long id : subscriptionIdList) {
                        if (id == subscriptionId) {
                            contain = true;
                        }
                    }
                    if (!contain) {
                        subscriptionIdList.add(subscriptionId);
                    }
                }

                List<Subscription> updatedList = new ArrayList<>();
                for (Long id : subscriptionIdList) {
                    Subscription subscription = DBManager.getSubscriptionDao().queryBuilder().where(SubscriptionDao.Properties.Id.eq(id)).unique();
                    if (subscription == null) {
                        continue;
                    }

                    final long totalCount = DBManager.getArticleDao().queryBuilder().where(
                            ArticleDao.Properties.SubscriptionId.eq(id)).count();
                    final long unreadCount = DBManager.getArticleDao().queryBuilder().where(
                            ArticleDao.Properties.SubscriptionId.eq(id),
                            ArticleDao.Properties.Read.eq(false)).count();
                    // TODO: 2018/5/19 update other params if any
                    subscription.setTotalCount(totalCount);
                    subscription.setUnreadCount(unreadCount);
                    DBManager.getSubscriptionDao().insertOrReplace(subscription);
                    updatedList.add(subscription);
                }

                notifyObservers(ModelAction.MODIFY, updatedList);
                break;
        }
    }

    public void delete(Subscription... subscriptions) {
        delete(Arrays.asList(subscriptions));
    }

    public void delete(final List<Subscription> subscriptions) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                for (Subscription subscription : subscriptions)  {
                    DBManager.getArticleDao().deleteInTx(ArticleModel.getInstance()
                            .queryBySubscriptionIdSync(subscription.getId()));
                }
                DBManager.getSubscriptionDao().deleteInTx(subscriptions);
                ThreadManager.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyObservers(ModelAction.DELETE, subscriptions);
                    }
                });
            }
        });
    }
}
