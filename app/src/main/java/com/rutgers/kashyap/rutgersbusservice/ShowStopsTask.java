package com.rutgers.kashyap.rutgersbusservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.rutgers.kashyap.rutgersbusservice.Graph.Edge;
import com.rutgers.kashyap.rutgersbusservice.Graph.Stop;
import com.rutgers.kashyap.rutgersbusservice.data.DBContract;
import com.rutgers.kashyap.rutgersbusservice.data.DBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by khpatel4991 on 3/20/2015.
 */
public class ShowStopsTask extends AsyncTask<Void, Void, String[]>
{
	private static final String LOG_TAG = ShowStopsTask.class.getSimpleName();

	private Activity _activity;

	private HashMap<String, Stop> stopsMap = new HashMap<String, Stop>();

	private ProgressDialog _progressDialog;

	private DBHelper _DBHelper;


	public ShowStopsTask(Activity activity)
	{
		_activity = activity;
		_progressDialog = new ProgressDialog(_activity);
		_DBHelper = new DBHelper(_activity);
	}

	private final static String URL_CONFIG = "http://runextbus.herokuapp.com/config";


	private final static String STOPS = "stops";
	private final static String TITLE = "title";
	private final static String LATITUDE = "lat";
	private final static String LONGITUDE = "lon";
	private final static String STOPID = "stopId";

	private final static String SCOTT = "scott";
	private final static String NURSING = "nursscho";

	private final static String ROUTES = "routes";

