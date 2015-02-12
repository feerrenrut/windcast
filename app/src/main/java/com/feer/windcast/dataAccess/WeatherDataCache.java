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

    private final ArrayList<WeatherStation> mStations = new ArrayList<WeatherStation>();
    private final ArrayList<String> mStatesLoaded = new ArrayList<String>();
    
    @Override
    public ArrayList<WeatherStation> GetWeatherStationsFrom(String state) {
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
        return mStations;
    }
    
    public void AddStationsForState(ArrayList<WeatherStation> stations, String state)
    {
        mStations.addAll(stations);
        mStatesLoaded.add(state);
        Collections.sort(mStations);
    }
    
    public boolean StationsForAllStatesAdded()
    {
        return mStatesLoaded.size() == mAllStationsInState_UrlList.length;
    }

    static class AllStationsURLForState
    {
        public AllStationsURLForState(String urlString, String state)
        {mUrlString = urlString; mState = state;}

        public String mUrlString;
        public String mState;
    }

    static final AllStationsURLForState[] mAllStationsInState_UrlList =
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
    public interface NotifyWhenCacheFilled
    {
        void OnCacheFilled(LoadedWeatherCache fullCache);
    }

    final private ArrayList<NotifyWhenCacheFilled> mNotifyUs = new ArrayList<NotifyWhenCacheFilled>();
    protected static final String TAG = "WeatherDataCache";

    /* when this is null, loading of the cache has not yet started.
    * Has to be package local for tests.
     */
    InternalCache mWeatherDataCache = null;
    private static WeatherDataCache sInstance = null;
    
    private WeatherDataCache()
    { }
    
    public static WeatherDataCache GetCache()
    {
        if(sInstance == null)
        {
            SetWeatherDataCache(new WeatherDataCache());
        }
        
        return sInstance;
    }
    
    public static void SetWeatherDataCache(WeatherDataCache instance)
    {
        sInstance = instance;
    }
    
    private InternalCache GetInternalCache()
    {
        return mWeatherDataCache;
    }


    public void OnCacheFilled(NotifyWhenCacheFilled notify)
    {
        if(isCacheFilled())
        {
            notify.OnCacheFilled(GetInternalCache());
        }
        else
        {
            mNotifyUs.add(notify);
            if(GetInternalCache() == null)
            {
                triggerFillCache();
            }
        }
    }

    private boolean isCacheFilled() {return GetInternalCache() != null && GetInternalCache().StationsForAllStatesAdded();}

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

    private void triggerFillCache()
    {
        mWeatherDataCache = new InternalCache();

        for(final InternalCache.AllStationsURLForState stationLink : InternalCache.mAllStationsInState_UrlList)
        {
            new AsyncTask<Void, Void, ArrayList<WeatherStation>>() {
                @Override
                protected ArrayList<WeatherStation> doInBackground(Void... params) {
                    ArrayList<WeatherStation> stations = new ArrayList<WeatherStation>();
                    try
                    {
                        URL url = new URL(stationLink.mUrlString);
                        stations.addAll(StationListReader.GetWeatherStationsFromURL(url, stationLink.mState));
                    } catch (Exception e)
                    {
                        Log.e(WeatherDataCache.TAG, "Couldn't create URL " + e.toString());
                        stations = null;
                    }
                    return stations;
                }

                @Override
                protected void onPostExecute(ArrayList<WeatherStation> weatherStations) {
                    if(weatherStations != null)
                    {
                        mWeatherDataCache.AddStationsForState(weatherStations, stationLink.mState);
                    }
                    
                    if(mWeatherDataCache.StationsForAllStatesAdded())
                    {
                        NotifyOfCacheFilled();
                    }
                }
            }.execute();
        }
    }

    private void NotifyOfCacheFilled() {
        for(NotifyWhenCacheFilled notify : mNotifyUs)
        {
            notify.OnCacheFilled(GetInternalCache());
        }
        mNotifyUs.clear();
    }
}
