package com.hyxen.adlocusaar.repository.remote;

import android.content.Context;
import android.webkit.WebSettings;

import com.hyxen.adlocusaar.utils.Logger;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Define custom user-agent.
 */
public class UserAgentInterceptor implements Interceptor {
    private static final String TAG = UserAgentInterceptor.class.getSimpleName();

    private String mUserAgent;
    private Context context=null;

    public UserAgentInterceptor(Context context) {
        this.context=context;
//        mUserAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        long t1 = System.nanoTime();
        Request newRequest=null;
        Response response=null;
        if(context==null){
            newRequest = chain.request();
            response = chain.proceed(chain.request());
        }else{
            newRequest = chain.request()
                    .newBuilder()
                    .removeHeader("User-Agent")//移除旧的
                    .addHeader("User-Agent", WebSettings.getDefaultUserAgent(context))//添加真正的头部
                    .build();
            response = chain.proceed(newRequest);
        }


        long t2 = System.nanoTime();

        Logger.d(TAG, String.format("Received response from (%s) %s in %.1fms",
                newRequest.method(),
                newRequest.url(),
                (t2 - t1) / 1e6d));
        return response;
    }
}
