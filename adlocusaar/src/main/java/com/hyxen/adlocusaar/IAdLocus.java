package com.hyxen.adlocusaar;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Map;

public interface IAdLocus {
    /**
     * register APP to Server
     */
    void registerApp();

    /**
     * Update Push Token
     * Do it, in refresh FCM Token class
     *
     * @param fcmToken
     */
    void updatePushToken(@NonNull String fcmToken);

    /**
     * Send the message received by FCM
     *
     * @param fcmMessage
     */
    void sendFCMMessage(Context ctx, @NonNull Map<String, String> fcmMessage);

    /**
     * Check User Statement
     *
     * @param fcmToken
     * @param fcmAppKey
     * @param appPackageName
     * @param appKey
     */
    void checkUserStatement(String fcmToken, @NonNull String fcmAppKey, @NonNull String appPackageName, @NonNull String appKey);
}
