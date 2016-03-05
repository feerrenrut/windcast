package com.feer.windcast.dataAccess;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.feer.helperLib.Base64;
import com.feer.helperLib.TokenGen;
import com.feer.windcast.GAE_ObservationReading;
import com.feer.windcast.GAE_WeatherStation;
import com.feer.windcast.ObservationReader;
import com.feer.windcast.StationListReader;
import com.feer.windcast.WeatherData;
import com.feer.windcast.WeatherStation;
import com.feer.windcast.backend.windcastdata.Windcastdata;
import com.feer.windcast.backend.windcastdata.model.LatestReading;
import com.feer.windcast.backend.windcastdata.model.StationData;
import com.feer.windcast.backend.windcastdata.model.StationsInUpdate;
import com.feer.windcast.backend.windcastdata.model.StationsInUpdateCollection;
import com.feer.windcast.dataAccess.backend.CreateWindcastBackendApi;
import com.feer.windcast.dataAccess.backend.GAE_StationCache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
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
    LoadedWeatherStationCache mInternalStationCache = null;

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
            notify.OnCacheFilled(mInternalStationCache);
            mInternalStationCache = null;
            mInternalStationCacheTime = null;
        }
        
        if(IsStationCacheFilled())
        {
            notify.OnCacheFilled(mInternalStationCache);
        }
        else
        {
            mNotifyUs.add(notify);
            if(mInternalStationCache == null)
            {
                TriggerFillStationCache();
            }
        }
    }

    private boolean IsStationCacheFilled() {return mInternalStationCache != null && mInternalStationCache.AreAllStatesFilled();}

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
        LoadCacheUsingWindcastAPI();
    }

    private void LoadCacheUsingWindcastAPI() {
        mInternalStationCacheTime = null;

        final GAE_StationCache cache = new GAE_StationCache();
        mInternalStationCache = cache;

        final long StartTime = System.nanoTime();

        final Windcastdata windcastdata = CreateWindcastBackendApi.create();
        new AsyncTask<Void, Void, ArrayList<WeatherData>>() {
            @Override
            protected ArrayList<WeatherData> doInBackground(Void... params) {
                return getWeatherDataFromBackend(windcastdata);
            }

            @Override
            protected void onPostExecute(ArrayList<WeatherData> weatherStations) {
                if(weatherStations == null)
                {
                    weatherStations = new ArrayList<WeatherData>();
                }
                cache.SetCachedStations(weatherStations);

                mInternalStationCacheTime = new Date();

                double difference = (System.nanoTime() - StartTime) / 1E9;
                Log.i("StationList", "Filled station list from GAE in (seoonds): " + difference);
                NotifyOfStationCacheFilled();

            }
        }.execute();
    }

    @Nullable
    private ArrayList<WeatherData> getWeatherDataFromBackend(Windcastdata windcastdata) {

        Log.i("WeatherDataCache", "Creating API sending request");
        StationsInUpdateCollection stationsInUpdateCol = null;

        Date date = new Date(2005, 03, 28);
        UUID id = new UUID(0, 1);
        String token = TokenGen.GetToken("TEST Message", date , id);
        try {
            stationsInUpdateCol = windcastdata.getStationList (
                    Base64.encodeToString(id.toString().getBytes(), Base64.NO_WRAP),
                    date.getTime(),
                    token ).execute();

        } catch (IOException e) {
            Log.e("WeatherDataCache", "Couldnt load stations and latest readings: " + e.toString());
        }
        if(stationsInUpdateCol == null || stationsInUpdateCol.isEmpty()) return null;

        Log.i("WeatherDataCache", "Got result");
        ArrayList<WeatherData> weatherData = new ArrayList<>(2000);
        for (StationsInUpdate stateStations : stationsInUpdateCol.getItems()) {

            List<StationData> stations = stateStations.getStations();
            Log.i("WeatherDataCache", "Number of stations from backend: " + stations.size());

            for (StationData stationData: stations) {
                LatestReading readingForStation = stationData.getLatestReading();

                WeatherData d = new WeatherData();
                d.Source = "GAE-backend";
                if(readingForStation != null) {
                    GAE_ObservationReading latest = new GAE_ObservationReading(readingForStation);
                    d.setLatestReading(latest);
                }

                d.Station = new GAE_WeatherStation(stationData);
                weatherData.add(d);
            }

        }
        return weatherData;
    }

    private void LoadCacheDirectlyFromBOM() {
        mInternalStationCacheTime = null;
        final InternalStationCache iCache = new InternalStationCache();
        mInternalStationCache = iCache;

        final long StartTime = System.nanoTime();

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
                    iCache.AddStationsForState(weatherStations, stationLink.mState);


                    if(iCache.StationsForAllStatesAdded())
                    {
                        mInternalStationCacheTime = new Date();

                        double difference = (System.nanoTime() - StartTime) / 1E9;
                        Log.i("StationList", "Filled station list from BOM in (seoonds): " + difference);
                        NotifyOfStationCacheFilled();
                    }
                }
            }.execute();
        }
    }

    private void NotifyOfStationCacheFilled() {
        for(NotifyWhenStationCacheFilled notify : mNotifyUs)
        {
            notify.OnCacheFilled(mInternalStationCache);
        }
        mNotifyUs.clear();
    }
}
