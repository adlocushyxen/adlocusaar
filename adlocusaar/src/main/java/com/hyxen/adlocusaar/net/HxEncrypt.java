package com.hyxen.adlocusaar.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import com.hyxen.adlocusaar.util.Base64;

public class HxEncrypt
{

	public static String SHA(String str)
	{
		MessageDigest sha = null;

		try
		{
			sha = MessageDigest.getInstance("SHA-1");
			sha.update(str.getBytes());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
		return toHexString(sha.digest());
	}


	public static String md5(String str)
	{
		String result = "";
		try 
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			result = toHexString(md.digest());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return result;
	}

	private static String toHexString(byte[] in) 
	{
		StringBuilder hexString = new StringBuilder();
		for (byte anIn : in) {
			String hex = Integer.toHexString(0xFF & anIn);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		in = null;
		return hexString.toString();
	}

	public static byte[] base64ToBytes(String base64)
	{
		byte[] bytes = null;
		if(base64 != null && !"".equals(base64))
		{
			bytes = Base64.decode(base64, Base64.DEFAULT);
		}
		return bytes;
	}

	public static String streamToBase64(InputStream is) throws IOException
	{

		byte[] data = getByte(is);

		String res=Base64.encodeToString(data, Base64.DEFAULT);
		data = null;
		return res;
	}


	public static byte[] getByte(InputStream in)
	{
		if (in == null)
		{
			return null;
		}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = 0;
        byte[] buffer = new byte[8192];

		try
		{
	        while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1)
	        {
	            baos.write(buffer, 0, bytesRead);
	        }
	        return baos.toByteArray();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		buffer = null;
		System.gc();
//		int sumSize = 0;
//		List<byte[]> totalBytes = new ArrayList<byte[]>();
//		byte[] buffer = new byte[1024];
//		int length = -1;
//		try
//		{
//			while ((length = in.read(buffer)) != -1)
//			{
//				sumSize += length;
//				byte[] tmp = new byte[length];
//				System.arraycopy(buffer, 0, tmp, 0, length);
//				totalBytes.add(tmp);
//			}
//			byte[] data = new byte[sumSize];
//			int start = 0;
//			for (byte[] tmp : totalBytes)
//			{
//				System.arraycopy(tmp, 0, data, start, tmp.length);
//				start += tmp.length;
//			}
//			return data;
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
		return null;
	}
}
