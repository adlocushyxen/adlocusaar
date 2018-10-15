package com.hyxen.adlocusaar.push;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Log;

import java.util.ArrayList;

/**
 * Created by kiddchen on 2014/1/14.
 */
public class EventDbAdapter
{

    public static final Object LOCK = new Object();
    public static final String DB_NAME = AdLocusUtil.PREFIX + "adlocus_event.db";
    private SQLiteDatabase mSQLiteDatabase = null;


    static final String TABLE_EVENT = "events";
    static final String COLUMN_EVENT_ID = "event_id";
    static final String COLUMN_JSON = "json";
    static final String COLUMN_TYPE = "type";
//    static final String COLUMN_BEGIN_TS = "begin_ts";
//    static final String COLUMN_END_TS = "end_ts";

    private static final String TABLE_PACKAGES = "ps";
    //has COLUMN_EVENT_ID
    private static final String COLUMN_PACKAGES = "ps";

    private static final String CREATE_TABLE_EVENT = "CREATE TABLE " + TABLE_EVENT
            + " (" + COLUMN_EVENT_ID + " TEXT PRIMARY KEY,"
            + COLUMN_TYPE			+ " INTEGER,"
            + COLUMN_JSON			+ " TEXT)";

    private static final String CREATE_TABLE_PS = "CREATE TABLE " + TABLE_PACKAGES
            + " ("
            + COLUMN_EVENT_ID + " TEXT,"
            + COLUMN_PACKAGES + " TEXT)";

    private static final String CREATE_INDEX_EVENT = "CREATE INDEX index_event ON " + TABLE_EVENT + " (" + COLUMN_EVENT_ID + " ASC)";

    private static final String CREATE_INDEX_PS = "CREATE INDEX index_ps ON " + TABLE_PACKAGES + " (" + COLUMN_EVENT_ID + ")";

    private static final String CREATE_UNIQUE_PS = "CREATE UNIQUE INDEX idx_ps ON " + TABLE_PACKAGES + "(" + COLUMN_EVENT_ID + "," + COLUMN_PACKAGES + ")";

    private DatabaseHelper mDBHelper = null;

//	private Context mContext;

    public class DatabaseHelper extends SQLiteOpenHelper
    {

        private static final int DB_VERSION = 1;
        public DatabaseHelper(Context context)
        {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_TABLE_EVENT);
            db.execSQL(CREATE_TABLE_PS);

            db.execSQL(CREATE_INDEX_EVENT);
            db.execSQL(CREATE_INDEX_PS);

            db.execSQL(CREATE_UNIQUE_PS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {

        }
    }

    public EventDbAdapter(Context context)
    {
        mDBHelper = new DatabaseHelper(context);
    }

    public void close()
    {
        mSQLiteDatabase.close();
    }

    public EventDbAdapter open() throws SQLException
    {
        mSQLiteDatabase = mDBHelper.getWritableDatabase();
        return this;
    }

    public long addEvent(int type, String eid, String json, String packageName)
    {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_EVENT_ID, eid);
        cv.put(COLUMN_PACKAGES, packageName);
        mSQLiteDatabase.insertWithOnConflict(TABLE_PACKAGES, null, cv, SQLiteDatabase.CONFLICT_IGNORE);

        cv = new ContentValues();
        cv.put(COLUMN_EVENT_ID, eid);
        cv.put(COLUMN_JSON, json);
        cv.put(COLUMN_TYPE, type);

