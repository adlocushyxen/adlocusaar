package com.hyxen.adlocusaar.repository.remote.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import com.hyxen.adlocusaar.utils.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

public class HxRequest implements Runnable {

    private static final int TIMEOUT = 10000;

    public enum Method {
        GET,
        POST,
        AUTO
    }


    private boolean mIsError = false;
    private int mErrorCode = -999;
    protected String mResult = null;
    private byte[] mResultByteArray = null;

    private final HashMap<String, String> mPostParameters = new HashMap<>();

    private String mUrl;
    private int mTimeout = -1;
    private int mRetryTimes = 1;
    private RequestListener mListener = null;

    private WeakReference<Context> mContextRef;

    private Method mMethod = Method.AUTO;
    private final Object mLock = new Object();
    private boolean mIsReturnByteArray = false;

    /**
     * 設定Request 方法
     *
     * @param method default is AUTO
     */
    public void setMethod(Method method) {
        mMethod = method;
    }

    public HxRequest(Context context, String url) {
        mContextRef = new WeakReference<Context>(context);
        mUrl = url;
    }

    public HxRequest setRetryTimes(int times) {
        mRetryTimes = times;
        if (mRetryTimes < 1)
            mRetryTimes = 1;
        return this;
    }

    private HttpURLConnection mConnection = null;
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);

    @Override
    public void run() {
        String result = null;
        int i = 0;
        while (true) {
            if (i++ >= (mRetryTimes)) break;
            mErrorCode = -999;
            mIsError = false;

            String strUrl = getUrl();
            try {
                URL url = new URL(strUrl);

                synchronized (mLock) {
                    mConnection = (HttpURLConnection) url.openConnection();
                }
                mConnection.setUseCaches(false);

                if (mTimeout != -1) {
                    mConnection.setConnectTimeout(mTimeout);
                    mConnection.setReadTimeout(mTimeout);
                }

                final boolean needPost = (mMethod != Method.GET) && (mPostParameters.size() > 0);
                if (needPost) {
                    mConnection.setDoOutput(true);
                    mConnection.setRequestMethod("POST");
                } else {
                    mConnection.setRequestMethod("GET");
                }


                mConnection.setRequestProperty("User-Agent", getUserAgent());

                if (!mPostParameters.containsKey("accept-charset"))
                    mConnection.setRequestProperty("Accept-Charset", "UTF-8");
                if (!mPostParameters.containsKey("content-type"))
                    mConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + "UTF-8");

                mConnection.setRequestProperty("Accept-Encoding", "gzip");

                isConnecting.set(true);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(TIMEOUT); //TIMEOUT為逾時的時間(毫秒)
                            while (isConnecting.get()) { //不斷嘗試斷開連結，直到連結真的完全斷開
                                mConnection.disconnect();
                                Thread.sleep(200);
                            }
                            synchronized (mLock) {
                                mConnection = null;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.start();

                if (needPost) {
                    final byte[] postData = getPostString().getBytes("UTF-8");
                    mConnection.setRequestProperty("Content-Length", String.valueOf(postData.length));
                    postData(postData, new BufferedOutputStream(mConnection.getOutputStream()));
                }
                mErrorCode = mConnection.getResponseCode();
                switch (mErrorCode) {
                    case HttpURLConnection.HTTP_OK: {
                        InputStream in = new BufferedInputStream(mConnection.getInputStream());
                        if ("gzip".equalsIgnoreCase(mConnection.getContentEncoding())) {
                            Logger.d("use gzip");
                            in = new GZIPInputStream(in);
                        }
                        if (mIsReturnByteArray) {
                            mResultByteArray = HxEncrypt.getByte(in);
                        } else {
                            result = readInputStream(in);
                        }

                        mIsError = false;
                        Logger.d(strUrl + "\n" + getPostString() + "\n" + (mIsReturnByteArray ? "byte length=" + mResultByteArray.length : result));
                        break;
                    }
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        mUrl = mConnection.getHeaderField("Location");
                        HxRequest.this.run();
                        return;

                    default:
                        mIsError = true;
                        Logger.d(strUrl + "\n" + getPostString() + "\nerror:" + mErrorCode);
                        break;
                }
            } catch (SocketException e) {
                mErrorCode = -3;
                mIsError = true;
                Logger.e(strUrl + "\n" + getPostString(), e.toString());
            } catch (Exception e) {
                mErrorCode = -2;
                mIsError = true;
                Logger.e(strUrl + "\n" + getPostString(), e.toString());
            } finally {
                isConnecting.set(false);
            }
        }
        mResult = result;
        if (mListener != null) {
            mListener.processContent(mErrorCode, result);
        }
        processContent(mErrorCode, result);
    }

    private String getUserAgent() {
        Context context = mContextRef.get();
        String userAgent = System.getProperty("http.agent");
        if (context != null)
            userAgent += " " + context.getPackageName();
        userAgent += " " + Build.MANUFACTURER;

        return userAgent;
    }


    private String readInputStream(InputStream inputStream) throws IOException {

        String contentType = mConnection.getContentType();
        String[] values = contentType.split(";"); //The values.length must be equal to 2...
        String charset = "UTF-8";
        for (String value : values) {
            value = value.trim();
            if (value.toLowerCase().startsWith("charset=")) {
                charset = value.substring("charset=".length());
            }
        }


        final BufferedInputStream bis = new BufferedInputStream(inputStream);
        final ByteArrayOutputStream bao = new ByteArrayOutputStream();
        int c;
        byte[] buffer = new byte[2048];
        while ((c = bis.read(buffer)) >= 0) {
            bao.write(buffer, 0, c);
        }
        bis.close();
        String text = new String(bao.toByteArray(), charset);
        bao.close();


        return text;
    }


    private void postData(byte[] postData, OutputStream outputStream) throws IOException {
        final int SIZE = postData.length;
        final int brock = 1024;
        int index = 0;
        while (index < SIZE) {
            int count;
            if (index + brock > SIZE) {
                count = SIZE % brock;
            } else {
                count = brock;
            }
            outputStream.write(postData, index, count);
            index += brock;
            outputStream.flush();
        }
        outputStream.close();
    }


    public void setGetByte() {
        mIsReturnByteArray = true;
    }

    public String getPostString() {
        StringBuilder sb = new StringBuilder();

        for (Entry<String, String> entry : mPostParameters.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null)
                continue;
            try {
                sb.append(entry.getKey().trim())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8").trim())
                        .append("&");
            } catch (UnsupportedEncodingException ignored) {
            }
        }
        int l = sb.lastIndexOf("&");
        if (l != -1) {
            sb.deleteCharAt(l);
        }
        return sb.toString().trim();
    }


    public byte[] getResultByteArray() {
        return mResultByteArray;
    }

    public String getResult() {
        return mResult;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public boolean hasError() {
        return mIsError;
    }

    public HxRequest setConnectTimeout(int timeoutMillis) {
        mTimeout = timeoutMillis;
        return this;
    }

    public HxRequest setUrl(String url) {
        mUrl = url;
        return this;
    }

    public String getUrl() {

        if (mMethod == Method.GET && !getPostString().equals("")) {
            return String.format("%s?%s", mUrl, getPostString());
        } else {
            return mUrl;
        }
    }

    public HxRequest setPostParameter(String key, String value) {
        mPostParameters.put(key, value);
        return this;
    }

    protected void processContent(int errorCode, String content) {

    }

    public HxRequest setListener(RequestListener listener) {
        mListener = listener;
        return this;
    }
}
