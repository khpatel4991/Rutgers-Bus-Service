package com.rutgers.kashyap.rutgersbusservice;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.rutgers.kashyap.rutgersbusservice.Graph.Edge;
import com.rutgers.kashyap.rutgersbusservice.Graph.Stop;
import com.rutgers.kashyap.rutgersbusservice.data.DBHelper;
import com.rutgers.kashyap.rutgersbusservice.data.DBContract;

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

public class MainActivity extends Activity
{
	private static final String LOG_TAG = MainActivity.class.getSimpleName();
	private HashMap<String, Stop> stopsMap = new HashMap<String, Stop>();


	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null)
		{
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}

		try
		{
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected())
			{

				Log.i(LOG_TAG, "Network is Connected");

				new InsertDBData(this).execute();


			} else
			{
				// display error
				Log.e(LOG_TAG, "Network is not connected");

			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			Log.d(LOG_TAG, "Finally");
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) return true;

		else if (id == R.id.action_createcustomroutes)
		{
			Intent intent = new Intent(this, CreateRouteActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment
	{

		public PlaceholderFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	}

	private class InsertDBData extends AsyncTask<Void, Void, String[]>
	{
		private Context context;

		public InsertDBData(Context context)
		{
			this.context = context;
		}

		private final String LOG_TAG = InsertDBData.class.getSimpleName();

		private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

		private DBHelper _DBHelper = new DBHelper(MainActivity.this);

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
			progressDialog.setMessage("Downloading your data...");
			progressDialog.show();
		}

		@Override
		protected String[] doInBackground(Void... voids)
		{
			try
			{
				Log.d(LOG_TAG, "In DO");
				URL url = new URL(URL_CONFIG);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);

				Log.i(LOG_TAG, "166");
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
				} catch (Exception e)
				{
					return null;
				}

				JSONObject jsonObject;
				// Convert string to object
				try
				{
					jsonObject = new JSONObject(result);
				} catch (JSONException e)
				{
					return null;
				}
				insertData(jsonObject);
				return getStops(jsonObject);


			} catch (Exception e)
			{
				Log.e(LOG_TAG, e.getMessage());
			}

			return new String[0];
		}

		@Override
		protected void onPostExecute(String[] stops)
		{
			super.onPostExecute(stops);
			final Spinner spinnerSource = (Spinner) findViewById(R.id.spinner_destination);
			final Spinner spinnerDestination = (Spinner) findViewById(R.id.spinner_source);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, stops);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerSource.setAdapter(adapter);
			spinnerDestination.setAdapter(adapter);
			this.progressDialog.dismiss();
		}

		private void insertData(JSONObject jsonObject) throws JSONException
		{
			Log.d(LOG_TAG, "insertData");
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

		}

		private String[] getStops(JSONObject jsonObject) throws JSONException
		{
			Log.i(LOG_TAG, "getStops");
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
}
