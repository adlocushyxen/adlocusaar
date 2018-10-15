package com.hyxen.adlocusaar.push;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.format.DateUtils;
import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
//import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPOutputStream;

class CellPushDbAdapter
{

    public static byte[] compress(byte[] dataToCompress)
    {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(dataToCompress.length);
            try {
                GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);
                try {
                    zipStream.write(dataToCompress);
                } finally {
                    zipStream.close();
                }
            } finally {
                byteStream.close();
            }

            return byteStream.toByteArray();
        }
        catch(Exception ignored) { }
        return dataToCompress;
    }


    public static final Object LOCK = new Object();
	public static final String DB_NAME = AdLocusUtil.PREFIX + "cell_push_adlocus_";
	private SQLiteDatabase mSQLiteDatabase = null;

	private static final String TABLE_FP = "fp";
    private static final String COLUMN_LAC = "lac";
	private static final String COLUMN_CELL_ID = "ci";

	private final String mDatabasePath;

    private final String mEid;

	private final Context mContext;


    private static final String CREATE_TABLE_EVENT = "CREATE TABLE " + TABLE_FP
            + " (" + COLUMN_LAC + " INTEGER PRIMARY KEY,"
            + COLUMN_CELL_ID	+ " BLOB)";

    static class DatabaseHelper extends SQLiteOpenHelper
    {

        private static final int DB_VERSION = 1;
        public DatabaseHelper(Context context, String dbName)
        {
            super(context, dbName, null, DB_VERSION);
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
    void compress() {

        CellPushDbAdapter db = new CellPushDbAdapter(mContext, mEid);
        db.mSQLiteDatabase = new DatabaseHelper(mContext, "dsajioj" + mEid).getWritableDatabase();

        Cursor cursor = mSQLiteDatabase.query(TABLE_FP, null, null, null, null, null, null, null);
        final int C_LAC = cursor.getColumnIndex(COLUMN_LAC);
        final int C_CID = cursor.getColumnIndex(COLUMN_CELL_ID);
        while (cursor.moveToNext()) {
            int lac = cursor.getInt(C_LAC);
            byte[] blob = cursor.getBlob(C_CID);
            blob = compress(blob);
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_LAC, lac);
            cv.put(COLUMN_CELL_ID, blob);
            db.mSQLiteDatabase.insert(TABLE_FP, null, cv);
        }
        cursor.close();
        db.close();
    }


	private CellPushDbAdapter(Context context, String eventId)
	{
        mContext = context;
        mEid = eventId;
        String dbNeme = DB_NAME + eventId;
        Log.d("open db:" + dbNeme);
		mDatabasePath = context.getDatabasePath(dbNeme).getAbsolutePath();
	}

	private void close()
	{
		mSQLiteDatabase.close();
	}

	private CellPushDbAdapter open() throws SQLException
	{
		mSQLiteDatabase = SQLiteDatabase.openDatabase(mDatabasePath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		return this;
	}
	
	private long getCount() throws Exception
	{
		SQLiteStatement s = mSQLiteDatabase.compileStatement("select count(*) from " + TABLE_FP);
		long ret = s.simpleQueryForLong();
		s.close();
		return ret;
	}


    /**
     *
     *
     * @param lac
     * @return eids
     */
    private ArrayList<Integer> getCis(int lac)
    {
        Log.d("getCis:" + lac);
        Cursor cursor = mSQLiteDatabase.query(TABLE_FP, null, COLUMN_LAC + "=?", new String[]{String.valueOf(lac)}, null, null, null, "1");
        final int C_CID = cursor.getColumnIndex(COLUMN_CELL_ID);
        ArrayList<Integer> cids = new ArrayList<>();
        while (cursor.moveToNext())
        {
            byte[] blob = cursor.getBlob(C_CID);
            int length = blob.length - 1;
            byte b;
            for (int i = 0; i < length; i++)
            {
                if ((b = blob[i]) == 0)
                {
                    continue;
                }
                boolean b1;
                for (int j = 0; j < Byte.SIZE; j++)
                {
                    b1 = (b << j & 0x80) != 0;
                    if (b1)
                    {
                        int cid = (i) * Byte.SIZE + ( j);
                        cids.add(cid);
                    }
                }
            }
            blob = null;
        }
        cursor.close();
        mSQLiteDatabase.close();
        System.gc();
        return cids;
    }


    private static final FileFilter FF = new FileFilter()
    {
        @Override
        public boolean accept(File file)
        {
            if (file.getName().startsWith(DB_NAME)) {
                if (DateUtils.isToday(file.lastModified())) {
                    return true;
                } else {
                    file.delete();
                }
            }
            return false;
        }
    };

    static HashMap<Integer, HashSet<String>> getCis(Context context, int lac)
    {
        File[] listFiles = context.getDatabasePath("d").getParentFile().listFiles(FF);
        CellPushDbAdapter db;
        HashMap<Integer, HashSet<String>> all = new HashMap<>();
        for (File file: listFiles)
        {
            String eid = file.getName().split(DB_NAME)[1];
            db = new CellPushDbAdapter(context, eid);
            db.open();
//            db.compress();
            ArrayList<Integer> cis = db.getCis(lac);
            db.close();
            Log.d("getCis:lac" + lac);
            Log.d("getCis:ci:" + cis);
            for (int ci : cis)
            {
                addEvent(all, ci, eid);
            }
        }
        return all;
    }


    private static void addEvent(HashMap<Integer, HashSet<String>> all, int cid, String eid)
    {
    	HashSet<String> eids = all.get(cid);
        if(eids == null)
        {
            eids = new HashSet<>();
        }
        eids.add(eid);
        all.put(cid, eids);
    }

    public static void removeEid(Context context, String eid)
    {
        context.getDatabasePath(DB_NAME + eid).delete();
    }

    public static boolean exists(Context context, String eid)
    {
        return context.getDatabasePath(DB_NAME + eid).exists();
    }
}
