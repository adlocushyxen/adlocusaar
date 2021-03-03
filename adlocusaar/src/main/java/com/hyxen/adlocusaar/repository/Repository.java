package com.hyxen.adlocusaar.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.hyxen.adlocusaar.R;
import com.hyxen.adlocusaar.constants.ApiStatus;
import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.data.RemoteResponse;
import com.hyxen.adlocusaar.repository.data.request.CollectionRequest;
import com.hyxen.adlocusaar.repository.data.request.FeedbackRequest;
import com.hyxen.adlocusaar.repository.data.request.GetLbsFileRequest;
import com.hyxen.adlocusaar.repository.data.request.NewAndRequest;
import com.hyxen.adlocusaar.repository.data.request.PushTokenRequest;
import com.hyxen.adlocusaar.repository.data.response.GetCollectionResponse;
import com.hyxen.adlocusaar.repository.data.response.GetLbsFileResponseUrl;
import com.hyxen.adlocusaar.repository.data.response.GetLbsTaskResponse;
import com.hyxen.adlocusaar.repository.data.response.GetNewAndResponse;
import com.hyxen.adlocusaar.repository.data.response.GetNewImpressionResponse;
import com.hyxen.adlocusaar.repository.remote.AdLocusAPI;
import com.hyxen.adlocusaar.repository.remote.RemoteAPI;
import com.hyxen.adlocusaar.utils.AdLocusUtil;
import com.hyxen.adlocusaar.utils.Logger;
import com.hyxen.adlocusaar.utils.MiscUtils;
import com.hyxen.adlocusaar.utils.PhoneCellUtil;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * Define all apis here, including remote apis and local apis.
 */
public class Repository {
    private static final String TAG = Repository.class.getSimpleName();
    //User Base Info
    private static final String KEY_FIREBASE_TOKEN = "firebase_token";
    private static final String KEY_HASH_DEVICE_ID = "hash_device_id";
    private static final String KEY_SDK_VERSION = "sdk_version";
    private static final String KEY_APP_KEY = "app_key";//Hyxen app key
    private static final String KEY_DEVICE_MAC = "device_mac";
    private static final String KEY_DEVICE_MODEL = "device_model";
    private static final String KEY_FCM_APP_KEY = "fcm_app_key";
    private static final String KEY_APP_PACKAGE_NAME = "app_package_name";
    private static final String KEY_REGISTER_STATE = "register_state";
    private static final String KEY_LBS_TASK_JSON = "lbs_task_json";
    private static final String KEY_ANDROID_ID_USER_STATE = "android_id_user_state";
    private static final String KEY_GOOGLE_AD_ID = "google_ad_id";

    //FCM Message
    private static final String KEY_FCM_MESSAGE = "fcm_message";

    /// RD Site
    private static final String APP_KEY = "4116f0ac07a19b7db4b0ed7839b70618812850f1";

    private static WeakReference<Context> mContextRef;
    private static SingleTransformer sRemoteErrorHandler = new RemoteErrorHandler();

    public static void init(Context context) {
        mContextRef = new WeakReference<>(context);
        RemoteAPI.init(context);
    }

    /*--------------------------------------------------------------------------------------------*/
    /* Remote Apis */

