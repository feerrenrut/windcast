package com.feer.windcast.dataAccess;

import android.util.Log;

import java.util.ArrayList;

/**
 */
public class WeatherDataCache
{
    protected static final String TAG = "WeatherDataCache";

    public WeatherDataCache() { }

    // if set loading is in progress.
    private StationListCacheLoader.InternalCacheLoader mCacheLoader;
    private boolean HasCacheLoadStarted() {
        return null != mCacheLoader;
    }

    void SetCacheLoader(StationListCacheLoader.InternalCacheLoader internalCacheLoader) {
        Log.v(TAG, "setting cache loader");
        if(!HasCacheLoadStarted() && (IsStationCacheStale() || !IsStationCacheFilled())) {
            Log.v(TAG, "starting cache loader");
            mCacheLoader = internalCacheLoader;
            mCacheLoader.TriggerFillStationCache(new StationListCacheLoader.CacheLoaderInterface() {
                @Override
                public void onComplete(LoadedWeatherStationCache cache) {
                    mInternalStationCache = cache;
                    mCacheLoader = null;
                    NotifyOfStationCacheFilled();
                }
            });
        } else {
            Log.v(TAG, "Not starting loader: IsStationCacheStale: " + IsStationCacheStale() + " HasCacheLoadStarted: " + HasCacheLoadStarted());
        }
    }

    public interface NotifyWhenStationCacheFilled {
        /* called when the cache updates.
         */
        void OnCacheFilled(LoadedWeatherStationCache fullCache);

        /* If true the object will receive the next update. Once false updates will no longer continue
         * and the object will need to re-register.
         */
        boolean ShouldContinueGettingNotifications();
    }
    final private ArrayList<NotifyWhenStationCacheFilled> mNotifyUs = new ArrayList<>();

    LoadedWeatherStationCache mInternalStationCache = null;
    private boolean IsStationCacheFilled() {
        return mInternalStationCache != null && mInternalStationCache.AreAllStatesFilled();
    }
    void OnStationCacheFilled(NotifyWhenStationCacheFilled notify) {
        if (IsStationCacheFilled() ) {
            notify.OnCacheFilled(mInternalStationCache);
            if (!notify.ShouldContinueGettingNotifications()) {
                return;
            }
        }
        mNotifyUs.add(notify);
    }

    private void NotifyOfStationCacheFilled() {
        ArrayList<NotifyWhenStationCacheFilled> notifyAgain = new ArrayList<>();
        for( NotifyWhenStationCacheFilled notify : mNotifyUs ) {
            notify.OnCacheFilled( mInternalStationCache );
            if( notify.ShouldContinueGettingNotifications() ) {
                notifyAgain.add( notify );
            }
        }
        mNotifyUs.addAll(notifyAgain);
    }

    private boolean IsStationCacheStale() {
        return mInternalStationCache != null && mInternalStationCache.IsStale();
    }
}
