package com.openapi.comm.utils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpManager {
    public static final String TAG = HttpManager.class.getSimpleName();
    public static HttpManager sInstance = new HttpManager();
    private static final int TIME_OUT_S = 15;
    private OkHttpClient mClient = null;

    public static class RequestCallBack {
        final void handleError(int code, String errMsg) {
            onError(code, errMsg);
            onFinish();
        }

        final void handleSuccess(int code, String bodyString) {
            onSuccess(code, bodyString);
            onFinish();
        }

        public void onFinish() {

        }

        public void onError(int code, String errMsg) {

        }

        public void onSuccess(int code, String bodyString) {

        }
    }

    private HttpManager() {
        mClient = createClient();
    }

    public static HttpManager getInstance() {
        return sInstance;
    }

    public void request(String url, final RequestCallBack callBack) {
       Request.Builder reqBuilder = new Request.Builder();
       reqBuilder.url(url);
       Request req = reqBuilder.build();
       Call call = proxy().newCall(req);
       call.enqueue(new Callback() {
           @Override
           public void onFailure(@NonNull Call call, @NonNull IOException e) {
               LogUtil.e(TAG, "request error:" + e);
               if (callBack != null) {
                   e.printStackTrace();
                   callBack.handleError(-1, "" + e);
               }
           }

           @Override
           public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LogUtil.e(TAG, "request ok:" + response.code());
                if (callBack != null) {
                    callBack.handleSuccess(response.code(), response.body().string());
                }
           }
       });
    }

    public void cancel() {

    }

    private OkHttpClient createClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(TIME_OUT_S, TimeUnit.SECONDS).
                writeTimeout(TIME_OUT_S, TimeUnit.SECONDS).
                readTimeout(TIME_OUT_S, TimeUnit.SECONDS);
        return builder.build();
    }

    private OkHttpClient proxy() {
        return mClient;
    }
}
