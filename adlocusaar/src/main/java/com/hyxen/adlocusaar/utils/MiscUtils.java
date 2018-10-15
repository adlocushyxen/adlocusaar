package com.hyxen.adlocusaar.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;

import com.bluelinelabs.logansquare.LoganSquare;
import com.hyxen.adlocusaar.R;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * Some useful utilities.
 */
public class MiscUtils {
    private static final String TAG = MiscUtils.class.getSimpleName();

    /**
     * Serialize the passing object to a json string.
     *
     * @param obj
     * @return json string, or empty string if failed.
     */
    public static String toJSONString(Object obj) {
        String result = "";
        if (obj == null) {
            return result;
        }

        try {
            result = LoganSquare.serialize(obj);
        } catch (Exception e) {
            Logger.e(TAG, "Fail to serialize object! object = " + obj.getClass().getSimpleName(), e);
        }

        return result;
    }

    /**
     * Parse the passing json string to an object.
     * This method will try to create an instance of return type if parsing failed.
     *
     * @param jsonStr
     * @param claz
     * @param <T>
     * @return json object, or null if failed.
     * @see {@link MiscUtils#newInstance(Class)}
     */
    public static <T> T parseJSON(String jsonStr, Class<T> claz) {
        T result = null;

        try {
            result = LoganSquare.parse(jsonStr, claz);
        } catch (Exception e) {
            Logger.e(TAG, "Fail to parse json! Message = " + e.getMessage());
        }

        if (result == null) {
            result = newInstance(claz);
        }

        return result;
    }

    /**
     * Parse the json string to a list.
     * This method will try to create an empty list if it fail.
     *
     * @param jsonStr
     * @param claz
     * @param <T>
     * @return
     */
    public static <T> List<T> parseJSONList(String jsonStr, Class<T> claz) {
        if (TextUtils.isEmpty(jsonStr) || claz == null) {
            return new ArrayList<>();
        }

        List<T> result = null;
        try {
            result = LoganSquare.parseList(jsonStr, claz);
        } catch (Exception e) {
            Logger.w(TAG, "Fail to parse json list! Message = " + e.getMessage());
        }

        if (result == null) {
            result = new ArrayList<>();
        }

        return result;
    }

    /**
     * Parse the json string to a map.
     * This method will try to create an empty map if it fail.
     *
     * @param jsonStr
     * @param claz
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> parseJSONMap(String jsonStr, Class<T> claz) {
        if (TextUtils.isEmpty(jsonStr) || claz == null) {
            return new HashMap<>();
        }

        Map<String, T> result = null;
        try {
            result = LoganSquare.parseMap(jsonStr, claz);
        } catch (Exception e) {
            Logger.w(TAG, "Fail to parse json map! Message = " + e.getMessage());
        }

        if (result == null) {
            return new HashMap<>();
        }

        return result;
    }

    /**
     * Create an instance of passing type.
     *
     * @param claz
     * @param <T>
     * @return new instance, or null if failed.
     * @see {@link Class#newInstance()}
     */
    public static <T> T newInstance(Class<T> claz) {
        if (claz == null) {
            return null;
        }

        T result = null;
        try {
            result = claz.newInstance();
        } catch (Exception e) {
            Logger.e(TAG, "Fail to create an instance for given class! class = " + claz.getSimpleName(), e);
        }

        return result;
    }

    /**
     * Throw NullPointerException if the passing object is null.
     *
     * @param t
     * @param message
     * @param <T>
     * @return
     */
    public static <T> T checkNotNull(T t, String message) {
        if (t == null) {
            if (TextUtils.isEmpty(message)) {
                message = "checkNotNull()";
            }

            throw new NullPointerException(message);
        }

        return t;
    }

    /**
     * Throw NullPointerException if the passing array is either null or empty.
     *
     * @param arr
     * @param message
     * @param <T>
     * @return
     */
    public static <T> T[] checkNotEmpty(T[] arr, String message) {
        if (arr == null || arr.length == 0) {
            if (TextUtils.isEmpty(message)) {
                message = "checkNotEmpty()";
            }

            throw new NullPointerException(message);
        }

        return arr;
    }

