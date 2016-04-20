package com.feer.windcast.dataAccess;

import android.os.AsyncTask;
import android.util.Log;

import com.feer.windcast.StationListReader;
import com.feer.windcast.WeatherData;
import com.feer.windcast.dataAccess.StationListCacheLoader.CacheLoaderInterface;
import com.feer.windcast.dataAccess.StationListCacheLoader.InternalCacheLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Reef on 16/03/2016.
 */
public class BomWeatherStationLoader implements InternalCacheLoader {

    CacheLoaderInterface mCallback;
    @Override
    public void TriggerFillStationCache(CacheLoaderInterface callback) {
        mCallback = callback;
        LoadCacheDirectlyFromBOM();
    }

    private void LoadCacheDirectlyFromBOM() {
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
                    iCache.AddStationsForState (
                            weatherStations,
                            stationLink.mState,
                            new Date() );

                    if (iCache.StationsForAllStatesAdded()) {
                        double difference = (System.nanoTime() - StartTime) / 1E9;
                        Log.i("StationList", "Filled station list from BOM in (seoonds): " + difference);
                        mCallback.onComplete(iCache);
                        mCallback = null;
                    }
                }
            }.execute();
        }
    }


}
