package com.rutgers.kashyap.rutgersbusservice.data;

import android.provider.BaseColumns;

/**
 * Created by khpatel4991 on 3/20/2015.
 */
public class DBContract
{
	//Helper Strings
	public static final String CREATE_TABLE = "CREATE TABLE ";
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
	public static final String SELECT_ALL = "SELECT * FROM ";
	public static final String COMMA_SEP = ",";
	public static final String SORT_BY_TITLE = " ORDER BY title ASC";


	public DBContract() {}

	//Inner Class for Routes Table
	public static abstract class RouteEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "route";
		//public static final String COLUMN_NAME_ROUTE_ID = "id";
		public static final String COLUMN_NAME_ROUTE_TAG = "tag";
		public static final String COLUMN_NAME_ROUTE_TITLE = "title";

		public static final String CREATE_ROUTE_TABLE_QUERY = CREATE_TABLE + TABLE_NAME + " (" +
				COLUMN_NAME_ROUTE_TAG + " TEXT PRIMARY KEY" + COMMA_SEP +
				COLUMN_NAME_ROUTE_TITLE + " TEXT NOT NULL);";

		public static final String DELETE_ROUTE_TABLE_QUERY = DROP_TABLE + TABLE_NAME;
		public static final String ALL_SORTED_ROUTE_QUERY = SELECT_ALL + TABLE_NAME + SORT_BY_TITLE + ";";
	}

	public static abstract class StopEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "stop";
		//public static final String COLUMN_NAME_STOP_ID = "id";
		public static final String COLUMN_NAME_STOP_TAG = "tag";
		public static final String COLUMN_NAME_STOP_TITLE = "title";
		public static final String COLUMN_NAME_STOP_LAT = "lat";
		public static final String COLUMN_NAME_STOP_LON = "lon";

		public static final String CREATE_STOP_TABLE_QUERY = CREATE_TABLE + TABLE_NAME + " (" +
				COLUMN_NAME_STOP_TAG + " TEXT PRIMARY KEY" + COMMA_SEP +
				COLUMN_NAME_STOP_TITLE + " TEXT NOT NULL" + COMMA_SEP +
				COLUMN_NAME_STOP_LAT + " REAL NOT NULL" + COMMA_SEP +
				COLUMN_NAME_STOP_LON + " REAL NOT NULL);";

		public static final String DELETE_ROUTE_TABLE_QUERY = DROP_TABLE + TABLE_NAME;

		public static final String ALL_SORTED_STOP_QUERY = SELECT_ALL + TABLE_NAME + SORT_BY_TITLE + ";";
	}
}
