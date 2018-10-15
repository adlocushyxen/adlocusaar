package com.hyxen.adlocusaar.repository.remote;

import android.content.Context;
import android.os.Build;

import com.hyxen.adlocusaar.BuildConfig;
import com.hyxen.adlocusaar.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

public abstract class RemoteAPI {
    private static final String TAG = RemoteAPI.class.getSimpleName();

    private static final int MAX_IDLE_CONNECTIONS = 8;
    private static final int KEEP_ALIVE_DURATION = 300;
    private static final int CONNECTION_TIMEOUT = 15;
    private static final int READ_TIMEOUT = 15;
    private static final int WRITE_TIMEOUT = 15;

    protected static WeakReference<Context> sContextRef;
    private static OkHttpClient mOkHttpClient;

    /*--------------------------------------------------------------------------------------------*/
    /* Helpers */
    public static void init(Context context) {
        sContextRef = new WeakReference<>(context);
    }

    private static OkHttpClient createClient() {
//        Context context = sContextRef.get();

        ConnectionPool connectionPool = new ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_DURATION, TimeUnit.SECONDS);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectionPool(connectionPool)
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addNetworkInterceptor(new UserAgentInterceptor(sContextRef.get()));
//        if (context != null) {
//            builder.addNetworkInterceptor(new SessionInterceptor(context));
//        }
        return enableTLS12OnPreLollipop(builder).build();
    }

    public static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            mOkHttpClient = createClient();
        }
        return mOkHttpClient;
    }

    public static OkHttpClient.Builder enableTLS12OnPreLollipop(OkHttpClient.Builder builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            try {
                builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT));
            } catch (Exception e) {
                Logger.e(TAG, "[enableTLS12OnPreLollipop] Error while setting TLS 1.2", e);
            }
        }

        return builder;
    }
}
