package me.zsr.rssmodel;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import me.zsr.rssbean.Article;
import me.zsr.rsscommon.VolleySingleton;

/**
 * author: shaoru.zsr@alibaba-inc.com
 * date: 2019-09-10
 * description:
 */
public class SInfoModel extends BaseModel implements ModelObserver<Article> {
    private static List<ModelObserver> mObserverList = new ArrayList<>();
    private static SInfoModel sInstance;

    public static SInfoModel getInstance() {
        if (sInstance == null) {
            sInstance = new SInfoModel();
        }
        return sInstance;
    }

    public void fetch() {
        SInfoListRequest request = new SInfoListRequest(
                new Response.Listener<List<Article>>() {
                    @Override
                    public void onResponse(List<Article> response) {
                        notifyObservers(ModelAction.ADD, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        VolleySingleton.getInstance().addToRequestQueue(request);

    }

    @Override
    public List<ModelObserver> getObserverList() {
        return mObserverList;
    }

    @Override
    public void onDataChanged(ModelAction action, List dataList) {

    }
}
