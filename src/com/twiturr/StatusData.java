package com.twiturr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

public class StatusData {
    private static final String TAG = StatusData.class.getSimpleName();

    public static final int DB_VERSION = 1;
    
    public static final String DB_NAME = "timeline.db";
    public static final String TABLE = "timeline";
    public static final String C_ID = "_id";
    public static final String C_CREATED_AT = "created_at";
    public static final String C_SOURCE = "source";
    public static final String C_TEXT = "txt";
    public static final String C_USER = "user";

    private static final String GET_ALL_BY_ORDER = C_CREATED_AT + " DESC";
    private static final String[] MAX_CREATED_AT_COLUMNS = { "max(" + C_CREATED_AT + ")" };
    private static final String[] DB_TEXT_COLUMNS = { C_TEXT };

    public class DbHelper extends SQLiteOpenHelper {
	public DbHelper(Context context) {
	    super(context, DB_NAME, null, DB_VERSION);
	}

	@Override public void onCreate(SQLiteDatabase db) {
	    String q = "";
	    q += "CREATE TABLE " + TABLE + " (";
	    q +=         C_ID + " INT PRIMARY KEY, ";
	    q += C_CREATED_AT + " INT, ";
	    q +=       C_USER + " TEXT, ";
	    q +=       C_TEXT + " TEXT, ";
	    q +=     C_SOURCE + " TEXT";
	    q += ")";

	    db.execSQL(q);
	    Log.d(TAG, "onCreated sql: "+q);
	}

	@Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
	    db.execSQL("DROP TABLE IF EXISTS "+TABLE);
	    Log.d(TAG, "onUpdated");
	    onCreate(db);
	}
    }

    private final DbHelper dbHelper;

    public StatusData(Context context) {
	this.dbHelper = new DbHelper(context);
	Log.i(TAG, "Initialize data");
    }

    public void close() {
	this.dbHelper.close();
    }

    public void delete() {
	SQLiteDatabase db = this.dbHelper.getWritableDatabase();
	this.dbHelper.onUpgrade(db, 1, 1);
	db.close();
    }

    public void insertOrIgnore(ContentValues values) {
	//Log.d(TAG, "insertOrIgnore on " + values);
	SQLiteDatabase db = this.dbHelper.getWritableDatabase();
	try {
	    try {
		db.insertOrThrow(TABLE, null, values);
	    } catch (SQLiteConstraintException e) {
		; // ignoring duplicate ids
	    }
	} finally {
	    db.close();
	}
    }

    public Cursor getStatusUpdates() {
	SQLiteDatabase db = this.dbHelper.getReadableDatabase();
	return db.query(TABLE, null, null, null, null, null, GET_ALL_BY_ORDER);
    }

    public long getLatestStatusCreatedAtTime() {
	SQLiteDatabase db = this.dbHelper.getReadableDatabase();
	try {
	    Cursor cursor = db.query(TABLE, MAX_CREATED_AT_COLUMNS, null, null, null, null, null);
	    try {
		return cursor.moveToNext() ? cursor.getLong(0) : Long.MIN_VALUE;
	    } finally {
		cursor.close();
	    }
	} finally {
	    db.close();
	}
    }

    public String getStatusTextById(long id) {
	SQLiteDatabase db = this.dbHelper.getReadableDatabase();
	try {
	    Cursor cursor = db.query(TABLE, DB_TEXT_COLUMNS, C_ID + "=" + id, null, null, null, null);
	    try {
		return cursor.moveToNext() ? cursor.getString(0) : null;
	    } finally {
		cursor.close();
	    }
	} finally {
	    db.close();
	}
    }
}