    /**
     * Throw NullPointerException if the passing string is either null or empty.
     *
     * @param s
     * @param message
     * @return
     */
    public static String checkNotEmpty(String s, String message) {
        if (TextUtils.isEmpty(s)) {
            if (TextUtils.isEmpty(message)) {
                message = "checkNotEmpty()";
            }

            throw new NullPointerException(message);
        }

        return s;
    }

    /**
     * Guarantee that the object would cast correctly.
     * IllegalArgumentException will be threw out under below situations:
     * 1. object is null
     * 2. cast to wrong type
     *
     * @param object
     * @param <T>
     * @return
     */
    @NonNull
    public static <T> T perfectCast(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot cast null object.");
        }

        try {
            return (T) object;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cast failed.");
        }
    }

    /**
     * Parse string to int safely.
     *
     * @param val
     * @return
     */
    public static int toInt(String val, int defaultVal) {
        if (TextUtils.isEmpty(val)) {
            Logger.w(TAG, "[toInt] value is empty");
            return defaultVal;
        }

        int result = defaultVal;

        try {
            result = Integer.valueOf(val);
        } catch (NumberFormatException e) {
            Logger.w(TAG, "Cannot parse string to int. " + String.valueOf(val));
        }

        return result;
    }

    /**
     * Parse string to float safely.
     *
     * @param val
     * @param defaultVal
     * @return
     */
    public static float toFloat(String val, float defaultVal) {
        if (TextUtils.isEmpty(val)) {
            Logger.w(TAG, "[toFloat] value is empty");
            return defaultVal;
        }

        float result = defaultVal;

        try {
            result = Float.valueOf(val);
        } catch (NumberFormatException e) {
            Logger.w(TAG, "Cannot parse string to float. " + String.valueOf(val));
        }

        return result;
    }

    /**
     * Parse string to double safely
     *
     * @param val
     * @param defaultVal
     * @return
     */
    public static double toDouble(String val, double defaultVal) {
        if (TextUtils.isEmpty(val)) {
            Logger.w(TAG, "[double] value is empty");
            return defaultVal;
        }

        double result = defaultVal;

        try {
            result = Double.parseDouble(val);
        } catch (NumberFormatException e) {
            Logger.w(TAG, "Cannot parse string to double. " + String.valueOf(val));
        }

        return result;
    }

    public static boolean toBoolean(String val, boolean defaultVal) {
        if (TextUtils.isEmpty(val)) {
            Logger.w(TAG, "[toBoolean] value is empty");
            return defaultVal;
        }

        if ("true".equals(val)) {
            return true;
        } else if ("false".equals(val)) {
            return false;
        } else {
            int num = toInt(val, 0);
            return num != 0;
        }
    }

    /**
     * Check SharedPreference string is empty
     *
     * @param context
     * @param key     SharedPreference Key
     * @return
     */
    public static boolean checkSharedStringIsEmpty(Context context, String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return TextUtils.isEmpty(pref.getString(key, ""));
    }

    /**
     * Check SharedPreference string is equal compare string
     *
     * @param context
     * @param key
     * @param compare
     * @return
     */
    public static boolean checkSharedStringEqual(Context context, String key, String compare) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String source = pref.getString(key, "");
        return TextUtils.equals(source, compare);
    }

    /**
     * resource to Bitmap
     *
     * @param resource
     * @return
     */
    public static Bitmap resourceToBitmap(Context context, int resource) {
        return BitmapFactory.decodeResource(context.getResources(), resource);
    }

    /**
     * Get App launcher icon
     *
     * @param context
     * @return
     */
    public static int getAppIcon(Context context) {
        int appIconResId = 0;
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            appIconResId = applicationInfo.icon;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return appIconResId;
    }
    public static String getRequestBodyToString(final RequestBody requestBody){
        try {
            final Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            //Log.w(TAG, "Failed to stringify request body: " + e.getMessage());
        }
        return "";

    }
}
