package me.zsr.rssmodel;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import me.zsr.rssbean.Subscription;
import me.zsr.rsscommon.LogUtil;
import me.zsr.rsscommon.StringUtil;
import me.zsr.rsscommon.VolleySingleton;

public class SubscriptionRequest extends Request<Subscription> {
    private static final String TAG = SubscriptionRequest.class.getSimpleName();
    private final Response.Listener<Subscription> mListener;

    public SubscriptionRequest(String url, Response.Listener<Subscription> mListener, Response.ErrorListener errorListener) {
        this(Method.GET, url, errorListener, mListener);
    }

    private SubscriptionRequest(int method, String url, Response.ErrorListener listener, Response.Listener<Subscription> mListener) {
        super(method, url, listener);
        this.mListener = mListener;
    }

    @Override
    protected Response<Subscription> parseNetworkResponse(NetworkResponse response) {
        String responseStr;
        try {
            responseStr = new String(response.data, StringUtil.guessEncoding(response.data));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            responseStr = new String(response.data);
        }
        Subscription subscription = SubscriptionParser.parse(responseStr);
        if (subscription == null) {
            return Response.error(new VolleyError("Parse result an empty subscription"));
        }
        return Response.success(subscription, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(Subscription response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    @Override
    public void deliverError(VolleyError error) {

        LogUtil.e(TAG, "deliverError " + error);

        if (error == null || error.networkResponse == null) {
            super.deliverError(error);
            return;
        }

        final int status = error.networkResponse.statusCode;
        // Handle 30x
        if (HttpURLConnection.HTTP_MOVED_PERM == status
                || status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_SEE_OTHER) {

            final String location = error.networkResponse.headers.get("Location");
            LogUtil.e(TAG, "Location: " + location);
            SubscriptionRequest request = new SubscriptionRequest(location, mListener, getErrorListener());
            // Construct a request clone and change the url to redirect location.
            VolleySingleton.getInstance().addToRequestQueue(request);
            return;
        }
        super.deliverError(error);
    }
}
