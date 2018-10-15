package com.hyxen.adlocusaar.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.webkit.WebSettings;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.hyxen.adlocusaar.AdLocus;
import com.hyxen.adlocusaar.BuildConfig;
import com.hyxen.adlocusaar.R;
import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.Repository;

import org.json.JSONArray;

import java.math.BigInteger;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AdLocusUtil {
    private static final String TAG = AdLocus.class.getSimpleName();
    private static final String MAC_DEFAULT = "02:00:00:00:00:00";

    private static final String FAKE_SCREEN = "320";

    public static final String MAC_SDK_VERSION = BuildConfig.VERSION_NAME;
    public static final String TEST_MODE = "0";//0:false, 1:true
    private static final String PREFERENCE_COLLECTION = "collection";
    private static final String PREFERENCE_COLLECTION_TRACKTIME = "collectiontracktime";

    private static AlertDialog.Builder mStatementDialog = null;

    private static final long NEXT_TRACK_TIME = (long) 86400 * 30 * 1000;

    public static String getMac(Context context) {

        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager == null) {
            Logger.e(TAG, "[getMac] manager is null");
            return null;
        }
        android.net.wifi.WifiInfo info = manager.getConnectionInfo();
        String mac = info.getMacAddress();
        if (mac != null && !mac.equals(MAC_DEFAULT)) return mac;
        return getMac6();
    }

    private static String getMac6() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF)).append(":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ignored) {
        }
        return MAC_DEFAULT;
    }

    /**
     * Get Google Ad Id
     *
     * @return
     */
    public static String getGoogleADID(Context context) {
        AdvertisingIdClient.Info idInfo;
        String androidAdId = "";
        try {
            idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context.getApplicationContext());
            androidAdId = idInfo.getId();
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }

        return androidAdId;
    }

    /**
     * Get And://Google AD Id
     *
     * @param context
     * @return
     */
    public static String getAndGoogleADID(Context context) {
        return String.format("and://%s", getGoogleADID(context));
    }

    /**
     * Gets the hashed Google Play Service AD ID for device id.
     *
     * @param context the application context.
     * @return The encoded Google Play Service AD ID.
     */
    public static String getEncodedGoogleADId(Context context) {
        String googleAdId = getGoogleADID(context);

        String hashedId;
        if (TextUtils.isEmpty(googleAdId) || isEmulator()) {
            hashedId = hashId("emulator" + System.currentTimeMillis());
        } else {
            hashedId = hashId(googleAdId);
        }

        return hashedId;
    }

    /**
     * Get Encode Android id
     *
     * @param context
     * @return
     */
    public static String getEncodeDeviceId(Context context) {
        String androidId = getDeviceId(context);

        String hashedId;
        if ((androidId == null) || isEmulator()) {
            hashedId = hashId("emulator" + System.currentTimeMillis());
        } else {
            hashedId = hashId(androidId);
        }

        if (hashedId == null) {
            return null;
        }

        return hashedId;
    }

    /**
     * Get Android id
     *
     * @param context
     * @return
     */
    private static String getDeviceId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if ((androidId == null) || isEmulator()) {
            androidId = "emulator" + System.currentTimeMillis();
        }

        return androidId;
    }

    /**
     * Method for returning an hashId hash of a string.
     *
     * @param val the string to hash.
     * @return A hex string representing the hashId hash of the input.
     */
    private static String hashId(String val) {
        return "and://" + sha1(val);
    }

    /**
     * Method for returning an sha1 hash of a string.
     *
     * @param val the string to hash.
     * @return A hex string representing the sha1 hash of the input.
     */
    public static String sha1(String val) {
        String result = null;

        if ((val != null) && (val.length() > 0)) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(val.getBytes());
                return String.format("%040x", new BigInteger(1, md.digest()));
            } catch (NoSuchAlgorithmException nsae) {
                result = val.substring(0, 40);
            }
        }
        return result;
    }

    /**
     * Checks whether or not the running device is an emulator.
     *
     * @return Boolean indicating if the app is currently running in an emulator.
     */
    public static boolean isEmulator() {
        return (Build.BOARD.equals("unknown") && Build.DEVICE.equals("generic") && Build.BRAND.equals("generic"));
    }

    public static String getScreen() {
        return FAKE_SCREEN;
    }

    /**
     * Show User Android id Statement
     *
     * @param context
     */
    public static boolean showAndroidIDStatement(Context context, DialogInterface.OnClickListener pos, DialogInterface.OnClickListener neg, DialogInterface.OnDismissListener dismiss) {
        if (Repository.getUserAndroidIdState() == Constants.TAG_ANDROID_ID_STATEMENT_STATE_FIRST) {
            if ( context instanceof Activity) {
                if (mStatementDialog == null) {
                    mStatementDialog = new AlertDialog.Builder(context, R.style.MyAlertDialog)
                            .setMessage(R.string.statement_message)
                            .setPositiveButton(R.string.common_grant, pos)
                            .setNegativeButton(R.string.common_denied, neg)
                            .setCancelable(false)
                            .setOnDismissListener(dismiss);
                }
                try{
                    Activity act=((Activity) context);
                    if(!act.isFinishing() && !act.isDestroyed()){
                        mStatementDialog.show();
                        return true;
                    }
                }catch(Exception e){}
            }
        }
        return false;
    }
    public static boolean isNotificationEnable(Context context){
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }
    public static String encrypt(String key, String encrypt) throws Exception
    {
        byte[] bKey = key.getBytes("UTF-8");
        byte[] bEncrypt = encrypt.getBytes("UTF-8");
        if(bEncrypt.length % 16 != 0)
        { //not a multiple of 8
            //create a new array with a size which is a multiple of 8
            byte[] padded = new byte[bEncrypt.length + 16 - (bEncrypt.length % 16)];

            //copy the old array into it
            System.arraycopy(bEncrypt, 0, padded, 0, bEncrypt.length);
            bEncrypt = padded;
        }
        SecretKeySpec skeySpec = new SecretKeySpec(bKey, "AES");
        Cipher cipher =  Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(bEncrypt);
        return Base64.encodeToString(encrypted, Base64.NO_WRAP).trim();
    }

    public static String decrypt(String key, String encryptedBase64) throws Exception
    {
        byte[] bKey = key.getBytes("UTF-8");
        byte[] encrypted = Base64.decode(encryptedBase64.trim(), Base64.NO_PADDING);
        SecretKeySpec skeySpec = new SecretKeySpec(bKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-8").trim();
    }
    public static boolean isLimitAdTrackingEnabled(final Context context){
        try {
            AdvertisingIdClient.Info idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            if (idInfo != null) return idInfo.isLimitAdTrackingEnabled();
        } catch (Exception ignored) { }
        return true;
    }
    public static JSONArray getAppList(final Context context){
        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        JSONArray a = new JSONArray();
        for (ApplicationInfo packageInfo : packages) {
            if ((packageInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) continue;

            a.put(packageInfo.packageName);
        }
        return a;
    }

    public static boolean hasCollectionTrackConsent(Context context, int collectionData) {
        int lastData = PreferenceManager.getDefaultSharedPreferences(context).getInt(PREFERENCE_COLLECTION, 0);
        if (collectionData != lastData) return true;
        long lastTrackTime = PreferenceManager.getDefaultSharedPreferences(context).getLong(PREFERENCE_COLLECTION_TRACKTIME, 0);
        return (System.currentTimeMillis() - lastTrackTime > NEXT_TRACK_TIME);
    }

    public static void setCollectionData(Context context, int collectionData) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(PREFERENCE_COLLECTION, collectionData)
                .putLong(PREFERENCE_COLLECTION_TRACKTIME, System.currentTimeMillis())
                .commit();
    }
    public static String getUserAgent(Context context) {
        String userAgent = "";
        int versionCode=0;
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            versionCode=packageInfo.versionCode;
        } catch (Exception e){

        }
        try{
            if (versionCode >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                try {
                    userAgent = WebSettings.getDefaultUserAgent(context);
                } catch (Exception e) {
                    userAgent = System.getProperty("http.agent");
                }
            } else {
                userAgent = System.getProperty("http.agent");
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0, length = userAgent.length(); i < length; i++) {
                char c = userAgent.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
            userAgent=sb.toString();
        }catch (Exception e){

        }

        return userAgent;
    }
}
