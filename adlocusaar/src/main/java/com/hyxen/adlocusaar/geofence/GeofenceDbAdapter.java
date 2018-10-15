package com.hyxen.adlocusaar.geofence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

class GeofenceDbAdapter
{
	static final String DB_NAME = "hx_geofence.db";

	private static final Object LOCK = new Object();
	public class DatabaseHelper extends SQLiteOpenHelper
	{

		private static final int DB_VERSION = 3;
		public DatabaseHelper(Context context)
		{
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(CREATE_TABLE_EVENT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
		}
	}
	private SQLiteDatabase mSQLiteDatabase = null;
	private DatabaseHelper mDBHelper = null;
	
	/*
	 * TABLE_EVENT
	 */
	private static final String TABLE_EVENT 		= "events";
	private static final String COLUMN_EVENT_ID 	= "event_id";
	private static final String COLUMN_IDENTIFY 	= "identify";
	private static final String COLUMN_LAT 			= "lat";
	private static final String COLUMN_LON 			= "lon";
	private static final String COLUMN_RADIUS 		= "radius";
	private static final String COLUMN_FENCE 		= "fence";
	private static final String COLUMN_STATUS 		= "status";
	private static final String COLUMN_STARTING_TS	= "starting_ts";
	private static final String COLUMN_EXPIRING_TS	= "expiring_ts";
	private static final String COLUMN_REF_EID		= "ref_eid";
	private static final String COLUMN_USER_FENCE	= "user_fence";

	/*
	 * FENCE
	 */
	static final int FENCE_NONE 		= 0;
	static final int FENCE_IN 		= 1;
	static final int FENCE_OUT 		= 2;
	
	static final int STATUS_NON				= 0;
	
	static final int STATUS_TRIGGER_IN		= 2;
//	static final int STATUS_TRIGGER_OUT		= 10;


	private static final String CREATE_TABLE_EVENT = "CREATE TABLE " + TABLE_EVENT
	+ " (" + COLUMN_EVENT_ID + " INTEGER PRIMARY KEY,"
	+ COLUMN_IDENTIFY		+ " TEXT,"
	+ COLUMN_LAT			+ " REAL,"
	+ COLUMN_LON			+ " REAL,"
	+ COLUMN_RADIUS			+ " INTEGER,"
	+ COLUMN_FENCE			+ " INTEGER DEFAULT 0,"
	+ COLUMN_STATUS			+ " INTEGER DEFAULT 0,"
	+ COLUMN_STARTING_TS	+ " INTEGER DEFAULT -1,"
	+ COLUMN_EXPIRING_TS	+ " INTEGER DEFAULT -1,"
	+ COLUMN_REF_EID		+ " INTEGER DEFAULT -1,"
	+ COLUMN_USER_FENCE		+ " INTEGER DEFAULT -1)";

	private Context mContext;

	private GeofenceDbAdapter(Context context)
	{
		mDBHelper = new DatabaseHelper(context);
		mContext = context;
	}

	private void close()
	{
		mDBHelper.close();
	}

	private SQLiteDatabase open() throws SQLException
	{
		mSQLiteDatabase = mDBHelper.getWritableDatabase();
		return mSQLiteDatabase;
	}
	
	public static ArrayList<Region> getAllRegion(Context context)
	{
		synchronized (LOCK) {
			GeofenceDbAdapter db = new GeofenceDbAdapter(context);
			db.open();
			ArrayList<Region> ret = new ArrayList<>();
			Cursor cursor = db.mSQLiteDatabase.query(TABLE_EVENT, null, null, null, null, null, null);
			Region r;
			while(cursor.moveToNext())
			{
				double lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT));
				double lon = cursor.getDouble(cursor.getColumnIndex(COLUMN_LON));
				int radius = cursor.getInt(cursor.getColumnIndex(COLUMN_RADIUS));
				int fance = cursor.getInt(cursor.getColumnIndex(COLUMN_FENCE));
				String identify = cursor.getString(cursor.getColumnIndex(COLUMN_IDENTIFY));
				long tsStarting = cursor.getLong(cursor.getColumnIndex(COLUMN_STARTING_TS));
				long tsExpiring = cursor.getLong(cursor.getColumnIndex(COLUMN_EXPIRING_TS));
				int status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
				int userFence = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_FENCE));

