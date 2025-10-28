package com.hyxen.adlocus;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.hyxen.adlocusaar.AdLocus;

import java.util.HashMap;
import java.util.Map;

//import com.google.firebase.iid.FirebaseInstanceId;
//import com.hyxen.adlocusaar.AdLocus;
//import com.hyxen.adlocusaar.utils.AdLocusUtil;

public class MainActivity extends AppCompatActivity  {

    private static final int TAG_LOCATION = 100;
    private static final int TAG_NOTIFICATIONS = 101;
    public String appkey="67ab3995eeb8efd913e9ce408c745a68804df01d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appkey=getString(R.string.app_key);
        AdLocus.getInstance(this)
                    .checkUserStatement( "",getString(R.string.fcm_app_key), getPackageName(), appkey);


        if(android.os.Build.VERSION.SDK_INT>= 33 ){
            if(ActivityCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.POST_NOTIFICATIONS"}, TAG_NOTIFICATIONS);
            }else {
                TestAD();
            }
        }else TestAD();




//        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//        pref.edit().putString("hash_device_id","and://1962e1a1b736a32b7ca3b86bda42fdc45f26603f");


//        Intent intent = new Intent(this, AdLocusActivity.class);
//        intent.setAction(AdLocusContract.ACTION_CLICK);
//        intent.putExtra(Constants.TAG_INTENT_URL, "https://card.apply.hsbc.com.tw/hsbcoa/oaadd?cardid=1&BannerID=ALS08");
//        intent.putExtra(Constants.TAG_INTENT_ID, -1);
//        intent.putExtra(Constants.TAG_INTENT_KEY_TRACK_IMP, "");
//
////        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);

//        new Collection(this).run();

//        RemoteAPI.init(this);
//        AdLocusHelp ah =new AdLocusHelp();
//        ah.postCollection(this);



//        Logger.d("",ah.getPostCollection(this));


//        String key="-----BEGIN PUBLIC KEY-----\n" +
//                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUlKGQpyjsOqrLkRMeCvbiE/ZG\n" +
//                "DXzJz6KAtprQ10G4lVVH6kkG82Fmj9hbm1agDCO5EAwHqTnzN0J0tQF+uhifcI54\n" +
//                "pRyRJ1dKXr+q9XqBIC43fBf5e2lBre8mGBK6WoSkHMxo9KWEhHk8SWVvAEHtVXUL\n" +
//                "6HQFQ5txU/SgC1vOrwIDAQAB\n" +
//                "-----END PUBLIC KEY-----";
//        String key=
//                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUlKGQpyjsOqrLkRMeCvbiE/ZG\n" +
//                "DXzJz6KAtprQ10G4lVVH6kkG82Fmj9hbm1agDCO5EAwHqTnzN0J0tQF+uhifcI54\n" +
//                "pRyRJ1dKXr+q9XqBIC43fBf5e2lBre8mGBK6WoSkHMxo9KWEhHk8SWVvAEHtVXUL\n" +
//                "6HQFQ5txU/SgC1vOrwIDAQAB\n" ;
//        byte[] publicBytes = Base64.decode(key, Base64.DEFAULT);
//
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//        String msg="";
//        try {
//            msg=Base64.encodeToString(RSAUtils.encryptByPublicKey("TESTAAAAA".getBytes(),publicBytes),Base64.NO_WRAP);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Log.d("===================encode");
//        Log.d(msg);
//        try {
//            RequestBody formBody = new FormBody.Builder()
//                    .add("msg", msg)
////                    .add("msg", "jJ26wV2wGpw1+Z4C8RTg0DEbx2w/F5aJw0JxsvLvUkvDqTrGpCDCt+ypRaOPWwEx+pwMfQy+YDq+ojXBlyNQTYAuwhF1ma3+9pVBJ6IjIRFY/iLkmWmIk/gNOhlYxn1Gl9lN3pRIGYnNU/hDKRW+WHxHsc8hO1ZSIhyKFnV2gRk=")
//                    .add("device_id", "123")
//                    .add("plain", "456")
//                    .build();
//            Request request = new Request.Builder()
//                    .url("https://data.rd.adlocus.com/new_log/and_device")
//                    .post(formBody)
//                    .build();
//            okHttpClient.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                    e.printStackTrace();
//                }
//
//                @Override
//                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                    Log.d("response:"+response.code());
//                    Log.d(new String(response.body().bytes()));
//                }
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


//        AdLocusUtil.getAndroidIDStatement(this);
//        AdLocusUtil.getAndroidIDStatement(this);

//
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            String token = FirebaseInstanceId.getInstance().getToken();
//            AdLocus.getInstance(this)
//                    .checkUserStatement(!TextUtils.isEmpty(token) ? token : "",
//                            getString(R.string.fcm_app_key), getPackageName(), appkey);
////            AdLocus.getInstance().receverAlarmAD(this,Constants.TAG_FCM_GA,"16226999405802","16216089796250");
//        } else {
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, TAG_LOCATION);
//        }

