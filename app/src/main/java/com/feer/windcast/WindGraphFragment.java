package com.feer.windcast;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class WindGraphFragment extends Fragment
{
    private static final String TAG = "WindGraphFragment";

    private static final String PARAM_KEY_STATION_URL = "param_weatherStation_URL";
    private static final String PARAM_KEY_STATION_NAME = "param_weatherStation_NAME";

    /*This is required so the fragment can be instantiated when restoring its activity's state*/
    public WindGraphFragment() {
    }

    public WindGraphFragment(WeatherStation station) {
        mStation = station;
    }

    private WeatherStation mStation = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if(savedInstanceState != null)
        {
            readBundle(savedInstanceState);
            if(mStation != null)
            {
                Log.e(TAG, "Strange state: mStation is set AND there is saved instance state. mStation could now be overridden");
            }
        }

        return rootView;
    }

    private void readBundle(Bundle savedInstanceState)
    {
        Log.v(TAG, "Reading bundle");

        String urlString = savedInstanceState.getString(PARAM_KEY_STATION_URL);
        String nameString = savedInstanceState.getString(PARAM_KEY_STATION_NAME);

        if(urlString != null)
        {
            Log.v(TAG, String.format("Previous Name (%s) and URL (%s)", nameString, urlString));

            mStation = new WeatherStation();
            try
            {
                mStation.url = new URL(urlString);
                mStation.Name = nameString;
            } catch (MalformedURLException e)
            {
                Log.e("WindCast","url not valid: " + urlString + "\n\n" + e.toString());
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        new AsyncTask<Void, Void, Boolean>()
        {
            WeatherData wd;

            @Override
            protected Boolean doInBackground(Void... params)
            {
                WeatherDataCache cache = WeatherDataCache.GetWeatherDataCache();

                URL url;
                if(mStation != null)
                {
                    url = mStation.url;
                }
                else
                {
                    Log.w(TAG, "No weather station set using first one!");
                    //TODO let the user know in a nice way!!
                    return false;
                }
                Log.i("WindCast", "Getting data from: " + url.toString());
                wd = cache.GetWeatherDataFor(url);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result)
            {
                Activity act = getActivity();
                // if the user hits back before this callback returns act could be null.
                // In this case we exit early.
                if (act == null) return;

                //TODO check the result, let the user know if we don't know what data to show them.
                final TextView label = (TextView) act.findViewById(R.id.label);
                if (label == null)
                {
                    throw new NullPointerException("unable to find the label");
                }

                if(wd == null)
                {
                    label.setText("Weather data is null!");
                }else
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(wd.Station.Name); sb.append('\n');
                    sb.append(wd.Station.State); sb.append('\n');

                    if(wd.ObservationData != null && !wd.ObservationData.isEmpty())
                    {
                        ObservationReading reading = wd.ObservationData.get(0);
                        sb.append(reading.LocalTime); sb.append("\n\n");
                        sb.append("Latest Wind Reading: ");

                        if(reading.WindBearing != null && reading.CardinalWindDirection != null && reading.WindSpeed_KMH != null)
                        {
                            sb.append(reading.WindBearing);
                            sb.append(" (").append(reading.CardinalWindDirection).append(" ) ");
                            sb.append(" ").append(reading.WindSpeed_KMH).append(" kmh");
                        }
                    }

                    label.setText(sb.toString());

                }
                // initialize our XYPlot reference:
                XYPlot  plot = (XYPlot) act.findViewById(R.id.mySimpleXYPlot);
                WindGraph.SetupGraph(wd, plot, act);
            }
        }.execute();



    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        if(mStation != null)
        {
            outState.putString(PARAM_KEY_STATION_URL, mStation.url.toString());
            outState.putString(PARAM_KEY_STATION_NAME, mStation.Name);
        }
    }
}
