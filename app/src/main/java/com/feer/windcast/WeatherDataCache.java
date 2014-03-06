package com.feer.windcast;

import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 */
public class WeatherDataCache
{
    Resources m_res;
    WeatherDataCache(Resources res)
    {
        m_res = res;
    }

    private static final String TAG = "WeatherDataCache";

    public boolean ShouldUseStaticData = false;

    public WeatherData GetWeatherDataFor(URL url)
    {
        WeatherData wd = null;
        try
        {
            BufferedInputStream bis;
            if(!ShouldUseStaticData)
            {
                URLConnection urlConnection = url.openConnection();
                InputStream is = urlConnection.getInputStream();
                bis = new BufferedInputStream(is);
            }
            else
            {
                Log.w(TAG, "Using static test data");
                InputStream is = m_res.openRawResource(R.raw.test_observation_data_badgingarra);
                bis = new BufferedInputStream(is);
            }
            wd = ObservationReader.ReadJsonStream(bis);

        } catch (MalformedURLException e)
        {
            Log.e(TAG, e.getMessage());
        } catch (IOException e)
        {
            Log.e(TAG, e.getMessage());
        }
        return wd;
    }

    private static ArrayList<WeatherStation> sm_stations = new ArrayList<WeatherStation>();
    private static boolean sm_initialised = false;

    public ArrayList<WeatherStation> GetWeatherStations()
    {
        if(!sm_initialised)
        {
            InputStream is = m_res.openRawResource(R.raw.all_wa_stations);
            BufferedInputStream bis = new BufferedInputStream(is);

            sm_stations = StationListReader.GetWeatherStationList(bis);
            Collections.sort(sm_stations);

            sm_initialised = true;
        }

        return sm_stations;
    }
}
