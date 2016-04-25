package com.feer.windcast;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.feer.windcast.dataAccess.LoadedWeatherStationCache;
import com.feer.windcast.dataAccess.StationListCacheLoader;
import com.feer.windcast.dataAccess.WeatherDataCache;
import com.feer.windcast.dataAccess.dependencyProviders.StationListCacheProvider;

import java.util.UUID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class WeatherStationsService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPDATE_WEATHER_STATION_INFO
            = "com.feer.windcast.action.UpdateWeatherStationInformation";
    
    private WeatherDataCache mCache;
    /**
     * Starts this service to Update the weather station cache
     * @see IntentService
     */
    public static void startAction_UpdateWeatherStations(Context context) {
        Intent intent = new Intent(context, WeatherStationsService.class);
        intent.setAction(ACTION_UPDATE_WEATHER_STATION_INFO);
        context.startService(intent);
    }

    public WeatherStationsService() {
        super("WeatherStationsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_WEATHER_STATION_INFO.equals(action)) {
                handleActionUpdateWeatherStations();
            }
        }
    }

    private void handleActionUpdateWeatherStations() {
        
        if(mCache == null)
        {
            mCache = StationListCacheProvider.GetWeatherDataCacheInstance();
        }

        // get the cache load started as soon as possible. We dont need the result here, but it will
        // be stored in the cache for later components.
        UUID userId = StationListCacheLoader.GetUserID(this.getApplicationContext());
        StationListCacheLoader loader = StationListCacheProvider.CreateStationListCacheLoader();
        loader.StartStationListCacheLoad(
                userId,
                mCache,
                new WeatherDataCache.NotifyWhenStationCacheFilled() {
            @Override
            public void OnCacheFilled(LoadedWeatherStationCache fullCache) {
            }

            @Override
            public boolean ShouldContinueGettingNotifications() {
                return false;
            }
        });
    }
}
