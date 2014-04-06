package com.feer.windcast;

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
    WeatherDataCache()
    {
    }

    private static final String TAG = "WeatherDataCache";

    public WeatherData GetWeatherDataFor(URL url)
    {
        WeatherData wd = null;
        try
        {
            BufferedInputStream bis;

            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            bis = new BufferedInputStream(is);

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

    public ArrayList<WeatherStation> GetWeatherStationsFromAllStates()
    {
        if(!smInitialised)
        {
            smInitialised = true;
            for(AllStationsURLForState stationLink : mAllStationsInState_UrlList)
            {
                try
                {
                    URL url = new URL(stationLink.mUrlString);
                    smStations.addAll(StationListReader.GetWeatherStationsFromURL(url, stationLink.mState));
                } catch (Exception e)
                {
                    Log.e(TAG, "Couldn't create URL "+e.toString());
                    smInitialised = false;
                    smStations = null;
                }
            }
            Collections.sort(smStations);
        }

        return smStations;
    }

    public ArrayList<WeatherStation> GetWeatherStationsFrom(String state)
    {
        if(!smInitialised)
        {
            GetWeatherStationsFromAllStates();
        }

        ArrayList<WeatherStation> stationsForState = new ArrayList<WeatherStation>();
        for(WeatherStation station : smStations)
        {
            if(station.State.equals(state))
            {
                stationsForState.add(station);
            }
        }
        return stationsForState;
    }
}
