package com.hyxen.adlocusaar.util;

public class Util
{
	public static String formatInt(int i, int radix, int len)
	{
		String s = null;
		if(radix == 16)
			s = Integer.toHexString(i);
		else
			s = Integer.toString(i, radix);
		int slen = s.length();
		if(slen > len){
			s = s.substring(0, len);
		 	return s;
		}
	 	StringBuilder buf = new StringBuilder();
	 	for (int j = 0; j < len - slen; j++)
	 		buf.append("0");
	 	buf.append(s);
	 	return buf.toString();
	}

	public static String BCDToString(byte[] bcd) {
	 	StringBuilder buf = new StringBuilder();
		for (byte ab : bcd) {
			buf.append(ab & 0X0F);
		}
	 	bcd = null;
	 	return buf.toString();
	}
}
