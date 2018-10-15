package com.hyxen.adlocusaar.utils;

import android.util.Log;

/**
 * A wrapped Log class.
 */
public class Logger {
    private static final String TAG = "Logger";
    private static int sDebugLevel = Log.VERBOSE;

    public static void setDebugLevel(int debugLevel) {
        sDebugLevel = debugLevel;
    }

    public static int getDebugLevel() {
        return sDebugLevel;
    }

    public static void v(String tag, String msg) {
        if (sDebugLevel <= Log.VERBOSE) {
            Log.v(TAG, String.format("[%s] %s", tag, msg));
        }
    }

    public static void v(String tag, String msg, Throwable e) {
        if (sDebugLevel <= Log.VERBOSE) {
            Log.v(TAG, String.format("[%s] %s", tag, msg), e);
        }
    }

    public static void d(String tag, String msg) {
        if (sDebugLevel <= Log.DEBUG) {
            Log.d(TAG, String.format("[%s] %s", tag, msg));
        }
    }

    public static void d(String tag, Object... args) {
        if (sDebugLevel <= Log.DEBUG) {
            StringBuilder a = new StringBuilder(100);
            if (null != args)
            {
                int n = args.length;
                for (int i = 0; i < n; i++)
                {
                    a.append(args[i]);
                }
            }
            Log.d(TAG, String.format("[%s] %s", tag, a.toString()));
        }
    }

    public static void d(String tag, String msg, Throwable e) {
        if (sDebugLevel <= Log.DEBUG) {
            Log.d(TAG, String.format("[%s] %s", tag, msg), e);
        }
    }

    public static void i(String tag, String msg) {
        if (sDebugLevel <= Log.INFO) {
            Log.i(TAG, String.format("[%s] %s", tag, msg));
        }
    }

    public static void i(String tag, String msg, Throwable e) {
        if (sDebugLevel <= Log.INFO) {
            Log.i(TAG, String.format("[%s] %s", tag, msg), e);
        }
    }

    public static void w(String tag, String msg) {
        if (sDebugLevel <= Log.WARN) {
            Log.w(TAG, String.format("[%s] %s", tag, msg));
        }
    }

    public static void w(String tag, String msg, Throwable e) {
        if (sDebugLevel <= Log.WARN) {
            Log.w(TAG, String.format("[%s] %s", tag, msg), e);
        }
    }

    public static void e(String tag, String msg) {
        if (sDebugLevel <= Log.ERROR) {
            Log.e(TAG, String.format("[%s] %s", tag, msg));
        }
    }

    public static void e(String tag, String msg, Throwable e) {
        if (sDebugLevel <= Log.ERROR) {
            Log.e(TAG, String.format("[%s] %s", tag, msg), e);
        }
    }
}