package com.hyxen.adlocusaar.engine;

import android.content.Context;

import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.net.RequestListener;
import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class HxRequestService extends HxRequest
{

	private static final String KEY = "e2e4193b842bb054";
	private static final byte[] AES_KEY;

	static
	{
		byte[] s;
		try
		{
			s = KEY.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			s = KEY.getBytes();
		}
		AES_KEY = s;
	}

	private RequestListener mListener;

	private String mLogContent;

	public HxRequestService(Context context, String host, String uri)
	{
		super(context, String.format("http://%s/%s/%s", host, uri, AdLocusUtil.getPushKey(context)));
	}

	public HxRequestService(Context context, String host, String uri, String uri2)
	{
		super(context, String.format("http://%s/%s/%s/%s", host, uri, AdLocusUtil.getPushKey(context), uri2));
	}

	public HxRequestService setPostContent(String content)
	{
		mLogContent = content;
		try
		{
			setPostParameter("d", encrypt(AES_KEY, content.getBytes("UTF-8")));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return this;
	}

	public HxRequestService setEncryptPostContent(String content)
	{
		setPostParameter("d", content);
		return this;
	}

//	@Override
//	public HxRequest setShowLog(boolean isShow)
//	{
//		mIsShowLog = isShow;
//		return this;
//	}

	@Override
	public String getResult()
	{
		return mResult;
	}

	@Override
	protected void processContent(int errorCode, String content)
	{
		super.processContent(errorCode, content);
		int err = errorCode;
		if (content != null) {
			try {
				content = decrypt(AES_KEY, content);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(!hasError() && content != null && !content.equals(""))
		{
			mResult = content;
			if(mListener != null)
			{
				mListener.processContent(errorCode, content);
			}
//			logD(getUrl() + "\n" + mLogContent + "\n" + content);
			try
			{
				JSONObject o = new JSONObject(content);

				err = o.optInt("err", -999);
				if(err == 0)
				{
					onSuccess(o) ;
					return;
				}
			}
			catch (JSONException ignored) { }
		}
		else
		{
			if(mListener != null)
			{
				mListener.processContent(errorCode, null);
			}
		}
//		logE(getUrl() + "\npost encode:" + getPostString() + "\npost:" + mLogContent + "\nerr : " + err + "\ncontent:" + content);
		onError(err);
	}

	@Override
	public HxRequest setListener(RequestListener listener)
	{
		mListener = listener;
		return this;
	}

	public static String encrypt(byte[] key, byte[] encrypt) throws Exception
	{
		if(encrypt.length % 16 != 0)
		{ //not a multiple of 8
			//create a new array with a size which is a multiple of 8
			byte[] padded = new byte[encrypt.length + 16 - (encrypt.length % 16)];

			//copy the old array into it
			System.arraycopy(encrypt, 0, padded, 0, encrypt.length);
			encrypt = padded;
		}
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher =  Cipher.getInstance("AES/ECB/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(encrypt);
		return Base64.encodeToString(encrypted, Base64.NO_WRAP).trim();
	}

	public static String decrypt(byte[] key, String encryptedBase64) throws Exception
	{
		byte[] encrypted = Base64.decode(encryptedBase64.trim(), Base64.NO_PADDING);
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return new String(decrypted, "UTF-8").trim();
	}

	protected void onSuccess(JSONObject json)
	{

	}

	protected void onError(int err)
	{

	}
}
