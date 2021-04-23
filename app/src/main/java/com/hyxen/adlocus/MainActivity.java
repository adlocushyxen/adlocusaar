package com.hyxen.adlocus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.iid.FirebaseInstanceId;
import com.hyxen.adlocusaar.AdLocus;
import com.hyxen.adlocusaar.AdLocusHelp;
import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.remote.RemoteAPI;
import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Log;
import com.hyxen.adlocusaar.utils.Base64;
import com.hyxen.adlocusaar.utils.Logger;
import com.hyxen.adlocusaar.utils.RSAUtils;
import com.hyxen.adlocusaar.view.main.AdLocusActivity;
import com.hyxen.adlocusaar.view.main.AdLocusContract;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity  {

    private static final int TAG_LOCATION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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







        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            String token = FirebaseInstanceId.getInstance().getToken();
            AdLocus.getInstance(this)
                    .checkUserStatement(!TextUtils.isEmpty(token) ? token : "",
                            getString(R.string.fcm_app_key), getPackageName(), getString(R.string.app_key));
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, TAG_LOCATION);
        }


        Logger.e("TAG", "[fcmMessage]:"+Constants.TAG_FCM_LC);
        Map<String, String> fcmMessage=new TreeMap<>();
        fcmMessage.put("type",Constants.TAG_FCM_LC);
        AdLocus.getInstance().sendFCMMessage(this,fcmMessage);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case TAG_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    String token = FirebaseInstanceId.getInstance().getToken();
                    AdLocus.getInstance(this)
                            .checkUserStatement(!TextUtils.isEmpty(token) ? token : "",
                                    getString(R.string.fcm_app_key), getPackageName(), getString(R.string.app_key));
                } else {
                    String token = FirebaseInstanceId.getInstance().getToken();
                    AdLocus.getInstance(this)
                            .checkUserStatement(!TextUtils.isEmpty(token) ? token : "",
                                    getString(R.string.fcm_app_key), getPackageName(), getString(R.string.app_key));
                }
                return;
            }
        }
    }
}
