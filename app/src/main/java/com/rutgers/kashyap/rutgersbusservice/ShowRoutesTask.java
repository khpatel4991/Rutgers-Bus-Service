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
public class ShowRoutesTask extends AsyncTask<Void, Void, String[]>
{

	private static final String LOG_TAG = ShowRoutesTask.class.getSimpleName();

	private Activity _activity;

	private ProgressDialog _progressDialog;

	private DBHelper _DBHelper;


	public ShowRoutesTask(Activity activity)
	{
		_activity = activity;
		_progressDialog = new ProgressDialog(_activity);
		_DBHelper = new DBHelper(_activity);
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		_progressDialog.setMessage("Populating Routes...");
		_progressDialog.show();

	}

	@Override
	protected String[] doInBackground(Void... params)
	{
		return getRoutesFromDB();
	}

	@Override
	protected void onPostExecute(String[] routes)
	{
		super.onPostExecute(routes);
		final Spinner spinnerRoutes = (Spinner) _activity.findViewById(R.id.spinner_routes);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(_activity, android.R.layout.simple_spinner_item, routes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerRoutes.setAdapter(adapter);
		_progressDialog.dismiss();
	}

	String[] getRoutesFromDB()
	{
		SQLiteDatabase db = _DBHelper.getReadableDatabase();
		List<String> routes = new ArrayList<>();
		Cursor c = db.rawQuery(DBContract.RouteEntry.SELECT_ALL_SORTED_QUERY, null);
		if(c.moveToFirst())
		{
			do
			{
				routes.add(c.getString(1));
			} while(c.moveToNext());
		}
		c.close();
		db.close();
		return routes.toArray(new String[routes.size()]);

	}
}
