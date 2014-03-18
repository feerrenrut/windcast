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
    Resources mRes;
    WeatherDataCache(Resources res)
    {
        mRes = res;
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
                InputStream is = mRes.openRawResource(R.raw.test_observation_data_badgingarra);
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

    private static ArrayList<WeatherStation> smStations = new ArrayList<WeatherStation>();
    private static boolean smInitialised = false;

    public ArrayList<WeatherStation> GetWeatherStations()
    {
        if(!smInitialised)
        {
            InputStream is = mRes.openRawResource(R.raw.all_wa_stations);
            BufferedInputStream bis = new BufferedInputStream(is);

            smStations = StationListReader.GetWeatherStationList(bis);
            Collections.sort(smStations);

            smInitialised = true;
        }

        return smStations;
    }
}
