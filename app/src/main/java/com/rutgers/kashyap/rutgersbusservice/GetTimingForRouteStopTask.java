package com.rutgers.kashyap.rutgersbusservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.rutgers.kashyap.rutgersbusservice.LinkedList.Node;
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
import java.util.List;

/**
 * Created by khpatel4991 on 3/23/15.
 */
public class GetTimingForRouteStopTask extends AsyncTask<String, Void, String>
{

    private static final String LOG_TAG = GetTimingForRouteStopTask.class.getSimpleName();

    private Activity _activity;

    private ProgressDialog _progressDialog;

    private DBHelper _DBHelper;

    SQLiteDatabase _db;


    private final static String URL_BASE = "http://runextbus.herokuapp.com/route/";

    private String _routeTitle;
    private String _sourceTitle;
    private String _destinationTitle;
    private ArrayList<Double> _sMinutes = new ArrayList<>();
    private ArrayList<Double> _dMinutes = new ArrayList<>();
    private List<Node> _thisRoute = new ArrayList<>();

    private final static String TIMINGS = "predictions";
    private final static String SECONDS = "seconds";
    private final static String MINUTES = "minutes";

    private final static String TITLE = "title";

    InputStream is = null;
    String result = "";

    public GetTimingForRouteStopTask(Activity activity, List<Node> nodeList, ArrayList<Double> sMinutes, ArrayList<Double> dMinutes)
    {
        _activity = activity;
        _thisRoute = nodeList;
        _sMinutes = sMinutes;
        _dMinutes = dMinutes;
        _progressDialog = new ProgressDialog(_activity);
        _DBHelper = new DBHelper(_activity);
        _db = _DBHelper.getWritableDatabase();
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        _progressDialog.setMessage("Getting Time For Route");
        _progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params)
    {
        _routeTitle = params[0];
        _sourceTitle = params[1];
        _destinationTitle = params[2];
        try
        {
            String routeTag = "";
            Cursor c = _db.rawQuery(DBContract.RouteEntry.GET_ROUTE_TAG_FROM_TITLE_QUERY + "'" + params[0] + "';", null);
            if(c.moveToFirst())
                routeTag = c.getString(0);
            c.close();
            Log.i(LOG_TAG, URL_BASE + routeTag);
            URL url = new URL(URL_BASE + routeTag);
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

            JSONArray jsonArray;
            // Convert string to object
            try
            {
                jsonArray = new JSONArray(result);
            }
            catch (JSONException e)
            {
                return null;
            }
            addTimingInDB(jsonArray);
            return updateText();
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String params)
    {
        super.onPostExecute(params);
        TextView textView =  (TextView) _activity.findViewById(R.id.label_add);
        textView.setText(params);
        _progressDialog.dismiss();
    }

    void addTimingInDB(JSONArray jsonArray) throws JSONException
    {
        _sMinutes.clear();
        _dMinutes.clear();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            JSONObject stop = jsonArray.getJSONObject(i);
            if (_sourceTitle.equals(stop.getString(TITLE)) || _destinationTitle.equals(stop.getString(TITLE)))
            {

                JSONArray timings = stop.getJSONArray(TIMINGS);
                for (int j = 0; j < timings.length(); j++)
                {
                    //ContentValues values = new ContentValues();
                    //values.put(DBContract.RouteStopTimeEntry.COLUMN_NAME_ROUTE, _routeTitle);
                    //values.put(DBContract.RouteStopTimeEntry.COLUMN_NAME_STOP, stop.getString(TITLE));
                    //values.put(DBContract.RouteStopTimeEntry.COLUMN_NAME_TIME, timings.getJSONObject(j).getDouble(SECONDS));
                    if(_sourceTitle.equals(stop.getString(TITLE)))
                        _sMinutes.add(timings.getJSONObject(j).getDouble(MINUTES));
                    else
                        _dMinutes.add(timings.getJSONObject(j).getDouble(MINUTES));
                    //_db.insert(DBContract.RouteStopTimeEntry.TABLE_NAME, null, values);
                }
            }
        }
        _db.close();
    }

    public String updateText()
    {
        StringBuilder output = new StringBuilder();
        for(int j = 0; j < _thisRoute.size(); j++)
        {
            Node temp = _thisRoute.get(j);
            output.append(temp.route + "\n");
            output.append(temp.source + ": ");
            for (int i = 0; i < temp.sMinutes.size(); i++)
                output.append(temp.sMinutes.get(i).intValue() + ", ");
            output.append("\n");
            output.append(temp.destination + ": ");
            for (int i = 0; i < temp.dMinutes.size(); i++)
                output.append(temp.dMinutes.get(i).intValue() + ", ");
            output.append("\n");
        }
        output.append("\n");
        return output.toString();
    }

    void loadTimingFromDB()
    {
        _db = _DBHelper.getReadableDatabase();
        Cursor c = _db.rawQuery(
                DBContract.RouteStopTimeEntry.GET_TIMES_FROM_ROUTE_STOP_QUERY1 + "'" + _routeTitle + "'" +
                        DBContract.RouteStopTimeEntry.GET_TIMES_FROM_ROUTE_STOP_QUERY2 + "'" + _sourceTitle + "';", null);
        if(c.moveToFirst())
        {
            do
            {
                _sMinutes.add(c.getDouble(0));
            } while(c.moveToNext());
        }
        c.close();
        _db.close();
    }

}
