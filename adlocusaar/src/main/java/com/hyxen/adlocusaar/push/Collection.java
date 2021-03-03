package com.hyxen.adlocusaar.push;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Base64;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.hyxen.adlocusaar.utils.RSAUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by kiddchen on 6/6/16.
 */
public class Collection extends HxRequest {
    private static final String TAG = "Collection";
    private int hashCollection = 0;
    private Collection(Context context) {
        super(context, AdLocusUtil.URL_DATA_COLLECTION);

        setPostParameter("device_id", AdLocusUtil.getEncodedDeviceId(context));
        setPostParameter("key", AdLocusUtil.getPushKey(context));

    }

    private JSONArray getAppList(){
        final PackageManager pm = getContext().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        JSONArray a = new JSONArray();
        for (ApplicationInfo packageInfo : packages) {
            if ((packageInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) continue;

            a.put(packageInfo.packageName);
        }
        return a;
    }

    @Override
    public void run() {
        try {
            String key=
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUlKGQpyjsOqrLkRMeCvbiE/ZG\n" +
                "DXzJz6KAtprQ10G4lVVH6kkG82Fmj9hbm1agDCO5EAwHqTnzN0J0tQF+uhifcI54\n" +
                "pRyRJ1dKXr+q9XqBIC43fBf5e2lBre8mGBK6WoSkHMxo9KWEhHk8SWVvAEHtVXUL\n" +
                "6HQFQ5txU/SgC1vOrwIDAQAB\n" ;
            byte[] publicBytes = com.hyxen.adlocusaar.utils.Base64.decode(key, com.hyxen.adlocusaar.utils.Base64.DEFAULT);

            JSONObject o = new JSONObject();
            o.put("mcc", getMcc());
            o.put("mnc", getMnc());
            if(Locale.getDefault().toString().length()>0)o.put("lang", com.hyxen.adlocusaar.utils.Base64.encodeToString(RSAUtils.encryptByPublicKey(Locale.getDefault().toString().getBytes(),publicBytes), com.hyxen.adlocusaar.utils.Base64.NO_WRAP));
            else o.put("lang", "");
//            o.put("lang", Locale.getDefault().toString());

            if(AdLocusUtil.getMac(getContext()).length()>0)o.put("mac", Base64.encodeToString(RSAUtils.encryptByPublicKey(AdLocusUtil.getMac(getContext()).getBytes(),publicBytes), Base64.NO_WRAP));
            else o.put("mac", "");
//            o.put("mac", AdLocusUtil.getMac(getContext()));
            o.put("noti", AdLocusUtil.isNotificationEnable(getContext()) ? 1 : 0);

            if(AdLocusUtil.getAdId(getContext()).length()>0)o.put("adid", Base64.encodeToString(RSAUtils.encryptByPublicKey(AdLocusUtil.getAdId(getContext()).getBytes(),publicBytes), Base64.NO_WRAP));
            else o.put("adid", "");
//            o.put("adid", AdLocusUtil.getAdId(getContext()));
            o.put("tar_enable", isLimitAdTrackingEnabled() ? 1 : 0);

            hashCollection = o.toString().hashCode();

            o.put("pkg", getAppList());


            setPostParameter("plain",  o.toString());
//            setPostParameter("plain", encrypt("e2e4193b842bb054", o.toString()));


//            String key=
//                    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUlKGQpyjsOqrLkRMeCvbiE/ZG\n" +
//                            "DXzJz6KAtprQ10G4lVVH6kkG82Fmj9hbm1agDCO5EAwHqTnzN0J0tQF+uhifcI54\n" +
//                            "pRyRJ1dKXr+q9XqBIC43fBf5e2lBre8mGBK6WoSkHMxo9KWEhHk8SWVvAEHtVXUL\n" +
//                            "6HQFQ5txU/SgC1vOrwIDAQAB\n" ;
//            byte[] publicBytes = com.hyxen.adlocusaar.utils.Base64.decode(key, com.hyxen.adlocusaar.utils.Base64.DEFAULT);
//            setPostParameter("plain",com.hyxen.adlocusaar.utils.Base64.encodeToString(RSAUtils.encryptByPublicKey(o.toString().getBytes(),publicBytes), com.hyxen.adlocusaar.utils.Base64.NO_WRAP).trim());
        } catch (Exception ignored) { }
        if (AdLocusUtil.SHOW_LOG) android.util.Log.d(TAG, "Collection() returned: " + getPostString());
        if (AdLocusUtil.SHOW_LOG) android.util.Log.d(TAG, "Collection() returned: " + AdLocusUtil.SHOW_LOG);

        if (hashCollection == 0 || !AdLocusUtil.hasCollectionTrackConsent(getContext(),hashCollection)) return;
        super.run();
    }

    @Override
    protected void processContent(int errorCode, String content) {
        if (hasError()) return;
        AdLocusUtil.setCollectionData(getContext(), hashCollection);
    }

    private int getMcc() {
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getSimOperator();

        if (TextUtils.isEmpty(networkOperator)) return -1;

        return Integer.parseInt(networkOperator.substring(0, 3));
    }

    private int getMnc() {
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getSimOperator();

        if (TextUtils.isEmpty(networkOperator)) return -1;

        return Integer.parseInt(networkOperator.substring(3));
    }

    private boolean isLimitAdTrackingEnabled(){
        try {
            AdvertisingIdClient.Info idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getContext().getApplicationContext());
            if (idInfo != null) return idInfo.isLimitAdTrackingEnabled();
        } catch (Exception ignored) { }
        return true;
    }

    public static void report(Context context){
        new Thread(new Collection(context)).start();
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
}
