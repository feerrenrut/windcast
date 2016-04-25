package com.feer.windcast.tests;

import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.dataAccess.StationListCacheLoader;
import com.feer.windcast.testUtils.WindCastMocks;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.feer.windcast.testUtils.AdapterMatchers.adapterHasCount;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.any;
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

        Mocks.FakeData.HasStations(EXPECTED_NUM_STATIONS).HasFavourites(0);

        when(Mocks.FavouritesCache.GetFavouriteURLs())
                .thenReturn(Mocks.FakeData.FavURLs());
        when(Mocks.LoadedCache.GetWeatherStationsFromAllStates())
                .thenReturn(Mocks.FakeData.Stations());
        when(Mocks.LoadedCache.AreAllStatesFilled())
                .thenReturn(true);
        when(Mocks.LoadedCache.IsStale())
                .thenReturn(true);

        Mocks.VerifyNoUnstubbedCallsOnMocks();
        launchActivity();

        onView(withId(android.R.id.list))
                .check(matches(
                        adapterHasCount(equalTo(EXPECTED_NUM_STATIONS))));

        verify(Mocks.internalCacheLoader, times(1))
                .TriggerFillStationCache(any(StationListCacheLoader.CacheLoaderInterface.class));

        verify(Mocks.LoadedCache, times(1)).GetWeatherStationsFromAllStates();
    }
}
