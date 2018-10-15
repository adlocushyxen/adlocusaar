package com.hyxen.adlocusaar.engine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WifiInfo implements Comparable<WifiInfo>
{
	public static final int SERV_CONNECTED = 1;
	public static final int SERV_NO_CONNECTION = 0;
	private int serv;
	private String name;

	private String mac;
	private int rssi = 0;

	public void setServ(int serv)
	{
		this.serv = serv;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setMac(String mac)
	{
		this.mac = mac;
	}

	public void setRssi(int rssi)
	{
		this.rssi = rssi;
	}
	
	public int getServ()
	{
		return serv;
	}

	public String getName()
	{
		return name;
	}

	public String getMac()
	{
		return mac;
	}

	public int getRssi()
	{
		return rssi;
	}
	
	public static String getJsonString(WifiInfo[] wifiInfos)
	{
		if(wifiInfos == null)
			return "";
		try
		{
			JSONArray a = new JSONArray();
			for (WifiInfo info : wifiInfos)
			{
				JSONObject o = new JSONObject();
				o.putOpt("mac", info.mac);
				o.putOpt("rssi", info.rssi);
				a.put(o);
			}
			JSONObject o = new JSONObject();
			o.putOpt("wifi", a);
			return o.toString();
		}
		catch (JSONException ignored)
		{
		}
		return "";
	}

	@Override
	public int compareTo(WifiInfo another)
	{
		if(serv == SERV_CONNECTED)
		{
			return -100000;
		}
		else if(another.serv == SERV_CONNECTED)
		{
			return 100000;
		}
		else
		{
			return Math.abs(rssi) - Math.abs(another.rssi);
		}
	}
}
