package com.feer.windcast.dataAccess;

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

    public WindCastApiWeatherStationLoader(UUID userId) {
        this.mUserId = userId;
        this.mUserIdBase64String = Base64.encodeToString(
                this.mUserId.toString().getBytes(),
                Base64.NO_WRAP);
    }

    CacheLoaderInterface mCallback;
    public void TriggerFillStationCache(CacheLoaderInterface callback) {
        mCallback = callback;
        LoadCacheUsingWindcastAPI();
    }

    private void LoadCacheUsingWindcastAPI() {
        final Windcastdata windcastdata = CreateWindcastBackendApi.create();
        new AsyncTask<Void, Void, Void>() {
            long StartTime;
            GAE_StationCache cache;

            @Override
            protected void onPreExecute() {
                StartTime = System.nanoTime();
                cache = new GAE_StationCache();
            }

            @Override
            protected Void doInBackground(Void... params) {
                ArrayList<WeatherData> weatherStations = getWeatherDataFromBackend(windcastdata);
                if (weatherStations == null) {
                    weatherStations = new ArrayList<WeatherData>();
                }
                cache.SetCachedStations(weatherStations, new Date());
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                double difference = (System.nanoTime() - StartTime) / 1E9;
                Log.i("StationList", "Filled station list from GAE in (seoonds): " + difference);
                mCallback.onComplete(cache);
                mCallback = null;
            }
        }.execute();
    }

    @Nullable
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
        if (stationsInUpdateCol == null || stationsInUpdateCol.isEmpty()) return null;

        Log.i("WeatherDataCache", "Got result");
        ArrayList<WeatherData> weatherData = new ArrayList<>(2000);
        for (StationsInUpdate stateStations : stationsInUpdateCol.getItems()) {

            List<StationData> stations = stateStations.getStations();
            Log.i("WeatherDataCache", "Number of stations from backend: " + stations.size());

            for (StationData stationData : stations) {
                LatestReading readingForStation = stationData.getLatestReading();

                WeatherData d = new WeatherData();
                d.Source = "GAE-backend";
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