        return mSQLiteDatabase.insertWithOnConflict(TABLE_EVENT, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public ArrayList<String> getEventPackages(String eventId)
    {
        Cursor c = mSQLiteDatabase.query(TABLE_PACKAGES, null, COLUMN_EVENT_ID + "=?", new String[]{eventId}, null, null, null);
        ArrayList<String> ret = new ArrayList<>();
        while (c.moveToNext())
        {
            ret.add(c.getString(c.getColumnIndex(COLUMN_PACKAGES)));
        }
        c.close();
        return ret;
    }

    public long getEventCount() throws Exception
    {
        Cursor c = mSQLiteDatabase.rawQuery("select * from sqlite_master where type= \'table\' and name= \'" + TABLE_EVENT + "\'", null);
        if(c == null)
        {
            return 0;
        }
        boolean hasTable = c.getCount() == 1;
        c.close();
        if(!hasTable)
        {
            return 0;
        }
        SQLiteStatement s = mSQLiteDatabase.compileStatement("select count(*) from " + TABLE_EVENT);
        long ret = s.simpleQueryForLong();
        s.close();
        return ret;
    }

    public Event getEventJson(String eid) throws Exception
    {
        String[] args = new String[]
                {
                        String.valueOf(eid)
                };
        Cursor cursor = mSQLiteDatabase.query(TABLE_EVENT, null, COLUMN_EVENT_ID + " = ?", args, null, null, null, "1");
        if(cursor == null)
        {
            return null;
        }
        Event pushEvent = null;
        if(cursor.moveToFirst())
        {
            pushEvent = cursorToEvent(cursor);
        }
        cursor.close();

        ArrayList<String> packages = getEventPackages(args[0]);
        pushEvent.packages.addAll(packages);

        Log.d("getEventJson," + eid + "," + pushEvent);
        return pushEvent;
    }

    static Event cursorToEvent(Cursor cursor)
    {
        int type = cursor.getInt(1);
        String json = cursor.getString(2);
        return new Event(type, json);
    }


    static Event getEventJson(Context context, String eid)
    {
        synchronized (LOCK)
        {
            try
            {
                EventDbAdapter db = new EventDbAdapter(context);
                db.open();
                Event ret = db.getEventJson(eid);
                db.close();
                return ret;
            }
            catch (Exception e)
            {
                clearDB(context);
            }
        }
        return null;
    }

    public static void clearDB(Context context)
    {
        synchronized (LOCK)
        {
            context.getDatabasePath(DB_NAME).delete();
            ServiceUtil.clearTriggeredEvent(context);
        }
    }

    /**
     *
     * @param context
     * @param city
     * @return 回傳檢查到的eid
     */
    public static ArrayList<String> checkCity(Context context, String city)
    {
        ArrayList<String> eids = new ArrayList<>();
        synchronized (LOCK)
        {
            EventDbAdapter db = new EventDbAdapter(context);
            db.open();
            String queryString = String.format("%s=? AND %s LIKE '%s'", COLUMN_TYPE, COLUMN_JSON, "%" + city + "%");
            Cursor cursor = db.mSQLiteDatabase.query(TABLE_EVENT, null, queryString, new String[]{String.valueOf(Event.TYPE_CITY)}, null, null, null);
            while (cursor.moveToNext())
            {
                Event event = cursorToEvent(cursor);
                eids.add(event.getEventId());
            }
            cursor.close();
            db.close();
        }
        return eids;
    }

    public static ArrayList<String> getBroadcastEvent(Context context)
    {
    	ArrayList<String> eids = new ArrayList<>();
        synchronized (LOCK)
        {
            EventDbAdapter db = new EventDbAdapter(context);
            db.open();
            String queryString = String.format("%s=?", COLUMN_TYPE);
            Cursor cursor = db.mSQLiteDatabase.query(TABLE_EVENT, null, queryString, new String[]{String.valueOf(Event.TYPE_BROADCAST)}, null, null, null);
            while (cursor.moveToNext())
            {
                Event event = cursorToEvent(cursor);
                eids.add(event.getEventId());
            }
            cursor.close();
            db.close();
        }
        return eids;
    }

    public static void removeEid(Context context, String eid)
    {
        synchronized (LOCK)
        {
            EventDbAdapter db = new EventDbAdapter(context);
            db.open();
            db.mSQLiteDatabase.delete(TABLE_EVENT, COLUMN_EVENT_ID + " = ?", new String[]{String.valueOf(eid)});
            db.mSQLiteDatabase.delete(TABLE_PACKAGES, COLUMN_EVENT_ID + " = ?", new String[]{String.valueOf(eid)});
            db.mSQLiteDatabase.execSQL("VACUUM");
            db.close();
        }
    }

	public static void removePackage(Context context, String triggerPackage)
	{
        synchronized (LOCK)
        {
            EventDbAdapter db = new EventDbAdapter(context);
            db.open();
            db.mSQLiteDatabase.delete(TABLE_PACKAGES, COLUMN_PACKAGES + " = ?", new String[]{String.valueOf(triggerPackage)});
            db.mSQLiteDatabase.execSQL("VACUUM");
            db.close();
        }
	}
}
