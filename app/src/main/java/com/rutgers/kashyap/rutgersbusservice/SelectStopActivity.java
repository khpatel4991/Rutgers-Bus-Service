package com.rutgers.kashyap.rutgersbusservice;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class SelectStopActivity extends Activity
{

	private final static String LOG_TAG = SelectStopActivity.class.getSimpleName();

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_stop);
		if (savedInstanceState == null)
		{
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}

		String routeName = getIntent().getStringExtra("Route");
		HashMap<String, String> routesMap = (HashMap<String, String>) getIntent().getSerializableExtra("RouteMap");

		try
		{
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected())
			{
				Log.i(LOG_TAG, "Network is Connected");
				new GetParticularStopsTask(this).execute(routesMap.get(routeName));
			} else Log.e(LOG_TAG, "Network is not connected");

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
		getMenuInflater().inflate(R.menu.select_stop, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment
	{

		public PlaceholderFragment()
		{
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			final View rootView = inflater.inflate(R.layout.fragment_select_stop, container, false);
			String routeName = getActivity().getIntent().getStringExtra("Route");
			TextView textView = (TextView) rootView.findViewById(R.id.stop_selector);
			textView.setText("Select stops for Bus " + routeName);

			final Spinner spinnerS = (Spinner) rootView.findViewById(R.id.spinner_source_for_route);
			final Spinner spinnerD = (Spinner) rootView.findViewById(R.id.spinner_destination_for_route);

			final Button button = (Button) rootView.findViewById(R.id.button_selected_stops);

			button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					long sourceId = spinnerS.getSelectedItemId();
					long destinationId = spinnerD.getSelectedItemId();

					String source = spinnerS.getSelectedItem().toString();
					String destination = spinnerD.getSelectedItem().toString();

					if (sourceId != 0 && destinationId != 0)
					{
						if (sourceId != destinationId)
						{
							Intent intent = new Intent();
							intent.putExtra("Source", source);
							intent.putExtra("Destination", destination);
							getActivity().setResult(RESULT_OK, intent);
							getActivity().finish();
							//startActivity(intent);
						}
					}
				}
			});


			return rootView;
		}
	}

	private class GetParticularStopsTask extends AsyncTask<String, Void, String[]>
	{

		private Context _context;

		public GetParticularStopsTask(Context c) {_context = c;}

		private final String LOG_TAG = GetParticularStopsTask.class.getSimpleName();

		private ProgressDialog progressDialog = new ProgressDialog(SelectStopActivity.this);

		private final static String BASE_URL = "http://runextbus.herokuapp.com/config";

		private final static String ROUTES = "routes";
		private final static String STOPS = "stops";

		InputStream is = null;
		String result = "";


		@Override
		protected String[] doInBackground(String... strings)
		{

			try
			{
				Log.d(LOG_TAG, "In DO");
				URL url = new URL(BASE_URL);
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


				return getRouteStops(jsonObject, strings[0]);


			} catch (Exception e)
			{
				Log.e(LOG_TAG, e.getMessage());
			}
			return new String[0];
		}

		String[] getRouteStops(JSONObject jsonObject, String selectedStop) throws JSONException
		{
			ArrayList<String> stops = new ArrayList<String>();
			JSONObject routesJSON = jsonObject.getJSONObject(ROUTES);
			JSONObject routeJSON = routesJSON.getJSONObject(selectedStop);
			JSONArray stopsJSONArray = routeJSON.getJSONArray(STOPS);
			for (int i = 0; i < stopsJSONArray.length(); i++)
			{
				stops.add(stopsJSONArray.getString(i));
			}
			stops.add(0, "Select a stop");
			return stops.toArray(new String[stops.size()]);
		}

		@Override
		protected void onPostExecute(String[] stops)
		{
			super.onPostExecute(stops);
			final Spinner spinner1 = (Spinner) findViewById(R.id.spinner_source_for_route);
			final Spinner spinner2 = (Spinner) findViewById(R.id.spinner_destination_for_route);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(_context, android.R.layout.simple_spinner_item, stops);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner1.setAdapter(adapter);
			spinner2.setAdapter(adapter);
			this.progressDialog.dismiss();

		}
	}

}
