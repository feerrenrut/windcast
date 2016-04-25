package com.feer.windcast.dataAccess;

import android.content.Context;
import android.content.SharedPreferences;

import com.feer.windcast.dataAccess.dependencyProviders.StationListCacheProvider;

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

    public void StartStationListCacheLoad(UUID userId,
                                          WeatherDataCache cache,
                                          WeatherDataCache.NotifyWhenStationCacheFilled notify) {
        cache.OnStationCacheFilled(notify);
        cache.SetCacheLoader(StationListCacheProvider.CreateInternalCacheLoader(userId));
    }

    public interface CacheLoaderInterface {
        void onComplete(LoadedWeatherStationCache cache);
    }

    public interface InternalCacheLoader {
        void TriggerFillStationCache(CacheLoaderInterface callback);
    }

}

