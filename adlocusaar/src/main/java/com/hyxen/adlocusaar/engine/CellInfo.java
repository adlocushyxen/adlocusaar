package com.hyxen.adlocusaar.engine;

import org.json.JSONException;
import org.json.JSONObject;

import com.hyxen.adlocusaar.util.Util;

import android.telephony.TelephonyManager;

public class CellInfo
{
	public static final int CELL_TYPE_GSM = 1;
	public static final int CELL_TYPE_WCDMA = 2;
	public static final int CELL_TYPE_LTE = 3;
	public static final int CELL_TYPE_CDMA = 4;
	public static final int CELL_TYPE_EVDO = 5;
	
	public static final String STATUS_CELL_SCAN_SUCCESS = "00";

	public static final int RADIO_TYPE_CELL = 1;
	
	private int mType = 1;

	private int mMcc = -1;
	private int mMnc = -1;
	private int mLac = -1;
	private int mCellID = -1;
	private int mBsic = -1;
	private int mRssi = 0;
	private long mTimespan;
	
	private int mSid = -1;
	private int mLat = Integer.MAX_VALUE;
	private int mLon = Integer.MAX_VALUE;

	private String mImsi = Util.formatInt(0, 10, 15);
	
	private boolean isNeighboring = false;
	
	public CellInfo()
	{
		mTimespan = System.currentTimeMillis();
	}
	
	void setNeighboring()
	{
		isNeighboring = true;
	}
	
	public boolean isNeighbroing()
	{
		return isNeighboring;
	}
	
	public String getHexString()
	{
        return getCiHexString() + Util.formatInt(0, 10, 15);
	}
	
	/**
	 * 
	 * @param locationType 
	 * @param accuracy
	 * @param appid
	 * @return
	 */
	public String getHexString(int locationType, int accuracy, int appid)
	{
		StringBuilder buf = new StringBuilder(getCiHexString());
		switch (locationType) {
		case 1:
		case 2:
			buf.append(locationType);
			break;
		default:
			buf.append(0);
			break;
		}
		buf.append(Util.formatInt(accuracy, 16, 4));
		buf.append(Util.formatInt(appid, 16, 2));
        buf.append(Util.formatInt(mSid, 16, 8));
        buf.append(Util.formatInt(mLat, 16, 8));
        buf.append(Util.formatInt(mLon, 16, 8));
        buf.append(Util.formatInt(0, 10, 16));
        return buf.toString();
	}
	
	private String getCiHexString()
	{
		StringBuilder buf = new StringBuilder();
        
        buf.append(RADIO_TYPE_CELL).append(mType).append(STATUS_CELL_SCAN_SUCCESS);
        buf.append(Util.formatInt(mMcc, 10, 3));	// mcc
        buf.append(Util.formatInt(mMnc, 10, 3));	// mnc
        buf.append(Util.formatInt(mLac, 16, 4));	// lac
        buf.append(Util.formatInt(mCellID, 16, 4));	// cellid
        buf.append(Util.formatInt(mBsic, 16, 4));	// bsic

        int rssi = mRssi;
        if (rssi < 0) rssi += 110;
        buf.append(Util.formatInt(rssi, 16, 4));	// rssi
        buf.append("FF");
//
//        String imsi = null;
//        if (_imsi == null) imsi = Util.formatInt(0, 10, 15);
//        else
//        {
//	        String res = Util.BCDToString(_imsi);
//			if (res.length() == 14) imsi = res + "0";
//			else if (res.length() > 14) imsi = res.substring(0, 15);
//        }
//        
        buf.append(mImsi);
        return buf.toString();
	}
	
	public long getIV()
	{
		return mLac * 65536l + mCellID;
	}
	
	public String getIdentify()
	{
		return String.valueOf(mMcc) + mMnc + mLac + mCellID;
	}
	
