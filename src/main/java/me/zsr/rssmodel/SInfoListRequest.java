package me.zsr.rssmodel;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import me.zsr.rssbean.Article;
import me.zsr.rsscommon.StringUtil;
import me.zsr.rsscommon.VolleySingleton;

public class SInfoListRequest extends Request<List<Article>> {
    private static final String TAG = SInfoListRequest.class.getSimpleName();
    private final Response.Listener<List<Article>> mListener;
    private static final String URL = "http://zhangshaoru.pythonanywhere.com/article_flow/?format=json";

    public SInfoListRequest(Response.Listener<List<Article>> mListener, Response.ErrorListener listener) {
        super(Method.GET, URL, listener);
        this.mListener = mListener;
    }

    @Override
    protected Response<List<Article>> parseNetworkResponse(NetworkResponse response) {
        String responseStr;
        try {
            responseStr = new String(response.data, StringUtil.guessEncoding(response.data));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            responseStr = new String(response.data);
        }
        List<Article> articleList = parseArticle(responseStr);
        if (articleList == null || articleList.size() == 0) {
            return Response.error(new VolleyError("Parse result an empty article list"));
        }

        return Response.success(articleList, HttpHeaderParser.parseCacheHeaders(response));
    }

    private List<Article> parseArticle(String dataStr) {
        List<Article> articleList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(dataStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                Article article = new Article();
                JSONObject object = jsonArray.getJSONObject(i);
                article.setLink(object.optString("link"));
                article.setTitle(object.optString("title"));
                article.setDescription(object.optString("desc"));
                article.setContent(object.optString("content"));
                article.setPublished(object.optLong("published"));
                article.setIconUrl(object.optString("iconUrl"));
                article.setSubscriptionTitle(object.optString("subscriptionTitle"));
                article.setSubscriptionUrl(object.optString("subscriptionUrl"));
                article.setCreateTime(object.optString("createTime"));
                article.setGroupId(object.optString("groupId"));
                article.setType(Article.TYPE_SINFO);
                articleList.add(article);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return articleList;
    }

    @Override
    protected void deliverResponse(List<Article> response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    @Override
    public void deliverError(VolleyError error) {
        Log.d(TAG, "deliverError " + error);

        if (error == null || error.networkResponse == null) {
            return;
        }

        final int status = error.networkResponse.statusCode;
        // Handle 30x
        if (HttpURLConnection.HTTP_MOVED_PERM == status
                || status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_SEE_OTHER) {

            final String location = error.networkResponse.headers.get("Location");
            Log.d(TAG, "Location: " + location);
            SInfoListRequest request = new SInfoListRequest(mListener, getErrorListener());
            // Construct a request clone and change the url to redirect location.
            VolleySingleton.getInstance().addToRequestQueue(request);
        }
    }
}
