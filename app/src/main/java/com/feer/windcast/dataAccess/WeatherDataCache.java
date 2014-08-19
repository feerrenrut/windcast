package com.feer.windcast.dataAccess;

import android.util.Log;

import com.feer.windcast.ObservationReader;
import com.feer.windcast.StationListReader;
import com.feer.windcast.WeatherData;
import com.feer.windcast.WeatherStation;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 */
public class WeatherDataCache
{
    private WeatherDataCache()
    {
    }

    private static WeatherDataCache sWeatherDataCache = null;

    public static WeatherDataCache GetWeatherDataCache()
    {
        if(sWeatherDataCache == null)
        {
            sWeatherDataCache = new WeatherDataCache();
        }
        return sWeatherDataCache;
    }

    /*
    *Used in tests... unfortunate
     */
    public static void SetsWeatherDataCache(WeatherDataCache cache)
    {
        sWeatherDataCache = cache;
    }

    private static final String TAG = "WeatherDataCache";

    public FavouriteStationCache CreateNewFavouriteStationAccessor()
    {
        return new FavouriteStationCache();
    }

    public WeatherData GetWeatherDataFor(WeatherStation station)
    {
        WeatherData wd = null;
        try
        {
            ObservationReader obs = new ObservationReader(station);
            wd = obs.GetWeatherData();

        } catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }

        return wd;
    }

    private static ArrayList<WeatherStation> smStations;

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
        if(smStations == null)
        {
            smStations = new ArrayList<WeatherStation>();
            for(AllStationsURLForState stationLink : mAllStationsInState_UrlList)
            {
                try
                {
                    URL url = new URL(stationLink.mUrlString);
                    smStations.addAll(StationListReader.GetWeatherStationsFromURL(url, stationLink.mState));
                } catch (Exception e)
                {
                    Log.e(TAG, "Couldn't create URL "+e.toString());
                    smStations = null;
                }
            }
            if(smStations != null )
            {
                Collections.sort(smStations);
            }
        }

        return smStations;
    }

    public ArrayList<WeatherStation> GetWeatherStationsFrom(String state)
    {
        if( GetWeatherStationsFromAllStates() == null)
        {
            return null;
        }

        ArrayList<WeatherStation> stationsForState = new ArrayList<WeatherStation>();
        for(WeatherStation station : smStations)
        {
            if(station.GetStateAbbreviated().equals(state))
            {
                stationsForState.add(station);
            }
        }
        return stationsForState;
    }
}
