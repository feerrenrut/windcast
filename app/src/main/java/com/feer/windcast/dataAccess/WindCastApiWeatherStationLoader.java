package com.feer.windcast.dataAccess;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.feer.helperLib.Base64;
import com.feer.helperLib.TokenGen;
import com.feer.windcast.GAE_ObservationReading;
import com.feer.windcast.GAE_WeatherStation;
import com.feer.windcast.WeatherData;
import com.feer.windcast.backend.windcastdata.Windcastdata;
import com.feer.windcast.backend.windcastdata.model.LatestReading;
import com.feer.windcast.backend.windcastdata.model.StationData;
import com.feer.windcast.backend.windcastdata.model.StationsInUpdate;
import com.feer.windcast.backend.windcastdata.model.StationsInUpdateCollection;
import com.feer.windcast.dataAccess.StationListCacheLoader.CacheLoaderInterface;
import com.feer.windcast.dataAccess.StationListCacheLoader.InternalCacheLoader;
import com.feer.windcast.dataAccess.backend.CreateWindcastBackendApi;
import com.feer.windcast.dataAccess.backend.GAE_StationCache;
import com.feer.windcast.dataAccess.dependencyProviders.StationListFileLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Reef on 16/03/2016.
 */
public class WindCastApiWeatherStationLoader implements InternalCacheLoader{
    final UUID mUserId;
    final private String mUserIdBase64String;
    final private Context mContext;
    public WindCastApiWeatherStationLoader(UUID userId, Context context) {
        this.mUserId = userId;
        this.mContext = context;
        this.mUserIdBase64String = Base64.encodeToString(
                this.mUserId.toString().getBytes(),
                Base64.NO_WRAP);
    }

    CacheLoaderInterface mCallback;
    public void TriggerFillStationCache(CacheLoaderInterface callback) {
        mCallback = callback;
        LoadCacheUsingWindcastAPI();
    }

    private ArrayList<WeatherData> LoadFromFile() {
        final StationListFileLoader fileLoader = new StationListFileLoader(mContext);
        return CreateWeatherDataFromStationCollection(
                                fileLoader.readFile(), "GAE_fileCache");
    }

    private void SaveStationsToFile(final StationsInUpdateCollection stationsInUpdateCol) {
        final StationListFileLoader fileLoader = new StationListFileLoader(mContext);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... unused) {
                fileLoader.writeToFile(stationsInUpdateCol);
                return null;
            }
        }.execute();
    }

    private void LoadCacheUsingWindcastAPI() {
        final Windcastdata windcastdata = CreateWindcastBackendApi.create();
        new AsyncTask<Void, Void, GAE_StationCache>() {
            long StartTime;

            @Override
            protected void onPreExecute() {
                StartTime = System.nanoTime();
            }

            @Override
            protected GAE_StationCache doInBackground(Void... params) {
                ArrayList<WeatherData> weatherStations = getWeatherDataFromBackend(windcastdata);
                if (weatherStations.isEmpty()) {
                    Log.i("WeatherDataCache", "Got no stations, loading from file-cache instead");
                    weatherStations = LoadFromFile();
                }
                GAE_StationCache cache = new GAE_StationCache();
                cache.SetCachedStations(weatherStations, new Date());
                return cache;
            }

            @Override
            protected void onPostExecute(GAE_StationCache cache) {
                double difference = (System.nanoTime() - StartTime) / 1E9;
                Log.i("StationList", "Filled station list from GAE in (seconds): " + difference);

                if(cache != null) {
                    mCallback.onComplete(cache);
                    mCallback = null;
                } else {

                }
            }
        }.execute();
    }

    private ArrayList<WeatherData> getWeatherDataFromBackend(Windcastdata windcastdata) {

        Log.i("WeatherDataCache", "Creating API sending request");
        StationsInUpdateCollection stationsInUpdateCol = null;

        Date date = new Date();
        String token = TokenGen.GetToken("getStationList", date, mUserId);
        try {
            stationsInUpdateCol = windcastdata.getStationList(
                    mUserIdBase64String,
                    date.getTime(),
                    token).execute();

        } catch (IOException e) {
            Log.e("WeatherDataCache", "Couldnt load stations and latest readings: " + e.toString());
        }
        ArrayList<WeatherData> weatherData = CreateWeatherDataFromStationCollection(
                stationsInUpdateCol, "GAE-backend");
        if (weatherData != null) {
            SaveStationsToFile(stationsInUpdateCol);
        }
        else {
            weatherData = new ArrayList<>();
        }
        return weatherData;
    }

    @Nullable
    private ArrayList<WeatherData> CreateWeatherDataFromStationCollection(StationsInUpdateCollection stationsInUpdateCol, final String source) {
        if (stationsInUpdateCol == null || stationsInUpdateCol.isEmpty()) return null;

        Log.i("WeatherDataCache", "Got result");
        ArrayList<WeatherData> weatherData = new ArrayList<>(stationsInUpdateCol.size());
        for (StationsInUpdate stateStations : stationsInUpdateCol.getItems()) {

            List<StationData> stations = stateStations.getStations();
            Log.i("WeatherDataCache", "Number of stations from backend (" + stateStations.getState() + "): " + stations.size());

            for (StationData stationData : stations) {
                LatestReading readingForStation = stationData.getLatestReading();

                WeatherData d = new WeatherData();
                d.Source = source;
                if (readingForStation != null) {
                    GAE_ObservationReading latest = new GAE_ObservationReading(readingForStation);
                    d.setLatestReading(latest);
                }

                d.Station = new GAE_WeatherStation(stationData);
                weatherData.add(d);
            }
        }
        return weatherData;
    }

}
