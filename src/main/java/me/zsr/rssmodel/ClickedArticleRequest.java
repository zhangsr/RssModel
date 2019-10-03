package me.zsr.rssmodel;

import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import me.zsr.rssbean.Article;
import me.zsr.rssbean.Subscription;
import me.zsr.rsscommon.DateUtil;
import me.zsr.rsscommon.StringUtil;

public class ClickedArticleRequest extends Request<String> {
    private static final String URL = "http://zhangshaoru.pythonanywhere.com/clicked_article/?format=json";
    private Article mArticle;
    private Subscription mSubscription;

    public ClickedArticleRequest(@NonNull Subscription subscription, Article article, Response.ErrorListener listener) {
        super(Method.POST, URL, listener);
        mSubscription = subscription;
        mArticle = article;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String responseStr;
        try {
            responseStr = new String(response.data, StringUtil.guessEncoding(response.data));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            responseStr = new String(response.data);
        }
        return Response.success(responseStr, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {

    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (mArticle == null) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("link", mArticle.getLink());
            jsonObject.put("title", mArticle.getTitle());
            jsonObject.put("desc", mArticle.getDescription());
            jsonObject.put("content", mArticle.getContent());
            jsonObject.put("published", mArticle.getPublished().longValue());
            jsonObject.put("iconUrl", mSubscription.getIconUrl());
            jsonObject.put("subscriptionTitle", mSubscription.getTitle());
            jsonObject.put("subscriptionUrl", mSubscription.getUrl());
            jsonObject.put("createTime", DateUtil.formatTime(System.currentTimeMillis()));
            jsonObject.put("groupId", mArticle.getGroupId());
            return jsonObject.toString().getBytes("utf-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }
}