    /**
     * Post Push Token
     */
    public static Single<RemoteResponse> postPushToken() {
        Logger.i(TAG, "[Method] -> postPushToken()");
        boolean error = false;
        PushTokenRequest request = getUserBaseData();

        if (request == null) {
            Logger.e(TAG, "[postPushToken] request is null");
            error = true;
        } else {
            if (TextUtils.isEmpty(request.getPushToken())) {
                Logger.e(TAG, "[postPushToken] request.getPushToken() is null");
                error = true;
            }
            if (TextUtils.isEmpty(request.getDeviceId())) {
                Logger.e(TAG, "[postPushToken] request.getDeviceId() is null");
                error = true;
            }
            if (TextUtils.isEmpty(request.getAppKey())) {
                Logger.e(TAG, "[postPushToken] request.getAppKey() is null");
                error = true;
            }
            if (TextUtils.isEmpty(request.getFcmAppKey())) {
                Logger.e(TAG, "[postPushToken] request.getFcmAppKey() is null");
                error = true;
            }
            // Optional param
            if (TextUtils.isEmpty(request.getSdkVersion())) {
                Logger.e(TAG, "[postPushToken] request.getSdkVersion() is null");
            }
            if (TextUtils.isEmpty(request.getDeviceMac())) {
                Logger.e(TAG, "[postPushToken] request.getDeviceMac() is null");
            }
            if (TextUtils.isEmpty(request.getAppPackageName())) {
                Logger.e(TAG, "[postPushToken] request.getAppPackageName() is null");
            }
            if (TextUtils.isEmpty(request.getDeviceModel())) {
                Logger.e(TAG, "[postPushToken] request.getDeviceModel() is null");
            }
        }
        if (error) {
            return illegalContext();
        }

        return AdLocusAPI.getInstance()
                .postPushToken(request)
                .compose(Repository.<RemoteResponse>applyErrorHandling());
    }

    /**
     * Get Android AD material data
     *
     * @param request
     * @return
     */
    public static Single<GetNewAndResponse> postNewAnd(Context ctx,NewAndRequest request) {
        Logger.i(TAG, "[Method] -> postNewAnd()");
//        Context ctx=null;
//        if(mContextRef!=null ) ctx=mContextRef.get();

        return AdLocusAPI.getInstance(ctx)
                .postNewAnd(request)
                .compose(Repository.<GetNewAndResponse>applyErrorHandling());
//                .filter(new Predicate<GetNewAndResponse>() {
//                    @Override
//                    public boolean test(GetNewAndResponse getNewAndResponse) throws Exception {
//                        Logger.i(TAG, "postNewAnd test " + MiscUtils.toJSONString(getNewAndResponse));
//                        return TextUtils.equals(getNewAndResponse.getAdType(), Constants.TAG_AD_TYPE_ICON) ||
//                                TextUtils.equals(getNewAndResponse.getAdType(), Constants.TAG_AD_TYPE_BANNER) ||
//                                TextUtils.equals(getNewAndResponse.getAdType(), Constants.TAG_AD_TYPE_BIG_VIEW);
//                    }
//                }).toSingle();
    }

    /**
     * Check user impression number is under the limit
     *
     * @param request @return
     * @param lat
     * @param lon
     */
    public static Single<GetNewImpressionResponse> postNewImpression(NewAndRequest request, String lat, String lon) {
        return AdLocusAPI.getInstance()
                .postNewImpression(request,lat,lon)
                .compose(Repository.<GetNewImpressionResponse>applyErrorHandling());
    }

    public static Single<GetNewAndResponse> checkNewAndObject(Context ctx,NewAndRequest request) {
        return Single.concat(postNewAnd(ctx,request), postNewImpression(request, "", ""))
                .collect(new Callable<GetNewAndResponse>() {
                    @Override
                    public GetNewAndResponse call() throws Exception {
                        return new GetNewAndResponse();
                    }
                }, new BiConsumer<GetNewAndResponse, Object>() {
                    @Override
                    public void accept(@NonNull GetNewAndResponse getNewAndResponse, @NonNull Object object) throws Exception {
                        if (object instanceof GetNewAndResponse) {
                            getNewAndResponse = (GetNewAndResponse) object;
                        }
                    }
                });

    }

    /**
     * Send feedback to server
     *
     * @param request
     * @return
     */
    public static Single<RemoteResponse> postFeedback(FeedbackRequest request) {
        return AdLocusAPI.getInstance()
                .postFeedback(request)
                .compose(Repository.<RemoteResponse>applyErrorHandling());
    }


