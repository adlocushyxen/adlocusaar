package com.hyxen.adlocusaar.repository.remote;

import com.hyxen.adlocusaar.repository.data.RemoteResponse;
import com.hyxen.adlocusaar.repository.data.response.GetCollectionResponse;
import com.hyxen.adlocusaar.repository.data.response.GetLbsFileResponseUrl;
import com.hyxen.adlocusaar.repository.data.response.GetLbsTaskResponse;
import com.hyxen.adlocusaar.repository.data.response.GetNewAndResponse;
import com.hyxen.adlocusaar.repository.data.response.GetNewImpressionResponse;

import io.reactivex.Single;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface AdLocusService {

    @POST("fcm/general/push_token")
    Single<RemoteResponse> postPushToken(
            @Body RequestBody data
    );

    @POST("request/new_and")
    Single<GetNewAndResponse> postNewAnd(
            @Body RequestBody data
    );

    @POST("devpush/newimp")
    Single<GetNewImpressionResponse> postNewImpression(
            @Body RequestBody data
    );

    @POST("bigview/feedback")
    Single<RemoteResponse> postFeedback(
            @Body RequestBody data
    );

    @POST("devpush/get_json_link")
    Single<GetLbsFileResponseUrl> getLbsFileUrl(
            @Body RequestBody data
    );

    @POST("fcm/ad_ab/lbs_task")
    Single<GetLbsTaskResponse> getLbsTask(
            @Body RequestBody data
    );

    @POST
    Single<GetCollectionResponse> postCollection(
            @Url String url,
            @Body RequestBody data
    );
}