	InputStream is = null;
	String result = "";

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		_progressDialog.setMessage("Downloading your data...");
		_progressDialog.show();
	}

	@Override
	protected String[] doInBackground(Void... voids)
	{
		String[] stops = getStopsFromDB();
		if(stops.length == 0)
		{
			Log.i(LOG_TAG, "Opening app for first time, Internet Connection needed");
			try
			{
				URL url = new URL(URL_CONFIG);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);

				// Starts the query
				conn.connect();
				is = conn.getInputStream();

				//Convert the InputStream into a string
				//String contentAsString = readIt(is, len);

				// Read response to string
				try
				{
					BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null)
					{
						sb.append(line + "\n");
					}
					is.close();
					result = sb.toString();
				}
				catch (Exception e)
				{
					return null;
				}

				JSONObject jsonObject;
				// Convert string to object
				try
				{
					jsonObject = new JSONObject(result);
				}
				catch (JSONException e)
				{
					return null;
				}
				insertDataIntoDB(jsonObject);
				return getStopsFromDB();
			}
			catch (Exception e)
			{
				Log.e(LOG_TAG, e.getMessage());
				return null;
			}
		}
		else
			return stops;
	}

	@Override
	protected void onPostExecute(String[] stops)
	{
		super.onPostExecute(stops);
		final Spinner spinnerSource = (Spinner) _activity.findViewById(R.id.spinner_destination_stop);
		final Spinner spinnerDestination = (Spinner) _activity.findViewById(R.id.spinner_source_stop);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(_activity, android.R.layout.simple_spinner_item, stops);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSource.setAdapter(adapter);
		spinnerDestination.setAdapter(adapter);
		_progressDialog.dismiss();
	}

	private String[] getStopsFromDB()
	{
		SQLiteDatabase db = _DBHelper.getReadableDatabase();
		List<String> stops = new ArrayList<>();
		Cursor c = db.rawQuery(DBContract.StopEntry.SELECT_ALL_SORTED_QUERY, null);
		if(c.moveToFirst())
		{
			do
			{
				stops.add(c.getString(1) + " (" + c.getString(0) + ")");
			} while(c.moveToNext());
		}
		c.close();
		db.close();
		return stops.toArray(new String[stops.size()]);
	}

	private void insertDataIntoDB(JSONObject jsonObject) throws JSONException
	{
		SQLiteDatabase db = _DBHelper.getWritableDatabase();

		//Routes Table
		JSONObject routesJSON = jsonObject.getJSONObject(ROUTES);
		JSONArray routesJSONArray = routesJSON.names();
		for (int i = 0; i < routesJSONArray.length(); i ++)
		{
			JSONObject routeJSON = routesJSON.getJSONObject(routesJSONArray.getString(i));

			ContentValues values = new ContentValues();
			values.put(DBContract.RouteEntry.COLUMN_NAME_ROUTE_TAG, routesJSONArray.getString(i));
			values.put(DBContract.RouteEntry.COLUMN_NAME_ROUTE_TITLE, routeJSON.getString(TITLE));

			JSONArray stopsForRoute = routeJSON.getJSONArray(STOPS);
			Log.d(LOG_TAG, "route= " + routesJSONArray.getString(i) + " len=" + stopsForRoute.length());
			for(int j = 0; j < stopsForRoute.length(); j++)
			{
				ContentValues vals = new ContentValues();
				vals.put(DBContract.RouteStopEntry.COLUMN_NAME_ROUTE, routesJSONArray.getString(i));
				vals.put(DBContract.RouteStopEntry.COLUMN_NAME_STOP, stopsForRoute.getString(j));
				//Log.d(LOG_TAG, "Entry: r= " +  routesJSONArray.getString(i) + " s= " + stopsForRoute.getString(i));
				db.insert(DBContract.RouteStopEntry.TABLE_NAME, null, vals);
			}

			db.insert(DBContract.RouteEntry.TABLE_NAME, null, values);
		}

		//Stops Table
		JSONObject stopsJSON = jsonObject.getJSONObject(STOPS);
		JSONArray stopsJSONArray = stopsJSON.names();
		for (int i = 0; i < stopsJSONArray.length(); i++)
		{
			JSONObject stopJSON = stopsJSON.getJSONObject(stopsJSONArray.getString(i));
			ContentValues values = new ContentValues();
			values.put(DBContract.StopEntry.COLUMN_NAME_STOP_TAG, stopsJSONArray.getString(i));
			values.put(DBContract.StopEntry.COLUMN_NAME_STOP_TITLE, stopJSON.getString(TITLE));
			values.put(DBContract.StopEntry.COLUMN_NAME_STOP_LAT, stopJSON.getDouble(LATITUDE));
			values.put(DBContract.StopEntry.COLUMN_NAME_STOP_LON, stopJSON.getDouble(LONGITUDE));
			db.insert(DBContract.StopEntry.TABLE_NAME, null, values);
			Stop newStop = new Stop(stopJSON.getInt(STOPID), stopJSON.getDouble(LATITUDE), stopJSON.getDouble(LONGITUDE), stopsJSONArray.getString(i), stopJSON.getString(TITLE));
			stopsMap.put(stopsJSONArray.getString(i), newStop);
		}
		db.close();
	}

	private String[] getStops(JSONObject jsonObject) throws JSONException
	{
		List<String> stops = new ArrayList<String>();
		JSONObject stopsJSON = jsonObject.getJSONObject(STOPS);
		JSONArray stopsJSONArray = stopsJSON.names();
		for (int i = 0; i < stopsJSONArray.length(); i++)
		{
			JSONObject stopJSON = stopsJSON.getJSONObject(stopsJSONArray.getString(i));
			Stop newStop = new Stop(stopJSON.getInt(STOPID), stopJSON.getDouble(LATITUDE), stopJSON.getDouble(LONGITUDE), stopsJSONArray.getString(i), stopJSON.getString(TITLE));
			stopsMap.put(stopsJSONArray.getString(i), newStop);
			stops.add(stopJSON.getString(TITLE) + " (" + stopsJSONArray.getString(i) + ")");
		}

		//Normalized Walking Distance
		double maxAllowedWalkingDistance = getNormalizedDistance(stopsJSON, SCOTT, NURSING);

		JSONObject routesJSON = jsonObject.getJSONObject(ROUTES);
		JSONArray routesJSONArray = routesJSON.names();
		for (int i = 0; i < routesJSONArray.length(); i++)
		{
			JSONObject routeJSON = routesJSON.getJSONObject(routesJSONArray.getString(i));
			String routeDisplayName = routeJSON.getString(TITLE);
			JSONArray routeStopsJSONArray = routeJSON.getJSONArray(STOPS);

			for (int j = 0; j < routeStopsJSONArray.length(); j++)
			{
				int next_j = (j == routeStopsJSONArray.length() - 1) ? 0 : (j + 1);
				String startStop = routeStopsJSONArray.getString(j);
				String endStop = routeStopsJSONArray.getString(next_j);

				Edge newEdge = new Edge(startStop, endStop, routesJSONArray.getString(i), routeDisplayName);

				stopsMap.get(startStop).outgoing.add(newEdge);
				stopsMap.get(endStop).incoming.add(newEdge);
			}
		}

		//Walking Edges
		for (int i = 0; i < stopsJSONArray.length(); i++)
		{
			for (int j = 0; j < stopsJSONArray.length(); j++)
			{
				String startStop = stopsJSONArray.getString(i);
				String endStop = stopsJSONArray.getString(j);

				if (getNormalizedDistance(stopsJSON, startStop, endStop) < maxAllowedWalkingDistance)
				{
					Edge walkingEdge = new Edge(startStop, endStop, "walk", "Walk");
					//stopsMap.get(startStop).incoming.add(walkingEdge);
					stopsMap.get(startStop).outgoing.add(walkingEdge);
					stopsMap.get(endStop).incoming.add(walkingEdge);
					//stopsMap.get(endStop).outgoing.add(walkingEdge);
				}
			}
		}
		Collections.sort(stops);
		return stops.toArray(new String[stops.size()]);
	}

	private double getNormalizedDistance(JSONObject stopsJSON, String start, String end) throws JSONException
	{
		double startLat = stopsJSON.getJSONObject(start).getDouble(LATITUDE);
		double startLon = stopsJSON.getJSONObject(start).getDouble(LONGITUDE);

		double endLat = stopsJSON.getJSONObject(end).getDouble(LATITUDE);
		double endLon = stopsJSON.getJSONObject(end).getDouble(LONGITUDE);

		return distance(startLat, startLon, endLat, endLon, 'M');
	}

	private double distance(double lat1, double lon1, double lat2, double lon2, char unit)
	{
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K')
		{
			dist = dist * 1.609344;
		} else if (unit == 'N')
		{
			dist = dist * 0.8684;
		}
		return (dist);
	}

	private double deg2rad(double deg)
	{
		return (deg * Math.PI / 180.0);
	}

	private double rad2deg(double rad)
	{
		return (rad * 180.0 / Math.PI);
	}
}