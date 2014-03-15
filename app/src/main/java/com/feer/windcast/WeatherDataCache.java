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
            sm_initialised = true;
            try
            {
                URL url = new URL("http://www.bom.gov.au/wa/observations/waall.shtml");
                sm_stations = StationListReader.GetWeatherStationsFromURL(url);
                Collections.sort(sm_stations);
            } catch (Exception e)
            {
                Log.e(TAG, "Couldnt create URL "+e.toString());
                sm_initialised = false;
                sm_stations = null;
            }
        }

        return sm_stations;
    }
}
