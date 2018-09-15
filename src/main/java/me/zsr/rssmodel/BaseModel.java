package me.zsr.rssmodel;

import java.util.List;

import me.zsr.rsscommon.LogUtil;
import me.zsr.rsscommon.ThreadManager;

public abstract class BaseModel {

    public void registerObserver(ModelObserver observer) {
        LOG_MA("registerObserver " + observer + " to " + getClass().getSimpleName());
        getObserverList().add(observer);
    }

    public void unRegisterObserver(ModelObserver observer) {
        LOG_MA("unRegisterObserver " + observer + " to " + getClass().getSimpleName());
        getObserverList().remove(observer);
    }

    public void notifyObservers(final ModelAction action, final List<?> dataList) {
        ThreadManager.post(new Runnable() {
            @Override
            public void run() {
                for (ModelObserver observer : getObserverList()) {
                    if (observer != null) {
                        observer.onDataChanged(action, dataList);
                    }
                }
            }
        });
    }

    public abstract List<ModelObserver> getObserverList();

    /**
     * Model Action
     */
    protected void LOG_MA(String action) {
        LogUtil.i(getClass().getSimpleName() + " MA : " + action);
    }
}
