package com.feer.windcast;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.feer.windcast.dataAccess.LoadedWeatherStationCache;
import com.feer.windcast.dataAccess.WeatherDataCache;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class WeatherStationsService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPDATE_WEATHER_STATION_INFO
            = "com.feer.windcast.action.UpdateWeatherStationInformation";
    
    private WeatherDataCache mCache;
    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
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
                handleActionUpdateWetherStations();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateWetherStations() {
        
        if(mCache == null)
        {
            mCache = WeatherDataCache.GetInstance();
        }
        
        mCache.OnStationCacheFilled(new WeatherDataCache.NotifyWhenStationCacheFilled() {
            @Override
            public void OnCacheFilled(LoadedWeatherStationCache fullCache) {
                return;
            }
        });
    }
}
