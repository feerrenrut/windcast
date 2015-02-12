package com.feer.windcast.testUtils;

import android.content.SharedPreferences;

import com.feer.windcast.WeatherStation;
import com.feer.windcast.WindCastNavigationDrawer;
import com.feer.windcast.dataAccess.FavouriteStationCache;
import com.feer.windcast.dataAccess.LoadedWeatherCache;
import com.feer.windcast.dataAccess.WeatherDataCache;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Reef on 11/02/2015.
 */
public class WindCastMocks {


    public FakeWeatherStationData mFakeStations;
    public LoadedWeatherCache mInternalCache;
    public WeatherDataCache mDataCache;
    public SharedPreferences mSettings;
    
    public WindCastMocks(SharedPreferences settings) throws Exception {
        mFakeStations = new FakeWeatherStationData("test Station");

        // set up weather data cache before starting the activity.
        mInternalCache = mock(LoadedWeatherCache.class);
        mDataCache = mock(WeatherDataCache.class);
        when(mDataCache.CreateNewFavouriteStationAccessor()).thenCallRealMethod();
        Mockito.doAnswer(createCallCacheFilledAnswer(mInternalCache))
                .when(mDataCache).OnCacheFilled(isA(WeatherDataCache.NotifyWhenCacheFilled.class));
        WeatherDataCache.SetWeatherDataCache(mDataCache);
        
        
        mSettings = settings;
        ClearPreferences();
        AddNavigationDrawerAlreadyOpenedPreference();
    }
    
    public void SetInternalCache_ReturnAFavStation(FavouriteStationCache mockFavCache)
    {
        final int STATION_INDEX = 4;
        WeatherStation oldFav = mFakeStations.GetAllStations().get(STATION_INDEX);
        oldFav.IsFavourite = true;
        ArrayList<String> favUrls = new ArrayList<String>();
        favUrls.add(oldFav.GetURL().toString());
        when(mockFavCache.GetFavouriteURLs()).thenReturn(favUrls);
        
        when(mDataCache.CreateNewFavouriteStationAccessor())
                .thenReturn(mockFavCache);
    }
    
    public void SetInternalCache_ReturnEmptyStationLists()
    {
        when(mInternalCache.GetWeatherStationsFromAllStates()).thenReturn(mFakeStations.EmptyStationList());
        when(mInternalCache.GetWeatherStationsFrom(anyString())).thenReturn(mFakeStations.EmptyStationList());
    }
   
    private Answer<Void> createCallCacheFilledAnswer(final LoadedWeatherCache cache)
    {
        return new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                ((WeatherDataCache.NotifyWhenCacheFilled)invocationOnMock.getArguments()[0])
                        .OnCacheFilled(cache);
                return null;
            }
        };
    }
    
    public void ClearPreferences()
    {
        SharedPreferences.Editor editor =  mSettings.edit();
        editor.clear();
        editor.commit();
    }

    public void AddNavigationDrawerAlreadyOpenedPreference()
    {

        SharedPreferences.Editor editor =  mSettings.edit();
        editor.putBoolean(WindCastNavigationDrawer.PREFS_NAVIGATION_DRAWER_OPENED, true);
        editor.commit();
    }

    public boolean GetPreference_HasDrawerBeenOpened()
    {
        return mSettings.getBoolean(WindCastNavigationDrawer.PREFS_NAVIGATION_DRAWER_OPENED, false);
    }
    
}