				r = new Fence(lat, lon, radius, fance, identify, tsStarting, tsExpiring, status, userFence);
				ret.add(r);
			}
			cursor.close();
			db.close();
			return ret;
		}

	}
	
	public static Fence getRegion(Context context, long eid)
	{
		synchronized (LOCK) {
			GeofenceDbAdapter db = new GeofenceDbAdapter(context);
			db.open();
			Cursor cursor = db.mSQLiteDatabase.query(TABLE_EVENT, null, String.format("%s=?", COLUMN_EVENT_ID), new String[]{String.valueOf(eid)}, null, null, null);
			Fence ret = null;
			while(cursor.moveToNext())
			{
				double lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT));
				double lon = cursor.getDouble(cursor.getColumnIndex(COLUMN_LON));
				int radius = cursor.getInt(cursor.getColumnIndex(COLUMN_RADIUS));
				int fance = cursor.getInt(cursor.getColumnIndex(COLUMN_FENCE));
				String identify = cursor.getString(cursor.getColumnIndex(COLUMN_IDENTIFY));
				long tsStarting = cursor.getLong(cursor.getColumnIndex(COLUMN_STARTING_TS));
				long tsExpiring = cursor.getLong(cursor.getColumnIndex(COLUMN_EXPIRING_TS));
				int status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
				int userFence = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_FENCE));
				ret= new Fence(lat, lon, radius, fance, identify, tsStarting, tsExpiring, status, userFence);
			}
			cursor.close();
			db.close();
			return ret;
		}
	}
	
	/**
	 * 
	 * @param eid event id
	 * @return now fance
	 */
	public static int setMatchEid(Context context, int eid)
	{
		synchronized (LOCK) {
			GeofenceDbAdapter db = new GeofenceDbAdapter(context);
			db.open();
			final String where = String.format("%s=?", COLUMN_EVENT_ID);
			final String[] selectionArgs = new String[]{String.valueOf(eid)};
			Cursor cursor = db.mSQLiteDatabase.query(TABLE_EVENT, null, where, selectionArgs, null, null, null);
			int fence = -1;
			int userFence = -1;
			while(cursor.moveToNext())
			{
				fence = cursor.getInt(cursor.getColumnIndex(COLUMN_FENCE));
				userFence = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_FENCE));
			}
			cursor.close();
			switch (fence)
			{
				case FENCE_NONE:
					ContentValues cv = new ContentValues();
					cv.put(COLUMN_FENCE, FENCE_IN);
					fence = FENCE_IN;
					if(userFence == FENCE_IN)
					{
						db.mSQLiteDatabase.delete(TABLE_EVENT, where, selectionArgs);
					}
					else
					{
						db.mSQLiteDatabase.update(TABLE_EVENT, cv, where, selectionArgs);
					}
					break;
				case FENCE_IN:
					db.mSQLiteDatabase.delete(TABLE_EVENT, where, selectionArgs);
					fence = FENCE_OUT;
					break;
				default:
					break;
			}
			db.close();
			return fence;
		}
	}
	
	public static void deleteEvent(Context context, int eid)
	{
		synchronized (LOCK) {
			GeofenceDbAdapter db = new GeofenceDbAdapter(context);
			db.open();
			final String where = String.format("%s=?", COLUMN_EVENT_ID);
			final String[] selectionArgs = new String[]{String.valueOf(eid)};
			db.mSQLiteDatabase.delete(TABLE_EVENT, where, selectionArgs);
			db.close();
		}
	}

	private static final String SQL_EXPITING_EVENT = String.format("%s>?", COLUMN_EXPIRING_TS);
	public static void removeExpiringEvent(Context context)
	{
		synchronized (LOCK) {
			GeofenceDbAdapter db = new GeofenceDbAdapter(context);
			db.open();
			Cursor cursor = db.mSQLiteDatabase.query(TABLE_EVENT, null, SQL_EXPITING_EVENT, new String[]{String.valueOf(System.currentTimeMillis())}, null, null, null);
			HashSet<String> list = new HashSet<>();
			while(cursor.moveToNext())
			{
				list.add(cursor.getString(cursor.getColumnIndex(COLUMN_IDENTIFY)));
			}
			cursor.close();

			db.mSQLiteDatabase.beginTransaction();
			try
			{
				for (String id : list)
				{
					deleteEvent(context, id.hashCode());
				}
				db.mSQLiteDatabase.setTransactionSuccessful();
			}
			catch (Exception ignored) { }
			finally
			{
				db.mSQLiteDatabase.endTransaction();
			}
			db.close();
		}
	}

	public static void addRegion(Context context, Region region)
	{
		synchronized (LOCK) {
			GeofenceDbAdapter db = new GeofenceDbAdapter(context);
			db.open();
			setRegion(context, region.getLat(), region.getLon(), region.getRadius(), region.getIdentify(), region.getStartingTs(), region.getExpiringTs(), region.getUserFence());
			db.close();
		}
	}

	public static void setRegion(Context context, double lat, double lon, int radius, String identify, long tsStarting, long tsExpiring, int userFence)
	{
		synchronized (LOCK) {
			GeofenceDbAdapter db = new GeofenceDbAdapter(context);
			db.open();
			int eventID = identify.hashCode();
			deleteEvent(context, eventID);
			ContentValues initialValues = new ContentValues();
			initialValues.put(COLUMN_EVENT_ID, eventID);
			initialValues.put(COLUMN_IDENTIFY, identify);
			initialValues.put(COLUMN_LAT, lat);
			initialValues.put(COLUMN_LON, lon);
			initialValues.put(COLUMN_RADIUS, radius);
			initialValues.put(COLUMN_STARTING_TS, tsStarting);
			initialValues.put(COLUMN_EXPIRING_TS, tsExpiring);
			initialValues.put(COLUMN_USER_FENCE, userFence);
			db.mSQLiteDatabase.insert(TABLE_EVENT, null, initialValues);
			db.close();
		}
	}


	public static void updateStatusSuccess(Context context, int eid, int status)
	{
		synchronized (LOCK) {
			GeofenceDbAdapter db = new GeofenceDbAdapter(context);
			db.open();
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_STATUS, status);
			db.mSQLiteDatabase.update(TABLE_EVENT, cv, String.format("%s=?", COLUMN_EVENT_ID), new String[]{String.valueOf(eid)});
			db.close();
		}
	}

	public static ArrayList<Region> checkLatLon(Context context, double lat1, double lon1) {

		synchronized (LOCK) {
			GeofenceDbAdapter db = new GeofenceDbAdapter(context);
			db.open();

			final double MAX_RADIUS = 1.5;
			final double lonBound = (MAX_RADIUS / Math.abs(Math.cos((lat1 / 180) * Math.PI) * 69));
			final double minLon = lon1 - lonBound;
			final double maxLon = lon1 + lonBound;
			final double latBound = (MAX_RADIUS / 69);
			final double minLat = lat1 - latBound;
			final double maxLat = lat1 + latBound;

			final String where = String.format(Locale.US, "(%s BETWEEN %f AND %f AND %s BETWEEN %f AND %f) AND %s=%d", COLUMN_LAT, minLat, maxLat, COLUMN_LON, minLon, maxLon, COLUMN_FENCE, FENCE_NONE);
			Cursor cursor = db.mSQLiteDatabase.query(TABLE_EVENT, null, where, null, null, null, null);

			Location loc = new Location("1");
			loc.setLatitude(lat1);
			loc.setLongitude(lon1);
			ArrayList<Region> ret = new ArrayList<>();
			while(cursor.moveToNext()) {
				double lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT));
				double lon = cursor.getDouble(cursor.getColumnIndex(COLUMN_LON));

				Location l = new Location("1");
				l.setLatitude(lat);
				l.setLongitude(lon);
				int radius = cursor.getInt(cursor.getColumnIndex(COLUMN_RADIUS));
				double dis = l.distanceTo(loc);

				if(Geofence.IS_SHOW_LOG)
				{
					Log.d("checkLatLon", "dis:" + dis);
				}
				if(dis <= radius) {
					int fence = cursor.getInt(cursor.getColumnIndex(COLUMN_FENCE));
					String identify = cursor.getString(cursor.getColumnIndex(COLUMN_IDENTIFY));
					long tsStarting = cursor.getLong(cursor.getColumnIndex(COLUMN_STARTING_TS));
					long tsExpiring = cursor.getLong(cursor.getColumnIndex(COLUMN_EXPIRING_TS));
					int status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
					int userFence = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_FENCE));
					ret.add(new Fence(lat, lon, radius, fence, identify, tsStarting, tsExpiring, status, userFence));
				}
			}
			cursor.close();
			db.close();
			return ret;
		}
	}

	public static void delete(Context context) {
		synchronized (LOCK) {
			context.getDatabasePath(GeofenceDbAdapter.DB_NAME).delete();
		}
	}
}
