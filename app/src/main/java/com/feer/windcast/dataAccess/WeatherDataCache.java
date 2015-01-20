package com.feer.windcast.dataAccess;

import android.os.AsyncTask;
import android.util.Log;

import com.feer.windcast.ObservationReader;
import com.feer.windcast.StationListReader;
import com.feer.windcast.WeatherData;
import com.feer.windcast.WeatherStation;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;


class InternalCache implements LoadedWeatherCache {

    protected InternalCache()
    {}

    private ArrayList<WeatherStation> mStations;
    @Override
    public ArrayList<WeatherStation> GetWeatherStationsFrom(String state) {
        if (GetWeatherStationsFromAllStates() == null) {
            return null;
        }

        ArrayList<WeatherStation> stationsForState = new ArrayList<WeatherStation>();
        for (WeatherStation station : mStations) {
            if (station.GetStateAbbreviated().equals(state)) {
                stationsForState.add(station);
            }
        }
        return stationsForState;
    }

    @Override
    public ArrayList<WeatherStation> GetWeatherStationsFromAllStates() {
        if (mStations == null) {
            InitialiseInternalStationList();
        }

        return mStations;
    }

    protected void InitialiseInternalStationList() {
        mStations = new ArrayList<WeatherStation>();
        for(AllStationsURLForState stationLink : mAllStationsInState_UrlList)
        {
            try
            {
                URL url = new URL(stationLink.mUrlString);
                mStations.addAll(StationListReader.GetWeatherStationsFromURL(url, stationLink.mState));
            } catch (Exception e)
            {
                Log.e(WeatherDataCache.TAG, "Couldn't create URL " + e.toString());
                mStations = null;
            }
        }
        if(mStations != null )
        {
            Collections.sort(mStations);
        }
    }

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
}
/**
 *
 */
public class WeatherDataCache
{
    private WeatherDataCache()
    { }

    public interface NotifyWhenCacheFilled
    {
        void OnCacheFilled(LoadedWeatherCache fullCache);
    }

    final private static ArrayList<NotifyWhenCacheFilled> sNotifyUs = new ArrayList<NotifyWhenCacheFilled>();
    private static InternalCache sWeatherDataCache = null;
    private static boolean sCacheFilled = false;
    protected static final String TAG = "WeatherDataCache";

    public static void OnCacheFilled(NotifyWhenCacheFilled notify)
    {
        if(isCacheFilled())
        {
            notify.OnCacheFilled(sWeatherDataCache);
        }
        else
        {
            sNotifyUs.add(notify);
            if(sWeatherDataCache == null)
            {
                triggerFillCache();
            }
        }
    }

    private static void triggerFillCache()
    {
        sWeatherDataCache = new InternalCache();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                sWeatherDataCache.InitialiseInternalStationList();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                sCacheFilled = true;

                for(NotifyWhenCacheFilled notify : sNotifyUs)
                {
                    notify.OnCacheFilled(sWeatherDataCache);
                }
                sNotifyUs.clear();
            }
        }.execute();
    }

    public static boolean isCacheFilled() {return sCacheFilled;}

    public static LoadedWeatherCache GetLoadedWeatherCache()
    {
        if(isCacheFilled())
        {
            return sWeatherDataCache;
        }
        else
        {
            return null;
        }
    }

    public static FavouriteStationCache CreateNewFavouriteStationAccessor()
    {
        return new FavouriteStationCache();
    }

    public static WeatherData GetWeatherDataFor(WeatherStation station)
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
}