	@Override
	public String toString()
	{
		JSONObject o = new JSONObject();
		try
		{
			o.put("Type", mType);
			o.put("Mcc", mMcc);
			o.put("Mnc", mMnc);
			o.put("Lac", mLac);
			o.put("CellID", mCellID);
			o.put("Rssi", mRssi);
			o.put("Bsic", mBsic);
			return o.toString(4);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return super.toString();
		}
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof CellInfo)
		{
			CellInfo ci = (CellInfo) obj;
			return (mMcc == ci.mMcc && mMnc == ci.mMnc && mLac == ci.mLac && mCellID == ci.mCellID);
		}
		else if (obj instanceof String)
		{
			String cs = (String) obj;
			return getHexString().equalsIgnoreCase(cs);
		}
		else return super.equals(obj);
	}
	
	@Override
	public CellInfo clone()
	{
		CellInfo ci = new CellInfo();
		ci.mType = mType;
		ci.mBsic = mBsic;
		ci.mCellID = mCellID;
		ci.mImsi = mImsi;
		ci.mLac = mLac;
		ci.mMcc = mMcc;
		ci.mMnc = mMnc;
		ci.mRssi = mRssi;
		ci.mTimespan = mTimespan;
		ci.mSid = mSid;
		ci.mLat = mLat;
		ci.mLon = mLon;
		ci.isNeighboring = ci.isNeighboring;
		return ci;
	}
	
	public boolean isValid()
	{
		if(mMcc < 0 || mMnc < 0 || mCellID < 0 || mLac < 0)
		{
			return false;
		}
		else if (mType == CELL_TYPE_CDMA)
		{
			if(mSid == -1 || mLat == Integer.MAX_VALUE || mLon == Integer.MAX_VALUE)
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean isValidRssi() {
		return mRssi < 0 && isValid();
	}
	
	/**
	 * 
	 * @return LocationConstants CELL_TYPE_GSM、CELL_TYPE_WCDMA、CELL_TYPE_LTE、CELL_TYPE_CDMA、CELL_TYPE_EVDO
	 */
	public int getType()
	{
		return mType;
	}

	public String getTypeName()
	{
		switch (mType)
		{
			case CELL_TYPE_GSM:
				return "GSM";
			case CELL_TYPE_WCDMA:
				return "WCDMA";
			case CELL_TYPE_LTE:
				return "LTE";
			case CELL_TYPE_CDMA:
				return "CDMA";
			default:
				return "UNKNOW";
		}
	}
	
	/**
	 * 
	 * @param type LocationConstants CELL_TYPE_GSM、CELL_TYPE_WCDMA、CELL_TYPE_LTE、CELL_TYPE_CDMA、CELL_TYPE_EVDO
	 */
	public void setType(int type)
	{
		this.mType = type;
	}
	
	public void setTypeWithNetworktype(int networkType, int phoneType)
	{
		switch (networkType)
		{
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case 12:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				mType = CELL_TYPE_CDMA;
				break;
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
				mType = CELL_TYPE_GSM;
				break;
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_UMTS:
				mType = CELL_TYPE_WCDMA;
				break;
			case 13:
				mType = CELL_TYPE_LTE;
				break;
			default:
				if(phoneType == TelephonyManager.PHONE_TYPE_CDMA)
				{
					mType = CELL_TYPE_CDMA;
				}
				else if(phoneType == TelephonyManager.PHONE_TYPE_GSM)
				{
					mType = CELL_TYPE_GSM;
				}
				else
				{
					mType = 9;
				}
				break;
		}
	}
	
	public int getMcc()
	{
		return mMcc;
	}
	
	public int getMnc()
	{
		return mMnc;
	}
	
	public int getLac()
	{
		return mLac;
	}
	
	public int getCellID()
	{
		return mCellID % 65536;
	}
	
	public int getBsic()
	{
		return mBsic;
	}
	
	public int getRssi()
	{
		return mRssi;
	}
	
	public String getImsi()
	{
		return mImsi;
	}
	
	public long getTimespan()
	{
		return mTimespan;
	}
	
	public void setTimespan(long ts)
	{
		mTimespan = ts;
	}
	
	public void setMcc(int mcc)
	{
		mMcc = mcc;
	}
	
	public void setMnc(int mnc)
	{
		mMnc = mnc;
	}
	
	public void setLac(int lac)
	{
		mLac = lac;
	}
	
	public void setCellID(int cellId)
	{
		mCellID = cellId;
	}
	
	public void setBsic(int bsic)
	{
		mBsic = bsic;
	}
	
	public void setRssi(int rssi)
	{
		mRssi = rssi;
	}
	public int getSid()
	{
		return mSid;
	}

	public void setSid(int sid)
	{
		this.mSid = sid;
	}

	public int getLat()
	{
		return mLat;
	}

	public void setLat(int lat)
	{
		this.mLat = lat;
	}

	public int getLon()
	{
		return mLon;
	}

	public void setLon(int lon)
	{
		this.mLon = lon;
	}
}
