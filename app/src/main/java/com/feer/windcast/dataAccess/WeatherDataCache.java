package com.feer.windcast.dataAccess;

import android.os.AsyncTask;
import android.util.Log;

import com.feer.windcast.ObservationReader;
import com.feer.windcast.StationListReader;
import com.feer.windcast.WeatherData;
import com.feer.windcast.WeatherStation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


class InternalStationCache implements LoadedWeatherStationCache {

    protected InternalStationCache()
    {}

    private final ArrayList<WeatherData> mStations = new ArrayList<WeatherData>();
    private final ArrayList<String> mStatesLoaded = new ArrayList<String>();
    
    @Override
    public ArrayList<WeatherData> GetWeatherStationsFrom(String state) {
        ArrayList<WeatherData> stationsForState = new ArrayList<WeatherData>();
        for (WeatherData station : mStations) {
            if (station.Station.GetStateAbbreviated().equals(state)) {
                stationsForState.add(station);
            }
        }
        return stationsForState;
    }

    @Override
    public ArrayList<WeatherData> GetWeatherStationsFromAllStates() {
        return mStations;
    }
    
    public void AddStationsForState(ArrayList<WeatherData> stations, String state)
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
    public interface NotifyWhenStationCacheFilled
    {
        void OnCacheFilled(LoadedWeatherStationCache fullCache);
    }

    final private ArrayList<NotifyWhenStationCacheFilled> mNotifyUs = new ArrayList<NotifyWhenStationCacheFilled>();
    protected static final String TAG = "WeatherDataCache";

    /* when this is null, loading of the cache has not yet started.
    * Has to be package local for tests.
     */
    InternalStationCache mInternalStationCache = null;
    Date mInternalStationCacheTime = null;
    private static WeatherDataCache sInstance = null;
    
    private WeatherDataCache() { }
    
    public static WeatherDataCache GetInstance()
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
    
    private InternalStationCache GetInternalStationCache()
    {
        return mInternalStationCache;
    }
    
    private boolean IsStationCacheStale()
    {
        long cacheTimeout = 15L * 60 * 1000; // 15 min
        
        return mInternalStationCacheTime != null &&
               new Date().getTime() - mInternalStationCacheTime.getTime() // elapsed time
               > cacheTimeout;
    }

    public void OnStationCacheFilled(NotifyWhenStationCacheFilled notify)
    {
        if( IsStationCacheFilled() && IsStationCacheStale() )
        {
            notify.OnCacheFilled(GetInternalStationCache());
            mInternalStationCache = null;
            mInternalStationCacheTime = null;
        }
        
        if(IsStationCacheFilled())
        {
            notify.OnCacheFilled(GetInternalStationCache());
        }
        else
        {
            mNotifyUs.add(notify);
            if(GetInternalStationCache() == null)
            {
                TriggerFillStationCache();
            }
        }
    }

    private boolean IsStationCacheFilled() {return GetInternalStationCache() != null && GetInternalStationCache().StationsForAllStatesAdded();}

    public FavouriteStationCache CreateNewFavouriteStationAccessor()
    {
        return new FavouriteStationCache();
    }

    /* Performs http request, should be called using AsyncTask
     */
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

    private void TriggerFillStationCache()
    {
        mInternalStationCacheTime = null;
        mInternalStationCache = new InternalStationCache();

        for(final InternalStationCache.AllStationsURLForState stationLink : InternalStationCache.mAllStationsInState_UrlList)
        {
            new AsyncTask<Void, Void, ArrayList<WeatherData>>() {
                @Override
                protected ArrayList<WeatherData> doInBackground(Void... params) {
                    ArrayList<WeatherData> stations = new ArrayList<WeatherData>();
                    try
                    {
                        URL url = new URL(stationLink.mUrlString);
                        stations.addAll(StationListReader.GetWeatherStationsFromURL(url, stationLink.mState));
                    }
                    catch (MalformedURLException urlEx)
                    {
                        Log.e(WeatherDataCache.TAG, "Couldn't create URL " + urlEx.toString());
                        stations = null;
                    } catch( Exception e)
                    {
                        Log.e(WeatherDataCache.TAG, "Error getting station list: " + e.toString());
                        stations = null;
                    }
                    return stations;
                }

                @Override
                protected void onPostExecute(ArrayList<WeatherData> weatherStations) {
                    if(weatherStations == null)
                    {
                        weatherStations = new ArrayList<WeatherData>();
                    }
                    mInternalStationCache.AddStationsForState(weatherStations, stationLink.mState);
                    
                    
                    if(mInternalStationCache.StationsForAllStatesAdded())
                    {
                        mInternalStationCacheTime = new Date();
                        NotifyOfStationCacheFilled();
                    }
                }
            }.execute();
        }
    }

    private void NotifyOfStationCacheFilled() {
        for(NotifyWhenStationCacheFilled notify : mNotifyUs)
        {
            notify.OnCacheFilled(GetInternalStationCache());
        }
        mNotifyUs.clear();
    }
}
