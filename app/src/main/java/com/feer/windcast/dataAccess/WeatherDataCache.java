package com.feer.windcast.dataAccess;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.feer.windcast.ObservationReader;
import com.feer.windcast.ObservationReading;
import com.feer.windcast.StationListReader;
import com.feer.windcast.WeatherData;
import com.feer.windcast.WeatherStation;
import com.feer.windcast.WindObservation;
import com.feer.windcast.backend.windcastdata.Windcastdata;
import com.feer.windcast.backend.windcastdata.model.LatestReading;
import com.feer.windcast.backend.windcastdata.model.LatestReadingCollection;
import com.feer.windcast.backend.windcastdata.model.StationData;
import com.feer.windcast.backend.windcastdata.model.StationDataCollection;
import com.feer.windcast.dataAccess.backend.CreateWindcastBackendApi;
import com.feer.windcast.dataAccess.backend.GAE_StationCache;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        new AsyncTask<Void, Void, ArrayList<WeatherData>>() {
            @Override
            protected ArrayList<WeatherData> doInBackground(Void... params) {
                return getWeatherDataFromBackend();
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
    private ArrayList<WeatherData> getWeatherDataFromBackend() {
        final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.ordinalDateTime().withOffsetParsed();
        final float KMH_TO_KNOT = 0.539957f;

        Windcastdata windcastdata = CreateWindcastBackendApi.create();
        StationDataCollection stations = null;
        LatestReadingCollection readings = null;
        try {
            stations = windcastdata.getStationList().execute();
            readings = windcastdata.getLatestObservation().execute();
        } catch (IOException e) {
            Log.e("WeatherDataCache", "Couldnt load stations and latest readings: " + e.toString());
        }
        if(stations == null ) return null;
        List<LatestReading> readingsList = readings.getItems();
        ArrayList<WeatherData> weatherData = new ArrayList<>();
        for (StationData stationData: stations.getItems()) {
            try {
                LatestReading readingForStation = null;
                for (LatestReading reading : readingsList) {
                    if(reading.getStationID().equals(stationData.getStationID())) {
                        readingForStation = reading;
                        readingsList.remove(reading);
                        break;
                    }
                }
                WeatherData d = new WeatherData();
                d.ObservationData = new ArrayList<>(1);
                if(readingForStation != null) {
                    ObservationReading r = new ObservationReading();
                    r.Wind_Observation = new WindObservation();

                    if(readingForStation.getLocalTime() != null) {
                        r.LocalTime = DATE_TIME_FORMATTER
                                .parseDateTime(readingForStation.getLocalTime())
                                .toDate();
                    }
                    if(readingForStation.getWindSpeedKMH() != null) {
                        r.Wind_Observation.WindSpeed_KMH = readingForStation.getWindSpeedKMH();
                        r.Wind_Observation.WindSpeed_KN = (int) (KMH_TO_KNOT * readingForStation.getWindSpeedKMH());
                    }
                    if(readingForStation.getCardinalWindDirection() != null) {
                        r.Wind_Observation.CardinalWindDirection = readingForStation.getCardinalWindDirection();
                        r.Wind_Observation.WindBearing = ObservationReader.ConvertCardinalCharsToBearing(r.Wind_Observation.CardinalWindDirection);
                    }
                    r.Wind_Observation.WindGustSpeed_KMH = 0;
                    d.ObservationData.add(r);
                }

                d.Station = new WeatherStation.WeatherStationBuilder()
                        .WithName(stationData.getDisplayName())
                        .WithState(WeatherStation.States.valueOf(stationData.getState()))
                        .WithURL(new URL(stationData.getDataUrl()))
                        .Build();
                weatherData.add(d);
            } catch (MalformedURLException e) {
                Log.e("WeatherDataCache", "Loading weatherStation: " + stationData.getDisplayName()
                        + " exception: " + e.toString());
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
