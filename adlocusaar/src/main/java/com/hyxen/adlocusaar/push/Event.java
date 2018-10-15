package com.hyxen.adlocusaar.push;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kiddchen on 2014/1/14.
 */
class Event
{
    public static final int STATUS_TRIGGER = 0;
    public static final int STATUS_TYPE3 = 1;

    public static final int TYPE_BROADCAST = 0;
    public static final int TYPE_CITY = 1;
    public static final int TYPE_POINT = 2;

    private final String mEventJson;
    private String mEventId;
    private String mSessionId;
    private String mInfo;
//    private int mStatus = STATUS_NON;

    private int mType = TYPE_BROADCAST;
    private long mBeginTs = 0;
    private long mEndTs = 0;
    
    private long mNextCheckHour = -1;

    /**
     * "bc":[ -- boardcast ad
     {
     "ad_id":"",
     "session_id":"",
     "begin_ts": xxxx, --unix timestamp
     "end_ts": xxxx,   --unix timestamp
     },
     {
     "ad_id":"",
     "session_id":"",
     "begin_ts": xxxx, --unix timestamp
     "end_ts": xxxx,   --unix timestamp
     }
     ],
     "city":[
     {
     "ad_id":"",
     "session_id":"",
     "begin_ts": xxxx, --unix timestamp
     "end_ts": xxxx,   --unix timestamp
     "city":"kaohsiung" -- refer to offline city mapping in sdk.
     }
     ],
     "pt":[
     {
     "ad_id":"",
     "dlpath":"",
     }
     ]
     * @param json
     */
    public Event(int type, String json)
    {
        mEventJson = json;
        mType = type;
        try
        {
            JSONObject o = new JSONObject(json);
            mEventId = o.optString("ad_id");
            mSessionId = o.optString("session_id");
            mBeginTs = o.optLong("begin_ts") * 1000;
            mEndTs = o.optLong("end_ts") * 1000;
            mNextCheckHour = o.optLong("next_check_hour", -1);
            switch (type)
            {
                case TYPE_BROADCAST:
                    break;
                case TYPE_CITY:
                    mInfo = o.optString("city");
                    break;
                case TYPE_POINT:
                    mInfo = o.optString("dlpath");
                    break;
            }
        }
        catch (JSONException ignored)
        {
        }
    }
    
    public void setCdHour(int hour)
    {
    	mNextCheckHour = System.currentTimeMillis() / 3600000 + hour;
    }

    public String getEventId(){ return mEventId; }

    public String getSessionId(){ return mSessionId; }

    public String getInfo(){ return mInfo; }

//    public int getStatus(){ return mStatus;  }

    public int getType(){ return mType; }

    public long getBeginTs(){ return mBeginTs; }

    public long getEndTs(){ return mEndTs; }

    public String getEventJson() {

        JSONObject o = new JSONObject();
        try
        {
            o.put("ad_id", mEventId);
            o.put("session_id", mSessionId);
            o.put("begin_ts", mBeginTs/ 1000);
            o.put("end_ts", mEndTs/1000);
            o.put("next_check_hour", mNextCheckHour);
            switch (mType)
            {
                case TYPE_BROADCAST:
                    break;
                case TYPE_CITY:
                    o.put("city", mInfo);
                    break;
                case TYPE_POINT:
                    o.put("dlpath", mInfo);
                    break;
            }
        }
        catch (JSONException ignored)
        {
        }
    	return o.toString(); 
    }
    
    public boolean isValidHour() {
        return mNextCheckHour == -1 || System.currentTimeMillis() / 3600000 >= mNextCheckHour;
    }

    @Override
    public int hashCode() 
    {
        if(mEventId == null)
        {
            return 0;
        }
        return mEventId.hashCode();
    }

    public final ArrayList<String> packages = new ArrayList<>();

    public boolean isValid()
    {
        long ts = System.currentTimeMillis();
        return (ts > mBeginTs && ts < mEndTs);
    }

    public boolean isExpired()
    {
        return System.currentTimeMillis() > mEndTs;
    }

    @Override
    public String toString()
    {
        return mBeginTs + " \n" + System.currentTimeMillis() + " \n" + mEndTs + "\n" + mEventJson;
    }

}

