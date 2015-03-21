package com.rutgers.kashyap.rutgersbusservice.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by khpatel4991 on 3/20/2015.
 */
public class DBHelper extends SQLiteOpenHelper
{
	public static final String LOG_TAG = DBHelper.class.getSimpleName();

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "BusService.db";

	public DBHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		Log.d(LOG_TAG, DBContract.RouteStopEntry.CREATE_TABLE_QUERY);
		db.execSQL(DBContract.RouteEntry.CREATE_TABLE_QUERY);
		db.execSQL(DBContract.StopEntry.CREATE_TABLE_QUERY);
		db.execSQL(DBContract.RouteStopEntry.CREATE_TABLE_QUERY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL(DBContract.RouteEntry.DELETE_TABLE_QUERY);
		db.execSQL(DBContract.StopEntry.DELETE_TABLE_QUERY);
		db.execSQL(DBContract.RouteStopEntry.DELETE_TABLE_QUERY);
		onCreate(db);
	}
}
