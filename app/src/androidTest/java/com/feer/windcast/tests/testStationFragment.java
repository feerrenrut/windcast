package com.feer.windcast.tests;

import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.WeatherData;
import com.feer.windcast.dataAccess.WeatherDataCache;
import com.feer.windcast.testUtils.WindCastMocks;

import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;

import static com.feer.windcast.testUtils.AdapterMatchers.adapterHasCount;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * http://youtu.be/uHoB0KzQGRg?t=54s
 * see https://code.google.com/p/android-test-kit/wiki/EspressoStartGuide
 */
public class testStationFragment extends ActivityInstrumentationTestCase2<MainActivity>
{
    public testStationFragment()
    {
        super(MainActivity.class);
    }

    private WindCastMocks Mocks;
    private final int drawerID = R.id.drawer_layout;

    // Activity is not created until get activity is called
    private void launchActivity()
    {
        getActivity();
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        Mocks = new WindCastMocks(
                PreferenceManager.getDefaultSharedPreferences(
                        this.getInstrumentation().getTargetContext()));
    }

    // tests that the initial loading of the cache is shown correctly
    public void test_callingOnce_NotifyStationsLoaded()
    {
        final int EXPECTED_NUM_STATIONS = 11;

        Mocks.Fakes.HasStations(EXPECTED_NUM_STATIONS).HasFavourites(0);

        when(Mocks.DataCache.CreateNewFavouriteStationAccessor())
                .thenReturn(Mocks.FavouritesCache);
        when(Mocks.FavouritesCache.GetFavouriteURLs())
                .thenReturn(Mocks.Fakes.FavURLs());
        when(Mocks.LoadedCache.GetWeatherStationsFromAllStates())
                .thenReturn(Mocks.Fakes.Stations());

        Mocks.VerifyNoUnstubbedCallsOnMocks();
        launchActivity();

        onView(withId(android.R.id.list))
                .check(matches(
                        adapterHasCount(equalTo(EXPECTED_NUM_STATIONS))));
        
        verify(Mocks.DataCache, times(1)).OnStationCacheFilled(Matchers.<WeatherDataCache.NotifyWhenStationCacheFilled>anyObject());
        verify(Mocks.LoadedCache, times(1)).GetWeatherStationsFromAllStates();
    }


    // test to check that when the cache is out of date and is reloaded in the background the ui is
    // also updated once the cache comes back and calls to notify a second time.
    public void test_callingTwice_NotifyStationsLoaded()
    {
        final int EXPECTED_NUM_STATIONS_FIRST = 11;        

        Mocks.Fakes.HasStations(EXPECTED_NUM_STATIONS_FIRST).HasFavourites(0);

        when(Mocks.DataCache.CreateNewFavouriteStationAccessor())
                .thenReturn(Mocks.FavouritesCache);
        when(Mocks.FavouritesCache.GetFavouriteURLs())
                .thenReturn(Mocks.Fakes.FavURLs());
        when(Mocks.LoadedCache.GetWeatherStationsFromAllStates())
                .thenReturn(Mocks.Fakes.Stations());

        final WeatherDataCache.NotifyWhenStationCacheFilled notify;

        WindCastMocks.RememberNotifyOnStationCacheFilled answer = Mocks.new RememberNotifyOnStationCacheFilled(Mocks.LoadedCache);

        // override the notify of cache so that we can call it a second time;
        Mockito.doAnswer(answer) // set call back for waiting for
                .when(Mocks.DataCache).OnStationCacheFilled(  // loaded data
                isA(WeatherDataCache.NotifyWhenStationCacheFilled.class));

        Mocks.VerifyNoUnstubbedCallsOnMocks();
        launchActivity();

        onView(withId(android.R.id.list))
                .check(matches(
                        adapterHasCount(equalTo(EXPECTED_NUM_STATIONS_FIRST))));

        verify(Mocks.DataCache, times(1)).OnStationCacheFilled(Matchers.<WeatherDataCache.NotifyWhenStationCacheFilled>anyObject());
        verify(Mocks.LoadedCache, times(1)).GetWeatherStationsFromAllStates();

        // now call again with "updated" data

        final int EXPECTED_NUM_STATIONS_SECOND = 1;
        ArrayList<WeatherData> oneStation = new ArrayList<WeatherData>(
                Mocks.Fakes.Stations().subList(0, EXPECTED_NUM_STATIONS_SECOND));

        when(Mocks.LoadedCache.GetWeatherStationsFromAllStates())
                .thenReturn(oneStation);
        
        answer.mNotify.OnCacheFilled(Mocks.LoadedCache);

        onView(withId(android.R.id.list))
                .check(matches(
                        adapterHasCount(equalTo(EXPECTED_NUM_STATIONS_SECOND))));
        
        verify(Mocks.DataCache, times(1)).OnStationCacheFilled(Matchers.<WeatherDataCache.NotifyWhenStationCacheFilled>anyObject());
        verify(Mocks.LoadedCache, times(2)).GetWeatherStationsFromAllStates();
    }
}
