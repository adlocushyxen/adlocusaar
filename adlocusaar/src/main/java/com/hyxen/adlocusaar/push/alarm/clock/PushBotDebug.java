package com.hyxen.adlocusaar.push.alarm.clock;

import com.hyxen.adlocusaar.AdLocus;
import com.hyxen.adlocusaar.utils.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by leo3x on 2018/11/14.
 */

public class PushBotDebug {
    private static final java.lang.String TAG = PushBotDebug.class.getSimpleName();
    public static void push(TreeMap<String,String> map){
        OkHttpClient client = new OkHttpClient();
        Request.Builder bb=new Request.Builder();
        FormBody.Builder builder = new FormBody.Builder();
        for(Map.Entry<String,String> entry:map.entrySet()){
            builder.add(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody=builder.build();
        Request request_bot = bb.url("https://hyxen.slack.com/services/hooks/slackbot?token=NIPcyGkN1cWjs48A7ny7sWLV&channel=customer_fcm_token").post(requestBody).build();
        if(AdLocus.isDebug()) Logger.d(TAG, "[push] https://hyxen.slack.com/services/hooks/slackbot?token=NIPcyGkN1cWjs48A7ny7sWLV&channel=customer_fcm_token");
        client.newCall(request_bot).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(AdLocus.isDebug())e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(AdLocus.isDebug()) Logger.d(TAG, "[push onResponse] "+response.code());
            }
        });
    }
}