    /**
     * Post Collection material data
     *
     * @param request
     * @return
     */
    public static Single<GetCollectionResponse> postCollection(CollectionRequest request) {
        Logger.i(TAG, "[Method] -> postCollection()");
        return AdLocusAPI.getInstance()
                .postCollection(request)
                .compose(Repository.<GetCollectionResponse>applyErrorHandling());
//                .filter(new Predicate<GetNewAndResponse>() {
//                    @Override
//                    public boolean test(GetNewAndResponse getNewAndResponse) throws Exception {
//                        Logger.i(TAG, "postNewAnd test " + MiscUtils.toJSONString(getNewAndResponse));
//                        return TextUtils.equals(getNewAndResponse.getAdType(), Constants.TAG_AD_TYPE_ICON) ||
//                                TextUtils.equals(getNewAndResponse.getAdType(), Constants.TAG_AD_TYPE_BANNER) ||
//                                TextUtils.equals(getNewAndResponse.getAdType(), Constants.TAG_AD_TYPE_BIG_VIEW);
//                    }
//                }).toSingle();
    }

    /**
     * Get Lbs File's Url
     * －－－－－－棄用邏輯－－－－－－
     *
     * @param request
     * @return
     */
    public static Single<GetLbsFileResponseUrl> getLbsFileUrl(GetLbsFileRequest request) {
        return AdLocusAPI.getInstance()
                .getLbsFileUrl(request)
                .compose(Repository.<GetLbsFileResponseUrl>applyErrorHandling());
    }

    /**
     * Get Lbs Task list
     *
     * @param request
     * @return
     */
    public static Single<GetLbsTaskResponse> getLbsTask(GetLbsFileRequest request) {
        return AdLocusAPI.getInstance()
                .getLbsTask(request)
                .compose(Repository.<GetLbsTaskResponse>applyErrorHandling());
    }

