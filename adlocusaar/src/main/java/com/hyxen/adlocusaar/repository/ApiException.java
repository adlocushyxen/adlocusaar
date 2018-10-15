package com.hyxen.adlocusaar.repository;

/**
 * An exception threw out when any error occurs through api call.
 */
public class ApiException extends Exception {

    private int mStatusCode;

    public ApiException(int code, String detailMessage) {
        super(detailMessage);
        mStatusCode = code;
    }

    public ApiException(int code, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        mStatusCode = code;
    }

    public int getCode() {
        return mStatusCode;
    }
}
