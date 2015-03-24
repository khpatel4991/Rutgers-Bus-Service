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
	public static final String DOT_SEP = ".";
	public static final String SORT_BY_TITLE = " ORDER BY title ASC";

	public DBContract() {}

	//Inner Class for Routes Table
	public static abstract class RouteEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "route";
		//public static final String COLUMN_NAME_ROUTE_ID = "id";
		public static final String COLUMN_NAME_ROUTE_TAG = "tag";
		public static final String COLUMN_NAME_ROUTE_TITLE = "title";

		public static final String CREATE_TABLE_QUERY = CREATE_TABLE + TABLE_NAME + " (" +
				COLUMN_NAME_ROUTE_TAG + " TEXT PRIMARY KEY" + COMMA_SEP +
				COLUMN_NAME_ROUTE_TITLE + " TEXT NOT NULL);";

		public static final String DELETE_TABLE_QUERY = DROP_TABLE + TABLE_NAME;
		public static final String SELECT_ALL_SORTED_QUERY = SELECT_ALL + TABLE_NAME + SORT_BY_TITLE + ";";

        public static final String GET_ROUTE_TAG_FROM_TITLE_QUERY =
                "SELECT " + COLUMN_NAME_ROUTE_TAG +
                " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_NAME_ROUTE_TITLE + "=";

	}

	public static abstract class StopEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "stop";
		//public static final String COLUMN_NAME_STOP_ID = "id";
		public static final String COLUMN_NAME_STOP_TAG = "tag";
		public static final String COLUMN_NAME_STOP_TITLE = "title";
		public static final String COLUMN_NAME_STOP_LAT = "lat";
		public static final String COLUMN_NAME_STOP_LON = "lon";

		public static final String CREATE_TABLE_QUERY = CREATE_TABLE + TABLE_NAME + " (" +
				COLUMN_NAME_STOP_TAG + " TEXT PRIMARY KEY" + COMMA_SEP +
				COLUMN_NAME_STOP_TITLE + " TEXT NOT NULL" + COMMA_SEP +
				COLUMN_NAME_STOP_LAT + " REAL NOT NULL" + COMMA_SEP +
				COLUMN_NAME_STOP_LON + " REAL NOT NULL);";

		public static final String DELETE_TABLE_QUERY = DROP_TABLE + TABLE_NAME;

		public static final String SELECT_ALL_SORTED_QUERY = SELECT_ALL + TABLE_NAME + SORT_BY_TITLE + ";";

	}

	public static abstract class RouteStopEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "route_stop";
		//public static final String COLUMN_NAME_ID = "id";
		public static final String COLUMN_NAME_ROUTE = "route";
		public static final String COLUMN_NAME_STOP = "stop";

		public static final String CREATE_TABLE_QUERY = CREATE_TABLE + TABLE_NAME + " (" +
				COLUMN_NAME_ROUTE + " TEXT NOT NULL" + COMMA_SEP +
				COLUMN_NAME_STOP + " TEXT NOT NULL" + COMMA_SEP +
				"PRIMARY KEY (" + COLUMN_NAME_ROUTE + COMMA_SEP + COLUMN_NAME_STOP + ")" + COMMA_SEP +
				"FOREIGN KEY (" + COLUMN_NAME_ROUTE + ") REFERENCES " + RouteEntry.TABLE_NAME + "(" + RouteEntry.COLUMN_NAME_ROUTE_TAG + ")" + COMMA_SEP +
				"FOREIGN KEY (" + COLUMN_NAME_STOP + ") REFERENCES " + StopEntry.TABLE_NAME + "(" + StopEntry.COLUMN_NAME_STOP_TAG + "));";

		public static final String DELETE_TABLE_QUERY = DROP_TABLE + TABLE_NAME;

		public static final String GET_STOPS_FROM_ROUTE_TITLE_QUERY =
				"SELECT " + StopEntry.TABLE_NAME + DOT_SEP + StopEntry.COLUMN_NAME_STOP_TAG + COMMA_SEP + StopEntry.TABLE_NAME + DOT_SEP + StopEntry.COLUMN_NAME_STOP_TITLE +
				" FROM " + TABLE_NAME + COMMA_SEP + StopEntry.TABLE_NAME + COMMA_SEP + RouteEntry.TABLE_NAME +
				" WHERE " + TABLE_NAME + DOT_SEP + COLUMN_NAME_ROUTE + "=" + RouteEntry.TABLE_NAME + DOT_SEP + RouteEntry.COLUMN_NAME_ROUTE_TAG +
				" AND " + TABLE_NAME + DOT_SEP + COLUMN_NAME_STOP + "=" + StopEntry.TABLE_NAME + DOT_SEP + StopEntry.COLUMN_NAME_STOP_TAG +
				" AND " + RouteEntry.TABLE_NAME + DOT_SEP + RouteEntry.COLUMN_NAME_ROUTE_TITLE + "=";
	}

    public static abstract class RouteStopTimeEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "route_stop_time";
        //public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_ROUTE = "route";
        public static final String COLUMN_NAME_STOP = "stop";
        public static final String COLUMN_NAME_TIME = "time";

        public static final String CREATE_TABLE_QUERY = CREATE_TABLE + TABLE_NAME + " (" +
                COLUMN_NAME_ROUTE + " TEXT NOT NULL" + COMMA_SEP +
                COLUMN_NAME_STOP + " TEXT NOT NULL" + COMMA_SEP +
                COLUMN_NAME_TIME + " REAL NOT NULL" + COMMA_SEP +
                "FOREIGN KEY (" + COLUMN_NAME_ROUTE + ") REFERENCES " + RouteEntry.TABLE_NAME + "(" + RouteEntry.COLUMN_NAME_ROUTE_TITLE + ")" + COMMA_SEP +
                "FOREIGN KEY (" + COLUMN_NAME_STOP + ") REFERENCES " + StopEntry.TABLE_NAME + "(" + StopEntry.COLUMN_NAME_STOP_TITLE + "));";

        public static final String DELETE_TABLE_QUERY = DROP_TABLE + TABLE_NAME;

        public static final String GET_TIMES_FROM_ROUTE_STOP_QUERY1 =
                "SELECT " + COLUMN_NAME_TIME +
                " FROM " + TABLE_NAME + COMMA_SEP + RouteEntry.TABLE_NAME + COMMA_SEP + StopEntry.TABLE_NAME +
                " WHERE " + TABLE_NAME + DOT_SEP + COLUMN_NAME_ROUTE + "=" + RouteEntry.TABLE_NAME + DOT_SEP + RouteEntry.COLUMN_NAME_ROUTE_TITLE +
                " AND " + TABLE_NAME + DOT_SEP + COLUMN_NAME_STOP + "=" + StopEntry.TABLE_NAME + DOT_SEP + StopEntry.COLUMN_NAME_STOP_TITLE +
                " AND " + RouteEntry.TABLE_NAME + DOT_SEP + RouteEntry.COLUMN_NAME_ROUTE_TITLE + "=";

        public static final String GET_TIMES_FROM_ROUTE_STOP_QUERY2 =
                " AND " + StopEntry.TABLE_NAME + DOT_SEP + StopEntry.COLUMN_NAME_STOP_TITLE + "=";

    }

}