//        AdLocus.setDebug(this,false);
//        Logger.e("TAG", "[fcmMessage]:"+Constants.TAG_FCM_LC);
//        Map<String, String> fcmMessage=new TreeMap<>();
//        fcmMessage.put("type",Constants.TAG_FCM_LC);
//        AdLocus.getInstance().sendFCMMessage(this,fcmMessage);
//
//        String lbsstr= "{\"pt\":[{\"ad_id\":\"161972022358231011\",\"begin_ts\":\"1619971200\",\"end_ts\":\"1620057600\",\"fcm_push\":\"1\",\"llr\":[{\"lat\":\"25.019461\",\"lon\":\"121.542305\",\"radius\":\"1550\"},{\"lat\":\"25.043152\",\"lon\":\"121.525536\",\"radius\":\"1550\"}],\"session_id\":\"16197408040848\"},{\"ad_id\":\"161912044142341011\",\"begin_ts\":\"1619971200\",\"end_ts\":\"1620057600\",\"fcm_push\":\"1\",\"llr\":[{\"lat\":\"25.013651\",\"lon\":\"121.466765\",\"radius\":\"2000\"}],\"session_id\":\"16191360040784\"},{\"ad_id\":\"161972048435731011\",\"begin_ts\":\"1619971200\",\"end_ts\":\"1620057600\",\"fcm_push\":\"1\",\"llr\":[{\"lat\":\"25.019461\",\"lon\":\"121.542305\",\"radius\":\"1550\"},{\"lat\":\"25.043152\",\"lon\":\"121.525536\",\"radius\":\"1550\"}],\"session_id\":\"16197408040849\"},{\"ad_id\":\"161912055358021011\",\"begin_ts\":\"1619971200\",\"end_ts\":\"1620057600\",\"fcm_push\":\"1\",\"llr\":[{\"lat\":\"25.013651\",\"lon\":\"121.466765\",\"radius\":\"2000\"}],\"session_id\":\"16191360040783\"},{\"ad_id\":\"161912091316621011\",\"begin_ts\":\"1619971200\",\"end_ts\":\"1620057600\",\"fcm_push\":\"1\",\"llr\":[{\"lat\":\"25.013651\",\"lon\":\"121.466765\",\"radius\":\"2000\"}],\"session_id\":\"16191360040782\"},{\"ad_id\":\"161972059208041011\",\"begin_ts\":\"1619971200\",\"end_ts\":\"1620057600\",\"fcm_push\":\"1\",\"llr\":[{\"lat\":\"25.019461\",\"lon\":\"121.542305\",\"radius\":\"1550\"},{\"lat\":\"25.043152\",\"lon\":\"121.525536\",\"radius\":\"1550\"}],\"session_id\":\"16197408040850\"}],\"err\":\"0\"}";
//        Repository.setLbsTaskJson(lbsstr);
//        LbsChecker.getInstance(this).startAlarmTimer();

    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case TAG_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
////                    String token = FirebaseInstanceId.getInstance().getToken();
//                    String token = "test";
//                    AdLocus.getInstance(this)
//                            .checkUserStatement(!TextUtils.isEmpty(token) ? token : "",
//                                    getString(R.string.fcm_app_key), getPackageName(), appkey);
//                } else {
////                    String token = FirebaseInstanceId.getInstance().getToken();
//                    String token = "test";
//                    AdLocus.getInstance(this)
//                            .checkUserStatement(!TextUtils.isEmpty(token) ? token : "",
//                                    getString(R.string.fcm_app_key), getPackageName(), appkey);
//                }
//                return;
//            }
//        }
//    }

    public void TestAD(){
        Map<String, String> jsonData = new HashMap<>();
        jsonData.put("title", "mytitle");
        jsonData.put("body", "測試時定位關掉才會比較順");
        jsonData.put("url", "myurl");
        jsonData.put("type", "TEST");
        jsonData.put("adid", "17037696780777");
        jsonData.put("sessionid", "16624800102833");
        jsonData.put("target", "AdLocusSDK");
        AdLocus.getInstance().sendFCMMessage(this,jsonData);
    }
}
