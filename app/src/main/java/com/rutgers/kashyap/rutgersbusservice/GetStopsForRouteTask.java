package com.rutgers.kashyap.rutgersbusservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.rutgers.kashyap.rutgersbusservice.data.DBContract;
import com.rutgers.kashyap.rutgersbusservice.data.DBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khpatel4991 on 3/20/2015.
 */
public class GetStopsForRouteTask extends AsyncTask<String, Void, String[]>
{

	private static final String LOG_TAG = GetStopsForRouteTask.class.getSimpleName();

	private Activity _activity;

	private ProgressDialog _progressDialog;

	private DBHelper _DBHelper;

	public GetStopsForRouteTask(Activity activity)
	{
		_activity = activity;
		_progressDialog = new ProgressDialog(_activity);
		_DBHelper = new DBHelper(_activity);
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
	}

	@Override
	protected String[] doInBackground(String... route)
	{
		return getStopsForRouteFromDB(route[0]);
	}

	@Override
	protected void onPostExecute(String[] stops)
	{
		super.onPostExecute(stops);
		final Spinner spinnerSource = (Spinner) _activity.findViewById(R.id.spinner_source);
		final Spinner spinnerDestination = (Spinner) _activity.findViewById(R.id.spinner_destination);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(_activity, android.R.layout.simple_spinner_item, stops);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSource.setAdapter(adapter);
		spinnerDestination.setAdapter(adapter);
		_progressDialog.dismiss();
	}

	String[] getStopsForRouteFromDB(String routeTitle)
	{
		SQLiteDatabase db = _DBHelper.getReadableDatabase();
		List<String> stopsForRoute = new ArrayList<>();
		Cursor c = db.rawQuery(DBContract.RouteStopEntry.GET_STOPS_FROM_ROUTE_TITLE_QUERY + "'" + routeTitle + "';", null);
		if(c.moveToFirst())
		{
			do
			{
				stopsForRoute.add(c.getString(1));
			} while(c.moveToNext());
		}
		c.close();
		db.close();
		return stopsForRoute.toArray(new String[stopsForRoute.size()]);
	}

}

