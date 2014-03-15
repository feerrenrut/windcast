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

    private static class AllStationsURLForState
    {
        public AllStationsURLForState(String urlString, String state)
        {mUrlString = urlString; mState = state;}

        public String mUrlString;
        public String mState;
    }

    private static final AllStationsURLForState[] mAllStationsInState_UrlList =
    {
            new AllStationsURLForState("http://www.bom.gov.au/wa/observations/waall.shtml", "WA"),
            new AllStationsURLForState("http://www.bom.gov.au/nsw/observations/nswall.shtml", "NSW"), //Strangely some the stations for ACT (inc. Canberra) are on this page!
            new AllStationsURLForState("http://www.bom.gov.au/vic/observations/vicall.shtml", "VIC"),
            new AllStationsURLForState("http://www.bom.gov.au/qld/observations/qldall.shtml", "QLD"),
            new AllStationsURLForState("http://www.bom.gov.au/sa/observations/saall.shtml", "SA"),
            new AllStationsURLForState("http://www.bom.gov.au/tas/observations/tasall.shtml", "TAS"),
            new AllStationsURLForState("http://www.bom.gov.au/act/observations/canberra.shtml", "ACT"),
            new AllStationsURLForState("http://www.bom.gov.au/nt/observations/ntall.shtml", "NT")
    };

    public ArrayList<WeatherStation> GetWeatherStations()
    {
        if(!sm_initialised)
        {
            sm_initialised = true;
            for(AllStationsURLForState stationLink : mAllStationsInState_UrlList)
            {
                try
                {
                    URL url = new URL(stationLink.mUrlString);
                    sm_stations.addAll(StationListReader.GetWeatherStationsFromURL(url, stationLink.mState));
                } catch (Exception e)
                {
                    Log.e(TAG, "Couldn't create URL "+e.toString());
                    sm_initialised = false;
                    sm_stations = null;
                }
            }
            Collections.sort(sm_stations);
        }

        return sm_stations;
    }
}
