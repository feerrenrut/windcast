package com.feer.windcast.dataAccess;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.feer.helperLib.Base64;
import com.feer.helperLib.TokenGen;
import com.feer.windcast.GAE_ObservationReading;
import com.feer.windcast.GAE_WeatherStation;
import com.feer.windcast.StationListReader;
import com.feer.windcast.WeatherData;
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
 * Created by Reef on 6/03/2016.
 */
public class StationListCacheLoader {

    public static UUID GetUserID(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                "windcast_app_prefs", Context.MODE_PRIVATE);
        UUID userId;
        String userIdStr = sharedPreferences.getString(
                "USER_ID", null );
        if( userIdStr == null) {
            userId = UUID.randomUUID();
            userIdStr = userId.toString();
            sharedPreferences.edit().putString("USER_ID", userIdStr).apply();
        } else {
            userId = UUID.fromString(userIdStr);
        }
        return userId;
    }

    public static void StartStationListCacheLoad(UUID userId,
                                                 WeatherDataCache cache,
                                                 WeatherDataCache.NotifyWhenStationCacheFilled notify) {
        cache.OnStationCacheFilled(notify);
        cache.SetCacheLoader(new InternalCacheLoader(userId));
    }

    protected interface CacheLoaderInterface {
        void onComplete(LoadedWeatherStationCache cache, Date timeComplete);
    }

    protected static class InternalCacheLoader {
        final UUID mUserId;
        final private String mUserIdBase64String;

        public InternalCacheLoader(UUID userId) {
            this.mUserId = userId;
            this.mUserIdBase64String = Base64.encodeToString(
                    this.mUserId.toString().getBytes(),
                    Base64.NO_WRAP );
        }

        CacheLoaderInterface mCallback;
        public void TriggerFillStationCache(CacheLoaderInterface callback) {
            mCallback = callback;
            LoadCacheUsingWindcastAPI();
        }

        private void LoadCacheUsingWindcastAPI() {
            final long StartTime = System.nanoTime();
            final GAE_StationCache cache = new GAE_StationCache();
            final Date updatedAt = new Date();

            final Windcastdata windcastdata = CreateWindcastBackendApi.create();
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    ArrayList<WeatherData> weatherStations = getWeatherDataFromBackend(windcastdata);
                    if (weatherStations == null) {
                        weatherStations = new ArrayList<WeatherData>();
                    }
                    cache.SetCachedStations(weatherStations);
                    updatedAt.setTime(new Date().getTime());
                    return null;
                }

                @Override
                protected void onPostExecute(Void unused) {
                    double difference = (System.nanoTime() - StartTime) / 1E9;
                    Log.i("StationList", "Filled station list from GAE in (seoonds): " + difference);
                    mCallback.onComplete(cache, updatedAt);
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

        private void LoadCacheDirectlyFromBOM() {
            final Date internalStationCacheTime = new Date();
            final InternalStationCache iCache = new InternalStationCache();

            final long StartTime = System.nanoTime();

            for (final InternalStationCache.AllStationsURLForState stationLink : InternalStationCache.mAllStationsInState_UrlList) {
                new AsyncTask<Void, Void, ArrayList<WeatherData>>() {
                    @Override
                    protected ArrayList<WeatherData> doInBackground(Void... params) {
                        ArrayList<WeatherData> stations = new ArrayList<WeatherData>();
                        try {
                            URL url = new URL(stationLink.mUrlString);
                            stations.addAll(StationListReader.GetWeatherStationsFromURL(url, stationLink.mState));
                        } catch (MalformedURLException urlEx) {
                            Log.e(WeatherDataCache.TAG, "Couldn't create URL " + urlEx.toString());
                            stations = null;
                        } catch (Exception e) {
                            Log.e(WeatherDataCache.TAG, "Error getting station list: " + e.toString());
                            stations = null;
                        }
                        return stations;
                    }

                    @Override
                    protected void onPostExecute(ArrayList<WeatherData> weatherStations) {
                        if (weatherStations == null) {
                            weatherStations = new ArrayList<>();
                        }
                        iCache.AddStationsForState(weatherStations, stationLink.mState);

                        if (iCache.StationsForAllStatesAdded()) {
                            internalStationCacheTime.setTime(new Date().getTime());

                            double difference = (System.nanoTime() - StartTime) / 1E9;
                            Log.i("StationList", "Filled station list from BOM in (seoonds): " + difference);
                            mCallback.onComplete(iCache, internalStationCacheTime);
                            mCallback = null;
                        }
                    }
                }.execute();
            }
        }
    }
}

