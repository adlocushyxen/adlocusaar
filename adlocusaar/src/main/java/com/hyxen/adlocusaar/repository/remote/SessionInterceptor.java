package com.hyxen.adlocusaar.repository.remote;


import android.content.Context;
import android.text.TextUtils;

import com.hyxen.adlocusaar.utils.MiscUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Bring user session in each request.
 */
public class SessionInterceptor implements Interceptor {

    public static final String DEFAULT_SESSION_HEADER = "X-SESSION";

    private WeakReference<Context> mContextRef;
    private String mHeaderKey;

    public SessionInterceptor(Context context) {
        this(context, DEFAULT_SESSION_HEADER);
    }

    public SessionInterceptor(Context context, String key) {
        MiscUtils.checkNotNull(context, "context cannot be null.");

        mContextRef = new WeakReference<>(context);
        mHeaderKey = key;

        if (TextUtils.isEmpty(mHeaderKey)) {
            mHeaderKey = DEFAULT_SESSION_HEADER;
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String session = getSession(originalRequest);

        Request newRequest = originalRequest.newBuilder()
                .removeHeader(mHeaderKey)
                .addHeader(mHeaderKey, session)
                .build();

        return chain.proceed(newRequest);
    }

    private String getSession(Request req) {
//        Context context = mContextRef.get();
//        if (context == null) {
//            return "";
//        }
//        try {
//            Config config = Repository.getConfig().blockingGet();
//            if (config == null || TextUtils.isEmpty(config.getSess()))
//                return "";
//            return config.getSess();
//        } catch (Exception ex) {
//            return "";
//        }
        return "";
    }
}
