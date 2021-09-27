package com.androsoft.heschecker.Utils;

import android.app.Activity;
import android.content.pm.PackageManager;

import java.util.HashMap;

import okhttp3.Headers;
import okhttp3.Response;

public class RequestNetwork {
    private HashMap<String, Object> params = new HashMap<>();
    private HashMap<String, Object> params2 = new HashMap<>();
    private HashMap<String, Object> headers = new HashMap<>();

    private Activity activity;

    private int requestType = 0;

    public RequestNetwork(Activity activity) {
        this.activity = activity;
    }

    public void setHeaders(HashMap<String, Object> headers) {
        this.headers = headers;
    }

    public void setParams(HashMap<String, Object> params, int requestType) {
        this.params = params;
        this.requestType = requestType;
    }

    public void setParams2(String params2, int requestType) {
        this.params2 = params;
        this.requestType = requestType;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public HashMap<String, Object> getHeaders() {
        return headers;
    }

    public Activity getActivity() {
        return activity;
    }

    public int getRequestType() {
        return requestType;
    }

    public void startRequestNetwork(String method, String url, String tag, RequestListener requestListener) {
        RequestNetworkController.getInstance().execute(this, method, url, tag, requestListener, null);
    }

    public void startRequestNetworkOkHTTP(String method, String url, RequestListenerDetail requestListenerDetail) {
        RequestNetworkController.getInstance().execute2(this, method, url, requestListenerDetail, null);
    }

    public void startRequestNetwork2(String method, String url, String tag, RequestListener requestListener, HeaderListener headerListener) {
        RequestNetworkController.getInstance().execute(this, method, url, tag, requestListener, headerListener);
    }

    public interface HeaderListener {
        public void onHeadersReceived(Headers headers);
    }

    public interface RequestListener {
        public void onResponse(String tag, String response);
        public void onErrorResponse(String tag, String message);
    }
    public interface RequestListenerDetail {
        public void onResponse(String response, Response responseOkHttp);
        public void onErrorResponse(String tag, String message);
    }

}