    /**
     * Get Google Ad Id by background thread
     *
     * @param context
     * @return
     */
    public static Single<String> getGoogleADID(final Context context) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> e) throws Exception {
                String id = AdLocusUtil.getEncodedGoogleADId(context);
                Logger.d(TAG, "[getGoogleADID] -> " + id);
                e.onSuccess(id);
            }
        }).subscribeOn(Schedulers.newThread());

    }
    /*--------------------------------------------------------------------------------------------*/
    /* Local Preferences */


    /**
     * Set User BaseData
     *
     * @param pushTokenData Detail in "PushTokenRequest.class"
     */
    public static Single<Boolean> setUserBaseData(final PushTokenRequest pushTokenData) {
        Logger.i(TAG, "[Method] -> setUserBaseData()");
        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[setUserBaseData] context is null");
            return Single.error(new Throwable("[setUserBaseData] context is null"));
        }
        context = mContextRef.get();
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        return getGoogleADID(context).flatMap(new Function<String, SingleSource<Boolean>>() {
            @Override
            public SingleSource<Boolean> apply(String s) throws Exception {

                int userStatement = getUserAndroidIdState();
                        /*
                        因為Device ID不會變，所以不需要一直重複儲存，所以只需要檢查在暫存裡面的Device Id是否為空，若為空則存進去。

                        若使用者願意讓app使用android id，則讓 KEY_HASH_DEVICE_ID 欄位與 KEY_DEVICE_ID 欄位都放原本的data
                        若不允許則使用Google Ad id
                        */
                if (MiscUtils.checkSharedStringIsEmpty(context, KEY_HASH_DEVICE_ID)) {
                    pref.edit().putString(KEY_HASH_DEVICE_ID, userStatement == Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT ? AdLocusUtil.getEncodeDeviceId(context) : s).apply();
                }
                if (MiscUtils.checkSharedStringIsEmpty(context, KEY_GOOGLE_AD_ID)) {
                    pref.edit().putString(KEY_GOOGLE_AD_ID, AdLocusUtil.getAndGoogleADID(context)).apply();
                }
                if (MiscUtils.checkSharedStringIsEmpty(context, KEY_DEVICE_MAC))
                    pref.edit().putString(KEY_DEVICE_MAC, userStatement == Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT ?AdLocusUtil.getMac(context):"no_access").apply();
                if (MiscUtils.checkSharedStringIsEmpty(context, KEY_DEVICE_MODEL))
                    pref.edit().putString(KEY_DEVICE_MODEL, Build.MODEL).apply();
                if (MiscUtils.checkSharedStringIsEmpty(context, KEY_APP_PACKAGE_NAME) && !TextUtils.isEmpty(pushTokenData.getAppPackageName()))
                    pref.edit().putString(KEY_APP_PACKAGE_NAME, pushTokenData.getAppPackageName()).apply();

                //有可能改變的資料重新儲存
                if (!MiscUtils.checkSharedStringEqual(context, KEY_SDK_VERSION, AdLocusUtil.MAC_SDK_VERSION))
                    pref.edit().putString(KEY_SDK_VERSION, AdLocusUtil.MAC_SDK_VERSION).apply();
                if (!TextUtils.isEmpty(pushTokenData.getAppKey()))
                    pref.edit().putString(KEY_APP_KEY, pushTokenData.getAppKey()).apply();
                if (!TextUtils.isEmpty(pushTokenData.getFcmAppKey()))
                    pref.edit().putString(KEY_FCM_APP_KEY, pushTokenData.getFcmAppKey()).apply();

                //比對 SharedPreference 裡的FCM Token若相同則不再次存入
                String pushToken = pushTokenData.getPushToken();
                if (!TextUtils.isEmpty(pushToken)) {
                    if (!MiscUtils.checkSharedStringEqual(context, KEY_FIREBASE_TOKEN, pushToken))
                        pref.edit().putString(KEY_FIREBASE_TOKEN, pushToken).apply();
                } else
                    return Single.error(new ApiException(ApiStatus.ERROR_DATA_ERROR, "Push token is null"));

                return Single.just(true);
            }
        });
    }

    /**
     * Save PushToken to sharedPreference
     *
     * @param context
     * @param pushToken
     */
    public static boolean setPushToken(Context context, String pushToken) {
        Logger.i(TAG, "[Method] -> setPushToken()");
        if (TextUtils.isEmpty(pushToken)) {
            Logger.e(TAG, "[setPushToken] pushToken is empty");
            return false;
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(KEY_FIREBASE_TOKEN, pushToken).apply();
        return true;
    }
    public static String getPushToken(Context context, String defult) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(KEY_FIREBASE_TOKEN, defult);
    }

    /**
     * Get All User base Data
     *
     * @return PushTokenRequest Object
     */
    public static PushTokenRequest getUserBaseData() {
        Logger.i(TAG, "[Method] -> getUserBaseData()");
        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[getUserBaseData] context is null");
            return null;
        }
        context = mContextRef.get();
        PhoneCellUtil cgi = new PhoneCellUtil(context);

        PushTokenRequest result = new PushTokenRequest();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        result.setPushToken(pref.getString(KEY_FIREBASE_TOKEN, ""));
        result.setDeviceId(getHashDeviceId());
        result.setSdkVersion(pref.getString(KEY_SDK_VERSION, ""));
        result.setAppKey(getAppKey());
        result.setDeviceMac(pref.getString(KEY_DEVICE_MAC, ""));
        result.setDeviceModel(pref.getString(KEY_DEVICE_MODEL, ""));
        result.setFcmAppKey(pref.getString(KEY_FCM_APP_KEY, ""));
        result.setAppPackageName(pref.getString(KEY_APP_PACKAGE_NAME, ""));
        result.setMcc(cgi.getMcc());
        result.setMnc(cgi.getMnc());
        result.setD_ad_id(AdLocusUtil.getEncodedGoogleADId(context));
        return result;
    }

    /**
     * Get Hyxen App Key from sharedPreference
     *
     * @return
     */
    public static String getAppKey() {
        Logger.i(TAG, "[Method] -> getAppKey()");
        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[getAppKey] context is null");
            return "";
        }
        context = mContextRef.get();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(KEY_APP_KEY, "");
    }

    /**
     * Get not hash Google Ad ID
     *
     * @return
     */
    public static String getGoogleAdId() {
        Logger.i(TAG, "[Method] -> getGoogleAdId()");
        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[getGoogleAdId] context is null");
            return "";
        }
        context = mContextRef.get();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(KEY_GOOGLE_AD_ID, "");
    }

    /**
     * 取得Hash過的DeviceID form Cache
     * 若使用者同意給App使用Android ID 則實際內容為Android ID
     * 否則為Google Ad Id
     *
     * @return
     */
    public static String getHashDeviceId() {
        Logger.i(TAG, "[Method] -> getHashDeviceId()");
        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[getHashDeviceId] context is null");
            return "";
        }
        context = mContextRef.get();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(KEY_HASH_DEVICE_ID, "");
    }

    /**
     * If Register success
     * set this tag
     */
    public static void setRegisterSuccess() {
        Logger.i(TAG, "[Method] -> setRegisterSuccess()");
        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[setRegisterSuccess] context is null");
            return;
        }
        context = mContextRef.get();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(KEY_REGISTER_STATE, true).apply();
    }

    /**
     * Get Register success state
     *
     * @return
     */
    public static boolean getRegisterState() {
        Logger.i(TAG, "[Method] -> getRegisterState()");
        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[getRegisterState] context is null");
            return false;
        }
        context = mContextRef.get();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(KEY_REGISTER_STATE, false);
    }

    /**
     * Set Lbs Task Json
     *
     * @param taskJson
     */
    public static void setLbsTaskJson(@NonNull String taskJson) {
        Logger.i(TAG, "[Method] -> setLbsTaskJson()");
        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[setLbsTaskJson] context is null");
            return;
        }
        context = mContextRef.get();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(KEY_LBS_TASK_JSON, taskJson).apply();
    }

    /**
     * Get Lbs Task Json
     *
     * @return
     */
    public static String getLbsTaskJson(Context context) {
        Logger.i(TAG, "[Method] -> getLbsTaskJson()");
//        final Context context;
//        if (mContextRef == null || mContextRef.get() == null) {
//            Logger.e(TAG, "[getLbsTaskJson] context is null");
//            return "";
//        }
//        context = mContextRef.get();
        if(context==null){
            Logger.e(TAG, "[getLbsTaskJson] context is null");
            return "";
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(KEY_LBS_TASK_JSON, "");
    }

    /**
     * Get App Package Name
     *
     * @return
     */
    public static String getAppPackageMame() {
        Logger.i(TAG, "[Method] -> getAppPackageMame()");
        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[getAppPackageMame] context is null");
            return "";
        }
        context = mContextRef.get();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(KEY_APP_PACKAGE_NAME, "");
    }

    /**
     * 判斷LBS data 是否為空
     *
     * @return
     */
    public static Single<GetLbsTaskResponse> checkLbsDataIsEmpty(Context context) {
        return Single.just(getLbsTaskJson(context))
                .flatMap(new Function<String, Single<GetLbsTaskResponse>>() {
                    @Override
                    public Single<GetLbsTaskResponse> apply(String s) throws Exception {
                        GetLbsTaskResponse lbsJson = null;
                        if (!TextUtils.isEmpty(s)) {
                            lbsJson = MiscUtils.parseJSON(s, GetLbsTaskResponse.class);
                            if (lbsJson == null || lbsJson.getPt() == null) {
                                return Single.error(new Throwable("lbsJson.getPt is null"));
                            }
                        } else {
                            return Single.error(new Throwable("input is null"));
                        }

                        return Single.just(lbsJson);
                    }
                });
    }

    /**
     * Set user grant app use Android Id
     *
     * @param state Constants.TAG_ANDROID_ID_STATEMENT_STATE_FIRST : 還沒有設置過是否要讓app使用android id
     *              Constants.TAG_ANDROID_ID_STATEMENT_STATE_DENIED : 使用者拒絕 app使用android id
     *              Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT: 使用者同意 app使用android id
     */
    public static void setUserAndroidIdState(int state) {
        Logger.i(TAG, "[Method] -> setUserAndroidIdState()");
        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[setUserAndroidIdState] context is null");
            return;
        }
        context = mContextRef.get();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putInt(KEY_ANDROID_ID_USER_STATE, state).apply();
    }

    /**
     * Get user grant state
     *
     * @return 0：使用者還沒設置過
     * 1：使用者同意
     * 2：使用者拒絕
     */
    public static int getUserAndroidIdState() {
        Logger.i(TAG, "[Method] -> getUserAndroidIdState()");
        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[getUserAndroidIdState] context is null");
            return Constants.TAG_ANDROID_ID_STATEMENT_STATE_DENIED;
        }
        context = mContextRef.get();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(KEY_ANDROID_ID_USER_STATE, Constants.TAG_ANDROID_ID_STATEMENT_STATE_FIRST);
    }

    // TODO: 2018/4/30 Check是否需要暫存FCM Message
    public static void setFCMMessage(String fcmMessage) {
        Logger.i(TAG, "[Method] -> sendFCMMessage()");

        final Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.e(TAG, "[sendFCMMessage] context is null");
            return;
        }
        context = mContextRef.get();

        if (TextUtils.isEmpty(fcmMessage)) {
            Logger.e(TAG, "[sendFCMMessage] fcmMessage is null");
            return;
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(KEY_FCM_MESSAGE, fcmMessage).apply();
    }

    /*--------------------------------------------------------------------------------------------*/
    /* Applying transformers */
    @SuppressWarnings("unchecked")
    private static <T> SingleTransformer<T, T> applyErrorHandling() {
        return sRemoteErrorHandler;
    }

    private static SingleTransformer<GetNewAndResponse, GetNewAndResponse> applyNeoQuestion() {
        return new SingleTransformer<GetNewAndResponse, GetNewAndResponse>() {
            @Override
            public SingleSource<GetNewAndResponse> apply(Single<GetNewAndResponse> upstream) {
                return upstream.doOnSuccess(new Consumer<GetNewAndResponse>() {
                    @Override
                    public void accept(GetNewAndResponse getNewAndResponse) throws Exception {
                    }
                });
            }
        };
    }


    /*--------------------------------------------------------------------------------------------*/
    /* Errors */

    private static <T> Single<T> illegalContext() {
        int code = ApiStatus.ERROR_CONTEXT;
        String message = "application finished";
        return Single.error(new ApiException(code, message));
    }

    /*--------------------------------------------------------------------------------------------*/
    /* In memory cache */

    /*--------------------------------------------------------------------------------------------*/
    /* Internal helpers */

    /*--------------------------------------------------------------------------------------------*/
    /* Transformer */

    /**
     * Series operators to handle every error occurs during remote api calls.
     */
    private static class RemoteErrorHandler implements SingleTransformer<Object, Object> {
        private static final String TAG = RemoteErrorHandler.class.getSimpleName();

        @Override
        public SingleSource<Object> apply(Single<Object> upstream) {
            return upstream
                    .flatMap(new Function<Object, SingleSource<Object>>() {//判斷steam input 是否正確
                        @Override
                        public SingleSource<Object> apply(@NonNull Object o) throws Exception {
                            final Context context;
                            if (mContextRef == null || mContextRef.get() == null) {
                                Logger.e(TAG, "[apply] context is null");
                                return illegalContext();
                            }
                            context = mContextRef.get();
                            if (o == null) {
                                Logger.e(TAG, "Empty response!");

                                int code = ApiStatus.UNEXPECTED_RESPONSE;
                                String message = context.getString(R.string.common_error_unexpected_response);
                                ApiException exception = new ApiException(code, message);
                                return Single.error(exception);
                            }

                            return Single.just(o);
                        }
                    })
                    .flatMap(
                            new Function<Object, SingleSource<Object>>() {//判斷server response 是否正確
                                @Override
                                public SingleSource<Object> apply(@NonNull Object o) throws Exception {
                                    Context context = mContextRef.get();
                                    if (context == null) {
                                        Logger.e(TAG, "[apply] context is null");
                                        return illegalContext();
                                    }
                                    RemoteResponse resp = null;
                                    if (o instanceof RemoteResponse) {
                                        resp = (RemoteResponse) o;
                                    }
                                    if (resp != null) {
                                        String responseJson = MiscUtils.toJSONString(resp);
//                                        if (!resp.isSuccess()) {
//                                            responseJson="{\"ad_body\":\"原來大家都一樣？上班族的一日寫照…\",\"ad_icon\":\"http://s3-ap-northeast-1.amazonaws.com/adlocus-ad-pics/camp_2018/1b2e9781cbcc35c1315baf09bdd6b49fdfda9bfd1529915374.jpg\",\"ad_id\":\"152994417920651055\",\"ad_left_icon\":\"0\",\"ad_link\":\"http://1.ad-locus.com/dev/redirect/15299441792065/and/a34bf96563da39e35791c28c0ca44da8e0404154/5f9706f6f361550b59f6019082bc8963539e00e9e3325455710b3e02fa8750e86cbdc352776d3f34/15307028456305/native/0/1.10\",\"ad_native_text\":\"上班族的一日寫照…\",\"ad_title\":\"原來大家都一樣？\",\"ad_type\":\"1\",\"sid\":\"15306740449192\",\"track_imp\":\"\",\"err\":\"0\"}";
//                                            resp=MiscUtils.parseJSON(responseJson,RemoteResponse.class);
//                                        }
                                        if (!resp.isSuccess()) {
                                            Logger.e(TAG, "Unsuccessful response! resp = " + responseJson);
                                            int code = Integer.parseInt(resp.getErr());
                                            String message = resp.getErrMsg();
                                            ApiException exception = new ApiException(code, message);
                                            return Single.error(exception);
                                        } else {
                                            Logger.i(TAG, "response! resp = " + responseJson);
                                        }
                                    }
                                    return Single.just(o);
                                }
                            })
                    .onErrorResumeNext(new Function<Throwable, SingleSource<Object>>() {
                        @Override
                        public SingleSource<Object> apply(@NonNull Throwable throwable) throws Exception {
                            final Context context;
                            if (mContextRef == null || mContextRef.get() == null) {
                                Logger.e(TAG, "[apply] context is null");
                                return illegalContext();
                            }
                            context = mContextRef.get();
                            ApiException result;

                            if (throwable instanceof ApiException) {
                                result = (ApiException) throwable;
                            } else if (throwable instanceof HttpException) {
                                Logger.e(TAG, "HttpException!");
//                                HttpException error = (HttpException) throwable;
//                                error.code();
//                                String s=error.response().errorBody().string();

                                result = new ApiException(ApiStatus.NO_NETWORK, context.getString(R.string.common_error_server));
                            } else if (throwable instanceof SocketTimeoutException) {
                                Logger.e(TAG, "SocketTimeoutException!", throwable);

                                result = new ApiException(ApiStatus.NETWORK_TIMEOUT, context.getString(R.string.common_error_time_out));
                            } else if (throwable instanceof ConnectException || throwable instanceof UnknownHostException) {
                                Logger.e(TAG, "ConnectException!", throwable);

                                result = new ApiException(ApiStatus.NO_NETWORK, context.getString(R.string.common_error_no_network));
                            } else if (throwable instanceof JsonParseException) {
                                Logger.e(TAG, "Json Parse error!", throwable);

                                result = new ApiException(ApiStatus.JSON_PARSE_ERROR, context.getString(R.string.common_error_json_parse));
                            } else {
                                Logger.e(TAG, "Unexpected error!", throwable);

                                result = new ApiException(ApiStatus.UNKNOWN_ERROR, context.getString(R.string.common_error_unexpected));
                            }

                            return Single.error(result);
                        }
                    });
        }
    }
}
