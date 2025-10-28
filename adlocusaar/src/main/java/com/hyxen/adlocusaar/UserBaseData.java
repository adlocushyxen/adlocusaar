package com.hyxen.adlocusaar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.Repository;
import com.hyxen.adlocusaar.repository.data.request.PushTokenRequest;
import com.hyxen.adlocusaar.utils.AdLocusUtil;
import com.hyxen.adlocusaar.utils.MiscUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class UserBaseData {
    private static Context _ctx;
    private static SharedPreferences sharedPreferences;
    public static void init(Context ctx){
        _ctx=ctx;
        MasterKey mainKey = null;
        try {

            mainKey = new MasterKey.Builder(_ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            sharedPreferences = EncryptedSharedPreferences
                .create(
                    _ctx,
                    "ADlocusUserBaseData",
                    mainKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean setString(Context ctx ,String key,String value){
        if(sharedPreferences==null){
            if(ctx!=null)init(ctx);
        }
        if(sharedPreferences!=null){
            sharedPreferences.edit().putString(key,value).apply();
            return true;
        }
        return false;
    }
    public static String getString(Context ctx ,String key){
        if(sharedPreferences==null){
            if(ctx!=null)init(ctx);
        }
        if(sharedPreferences!=null){
            return sharedPreferences.getString(key,"");
        }
        return "";
    }
    public static boolean checkSharedStringIsEmpty(Context context, String key) {
        return TextUtils.isEmpty(getString(context,key));
    }
    public static boolean checkSharedStringEqual(Context context, String key, String compare) {
        String source = getString(context,key);
        return TextUtils.equals(source, compare);
    }

    private static String getEncodeDeviceId(Context context) {
        String androidId = getDeviceId(context);

        String hashedId;
        if ((androidId == null) || AdLocusUtil.isEmulator()) {
            hashedId = AdLocusUtil.hashId("emulator" + System.currentTimeMillis());
        } else {
            hashedId = AdLocusUtil.hashId(androidId);
        }

        if (hashedId == null) {
            return null;
        }

        return hashedId;
    }
    private static String getDeviceId(Context context) {
        String androidId=null;
        int userStatement = Repository.getUserAndroidIdState();
//        if(userStatement == Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT)androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if ((androidId == null) || AdLocusUtil.isEmulator()) {
            androidId = "emulator" + System.currentTimeMillis();
        }
        return androidId;
    }





    public static boolean setHashDeviceId(Context ctx){
        String key= Repository.KEY_HASH_DEVICE_ID;
        if (checkSharedStringIsEmpty(ctx, key)) {
            return setString(ctx,key, getEncodeDeviceId(ctx));
        }
        return false;
    }
    public static String getHashDeviceId(Context ctx){
        String key= Repository.KEY_HASH_DEVICE_ID;
        return getString(ctx ,key);
    }

    public static boolean setGoogleAdId(Context ctx){
        String key= Repository.KEY_GOOGLE_AD_ID;
        if (checkSharedStringIsEmpty(ctx, key)) {
            int userStatement = Repository.getUserAndroidIdState();
            if(userStatement == Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT){
//                return setString(ctx,key, AdLocusUtil.getGoogleADID(ctx));
            }
        }
        return false;
    }
    public static String getGoogleAdId(Context ctx){
        String key= Repository.KEY_GOOGLE_AD_ID;
        return getString(ctx ,key);
    }


    public static boolean setGoogleAndAdId(Context ctx){
        String key= Repository.KEY_GOOGLE_AND_AD_ID;
        if (checkSharedStringIsEmpty(ctx, key)) {
            int userStatement = Repository.getUserAndroidIdState();
            if(userStatement == Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT){
                return setString(ctx,key, AdLocusUtil.getAndGoogleADID(ctx));
            }
        }
        return false;
    }
    public static String getGoogleAndAdId(Context ctx){
        String key= Repository.KEY_GOOGLE_AND_AD_ID;
        return getString(ctx ,key);
    }

    public static boolean setGoogleEncodeAdId(Context ctx){
        String key= Repository.KEY_GOOGLE_ENCODE_AD_ID;
        if (checkSharedStringIsEmpty(ctx, key)) {
            int userStatement = Repository.getUserAndroidIdState();
            if(userStatement == Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT){
                return setString(ctx,key, AdLocusUtil.getEncodedGoogleADId(ctx));
            }
        }
        return false;
    }
    public static String getGoogleEncodeAdId(Context ctx){
        String key= Repository.KEY_GOOGLE_ENCODE_AD_ID;
        return getString(ctx ,key);
    }



    public static boolean setMac(Context ctx){
//        String key= Repository.KEY_DEVICE_MAC;
//        if (checkSharedStringIsEmpty(ctx, key)) {
//            int userStatement = Repository.getUserAndroidIdState();
//            return setString(ctx,key, userStatement == Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT ?AdLocusUtil.getMac(ctx):"no_access");
//        }
        return false;
    }
    public static String getMac(Context ctx){
        String key= Repository.KEY_DEVICE_MAC;
        return getString(ctx ,key);
    }

    public static boolean setDeviceModel(Context ctx){
//        String key= Repository.KEY_DEVICE_MODEL;
//        if (checkSharedStringIsEmpty(ctx, key)) {
//            return setString(ctx,key, Build.MODEL);
//        }
        return false;
    }
    public static String getDeviceModel(Context ctx){
        String key= Repository.KEY_DEVICE_MODEL;
        return getString(ctx ,key);
    }

    public static boolean setAppPackageName(Context ctx,final PushTokenRequest pushTokenData){
        String key= Repository.KEY_APP_PACKAGE_NAME;
        if (checkSharedStringIsEmpty(ctx, key)) {
            return setString(ctx,key, pushTokenData.getAppPackageName());
        }
        return false;
    }
    public static String getAppPackageName(Context ctx){
        String key= Repository.KEY_APP_PACKAGE_NAME;
        return getString(ctx ,key);
    }

    public static boolean setAppKey(Context ctx,final PushTokenRequest pushTokenData){
        String key= Repository.KEY_APP_KEY;
        if (!TextUtils.isEmpty(pushTokenData.getAppKey())) {
            return setString(ctx,key, pushTokenData.getAppKey());
        }
        return false;
    }
    public static String getAppKey(Context ctx){
        String key= Repository.KEY_APP_KEY;
        return getString(ctx ,key);
    }

    public static boolean setFcmAppKey(Context ctx,final PushTokenRequest pushTokenData){
        String key= Repository.KEY_FCM_APP_KEY;
        if (!TextUtils.isEmpty(pushTokenData.getFcmAppKey())) {
            return setString(ctx,key, pushTokenData.getFcmAppKey());
        }
        return false;
    }
    public static String getFcmAppKey(Context ctx){
        String key= Repository.KEY_FCM_APP_KEY;
        return getString(ctx ,key);
    }

    public static boolean setFcmToken(Context ctx,final PushTokenRequest pushTokenData){
        String key= Repository.KEY_FIREBASE_TOKEN;
        String pushToken = pushTokenData.getPushToken();
        if (!TextUtils.isEmpty(pushToken)) {
            if (!checkSharedStringEqual(ctx, key, pushToken)){
                return setString(ctx,key, pushToken);
            }
        }
        return false;
    }
    public static boolean setFcmToken(Context ctx,final String pushToken){
        String key= Repository.KEY_FIREBASE_TOKEN;
        if (!TextUtils.isEmpty(pushToken)) {
            return setString(ctx,key, pushToken);
        }
        return false;
    }
    public static String getFcmToken(Context ctx){
        String key= Repository.KEY_FIREBASE_TOKEN;
        return getString(ctx ,key);
    }







    public static boolean setSdkVersion(Context ctx){
        String key= Repository.KEY_SDK_VERSION;
        if (!checkSharedStringEqual(ctx, key, AdLocusUtil.MAC_SDK_VERSION)) {
            return setString(ctx,key, AdLocusUtil.MAC_SDK_VERSION);
        }
        return false;
    }
    public static String getSdkVersion(Context ctx){
        String key= Repository.KEY_SDK_VERSION;
        return getString(ctx ,key);
    }




    public static boolean setAppName(Context ctx,final String name){
        String key= Repository.KEY_APP_NAME;
        if (!TextUtils.isEmpty(name)) {
            return setString(ctx,key, name);
        }
        return false;
    }
    public static String getAppName(Context ctx,final String default_name){
        String key= Repository.KEY_APP_NAME;
        String ret = getString(ctx ,key);
        if(ret.equals(""))ret=default_name;
        return ret;
    }

}
