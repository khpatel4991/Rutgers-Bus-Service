package com.rutgers.kashyap.rutgersbusservice;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.rutgers.kashyap.rutgersbusservice.LinkedList.Node;

import java.util.ArrayList;
import java.util.List;


public class CreateRouteActivity extends Activity
{
	private final static String LOG_TAG = CreateRouteActivity.class.getSimpleName();

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_route);
		if (savedInstanceState == null)
			getFragmentManager().beginTransaction().add(R.id.container, new CreateRouteFragment()).commit();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_route, menu);
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
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class CreateRouteFragment extends Fragment
	{
		private List<Node> thisRoute = new ArrayList<>();

		public CreateRouteFragment() {}

		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState)
		{
			final View rootView = inflater.inflate(R.layout.fragment_create_route, container, false);

			new ShowRoutesTask(getActivity()).execute();

			final Spinner spinnerRoutes = (Spinner) rootView.findViewById(R.id.spinner_routes);

			final Spinner spinnerSource = (Spinner) rootView.findViewById(R.id.spinner_source);
			final Spinner spinnerDestination = (Spinner) rootView.findViewById(R.id.spinner_destination);

			final Button buttonAdd = (Button) rootView.findViewById(R.id.button_create);

			spinnerRoutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
			{
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int id, long pos)
				{
					new GetStopsForRouteTask(getActivity()).execute(spinnerRoutes.getSelectedItem().toString());
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent)
				{

				}
			});

			buttonAdd.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Node newNode = new Node();
					newNode.route = spinnerRoutes.getSelectedItem().toString();
					newNode.source = spinnerSource.getSelectedItem().toString();
					newNode.destination = spinnerDestination.getSelectedItem().toString();
					new GetTimingForRouteStopTask(getActivity(), thisRoute, newNode.sMinutes, newNode.dMinutes).execute(
                            newNode.route,
                            newNode.source,
                            newNode.destination
                    );
                    thisRoute.add(newNode);
				}
			});
			return rootView;
		}
	}
}
/*	private class GetTimingsForRouteTask extends AsyncTask<String, Void, ArrayList<Double>>
	{
		private final String LOG_TAG = GetTimingsForRouteTask.class.getSimpleName();
		private final static String TITLE = "title";
		private final static String TIMINGS = "predictions";
		private final static String SECONDS = "seconds";
		private final static String BASE_URL = "http://runextbus.herokuapp.com/route/";
		private String _source;
		private String _destination;
		InputStream is = null;
		String result = "";

		@Override
		protected void onPostExecute(ArrayList<Double> doubles)
		{
			super.onPostExecute(doubles);
			Node newNode = new Node(route, _source, _destination, doubles);
			myRoute.append(newNode);
			Toast.makeText(getApplicationContext(), seconds.get(0).toString(), Toast.LENGTH_SHORT).show();
		}

		@Override
		protected ArrayList<Double> doInBackground(String... strings)
		{
			_source = strings[2];
			_destination = strings[3];
			try
			{
				Log.d(LOG_TAG, "In DO");
				URL url = new URL(BASE_URL + strings[0]);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000 *//* milliseconds *//*);
				conn.setConnectTimeout(15000 *//* milliseconds *//*);
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
				} catch (Exception e)
				{
					return null;
				}

				JSONObject jsonObject;
				JSONArray jsonArray;
				// Convert string to object
				try
				{
					jsonArray = new JSONArray(result);
					//jsonObject = new JSONObject(result);
				} catch (JSONException e)
				{
					return null;
				}


				return getTimings(jsonArray, strings[0], strings[1]);


			} catch (Exception e)
			{
				Log.e(LOG_TAG, e.getMessage());
			}


			return null;
		}

		ArrayList<Double> getTimings(JSONArray jsonArray, String route, String sourceStop) throws JSONException
		{
			//ArrayList<Double> seconds = new ArrayList<Double>();
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject stop = jsonArray.getJSONObject(i);
				if (sourceStop.equals(stop.getString(TITLE))) ;
				{
					JSONArray timings = stop.getJSONArray(TIMINGS);
					for (int j = 0; j < timings.length(); j++)
						seconds.add(timings.getJSONObject(j).getDouble(SECONDS));
					return seconds;
				}
			}
			return null;
		}
	}


	private class GetRoutesTask extends AsyncTask<Void, Void, String[]>
	{
		private Context _context;

		public GetRoutesTask(Context context)
		{
			this._context = context;
		}

		private final String LOG_TAG = GetRoutesTask.class.getSimpleName();

		private ProgressDialog progressDialog = new ProgressDialog(CreateRouteActivity.this);

		private final static String BASE_URL = "http://runextbus.herokuapp.com/config";

		private final static String TITLE = "title";
		private final static String ROUTES = "routes";

		InputStream is = null;
		String result = "";

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			progressDialog.setMessage("Getting Routes...");
			progressDialog.show();
		}

		@Override
		protected String[] doInBackground(Void... voids)
		{

			try
			{
				Log.d(LOG_TAG, "In DO");
				URL url = new URL(BASE_URL);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000 *//* milliseconds *//*);
				conn.setConnectTimeout(15000 *//* milliseconds *//*);
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


				return getRoutes(jsonObject);


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
			final Spinner spinnerRoutes = (Spinner) findViewById(R.id.spinner_routes);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(_context, android.R.layout.simple_spinner_item, stops);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerRoutes.setAdapter(adapter);
			this.progressDialog.dismiss();
		}

		private String[] getRoutes(JSONObject jsonObject) throws JSONException
		{
			Log.i(LOG_TAG, "getRoutes");
			List<String> routes = new ArrayList<String>();

			JSONObject routesJSON = jsonObject.getJSONObject(ROUTES);
			JSONArray routesJSONArray = routesJSON.names();
			for (int i = 0; i < routesJSONArray.length(); i++)
			{
				JSONObject routeJSON = routesJSON.getJSONObject(routesJSONArray.getString(i));
				routesMap.put(routeJSON.getString(TITLE), routesJSONArray.getString(i));
				routes.add(routeJSON.getString(TITLE));
			}
			Collections.sort(routes);
			routes.add(0, "Select a stop");
			return routes.toArray(new String[routes.size()]);
		}
	}*/