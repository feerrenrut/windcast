package com.feer.windcast.dataAccess;

import android.util.Log;

import com.feer.windcast.ObservationReader;
import com.feer.windcast.WeatherData;
import com.feer.windcast.WeatherStation;

import java.util.ArrayList;
import java.util.Date;

/**
 */
public class WeatherDataCache
{
    protected static final String TAG = "WeatherDataCache";

    private WeatherDataCache() { }
    private static WeatherDataCache sInstance = null;
    public static WeatherDataCache GetInstance()  {
        if(sInstance == null) {
            SetInstance(new WeatherDataCache());
        }
        return sInstance;
    }
    // Just for automated tests.
    public static void SetInstance(WeatherDataCache instance) {
        sInstance = instance;
    }

    public FavouriteStationCache CreateNewFavouriteStationAccessor() {
        return new FavouriteStationCache();
    }

    /* Performs http request, should be called using AsyncTask
    * Not static, so that it can be mocked for tests.
     */
    public WeatherData GetWeatherDataFor(WeatherStation station) {
        WeatherData wd = null;
        try {
            ObservationReader obs = new ObservationReader(station);
            wd = obs.GetWeatherData();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return wd;
    }

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
                public void onComplete(LoadedWeatherStationCache cache, Date timeComplete) {
                    mInternalStationCache = cache;
                    mInternalStationCacheTime = timeComplete;
                    mCacheLoader = null;
                    NotifyOfStationCacheFilled();
                }
            });
        } else {
            Log.v(TAG, "Not starting loader: IsStationCacheStale: " + IsStationCacheStale() + " HasCacheLoadStarted: " + HasCacheLoadStarted());
        }
    }

    public interface NotifyWhenStationCacheFilled {
        /*
        * called when the cache updates.
         */
        void OnCacheFilled(LoadedWeatherStationCache fullCache);

        /*
        * If true the object will receive the next update. Once false updates will no longer continue
        * and the object will need to re-register.
         */
        boolean ShouldContinueGettingNotifications();
    }
    final private ArrayList<NotifyWhenStationCacheFilled> mNotifyUs = new ArrayList<>();

    LoadedWeatherStationCache mInternalStationCache = null;
    private boolean IsStationCacheFilled() {
        return mInternalStationCache != null && mInternalStationCache.AreAllStatesFilled();
    }
    public void OnStationCacheFilled(NotifyWhenStationCacheFilled notify) {
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

    Date mInternalStationCacheTime = null;
    private boolean IsStationCacheStale() {
        final long cacheTimeout = 15L * 60 * 1000; // 15 min
        final long currentTime = new Date().getTime();
        return mInternalStationCacheTime != null &&
                cacheTimeout <= // elapsed time
                        ( currentTime - mInternalStationCacheTime.getTime() );
    }
}
