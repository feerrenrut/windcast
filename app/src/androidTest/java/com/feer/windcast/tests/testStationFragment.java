package com.feer.windcast.tests;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.WeatherStation;
import com.feer.windcast.WindCastNavigationDrawer;
import com.feer.windcast.dataAccess.FavouriteStationCache;
import com.feer.windcast.dataAccess.WeatherDataCache;
import com.feer.windcast.testUtils.FakeWeatherStationData;
import com.feer.windcast.testUtils.WindCastMocks;

import java.util.ArrayList;

import static com.feer.windcast.testUtils.AdapterMatchers.adapterHasCount;
import static com.feer.windcast.testUtils.ItemHintMatchers.withItemHint;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.doesNotExist;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.contrib.DrawerActions.closeDrawer;
import static com.google.android.apps.common.testing.ui.espresso.contrib.DrawerActions.openDrawer;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * http://youtu.be/uHoB0KzQGRg?t=54s
 * see https://code.google.com/p/android-test-kit/wiki/EspressoStartGuide
 */
public class testStationFragment extends ActivityInstrumentationTestCase2<MainActivity>
{
    private WindCastMocks mMocks;
    private final int drawerID = R.id.drawer_layout;

    // Activity is not created until get activity is called
    private void launchActivity()
    {
        getActivity();
    }

    public testStationFragment()
    {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        mMocks = new WindCastMocks(
                PreferenceManager.getDefaultSharedPreferences(
                        this.getInstrumentation().getTargetContext()));

        when(mMocks.mInternalCache.GetWeatherStationsFromAllStates())
                .thenReturn(mMocks.mFakeStations.GetAllStations());
        when(mMocks.mDataCache.CreateNewFavouriteStationAccessor())
                .thenCallRealMethod();
    }

    public void test_initialTitleBar()
    {
        launchActivity();
        Activity act = getActivity();
        final String expectedTitle = act.getResources().getString(R.string.app_name);
        assertEquals(expectedTitle, act.getActionBar().getTitle().toString());
    }

    public void test_noAction_searchBoxNotShown()
    {
        launchActivity();
        onView(withId(R.id.weather_station_search_box))
            .check(matches(not(isDisplayed())));
    }

    public void test_noAction_searchIconShown()
    {
        launchActivity();
        onView(withId(R.id.search))
            .check(matches(isDisplayed()));
    }

    public void test_clickSearchOption_searchBoxShown()
    {
        launchActivity();
        onView(withId(R.id.search)).perform(click());
        onView(withId(R.id.weather_station_search_box))
        .check(matches(
                allOf(
                    isDisplayed(),
                    withItemHint(R.string.weather_station_search_text))));
    }

    public void test_enteringTextIntoSearchBox_FiltersStations()
    {
        when(mMocks.mInternalCache.GetWeatherStationsFromAllStates())
                .thenReturn(mMocks.mFakeStations.GetAllStations());

        launchActivity();

        onView(withId(R.id.search)).perform(click());

        onView(withId(R.id.weather_station_search_box))
                .perform(typeText("Station3\n")); // '\n' is interpreted as an enter press

        onView(withId(android.R.id.list))
               .check(matches(adapterHasCount(equalTo(1))));

        onData(instanceOf(WeatherStation.class))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).onChildView(isAssignableFrom(TextView.class))
                .check(matches(withText("test Station3 (WA)")));
    }

    public void test_withASingleStation_CreatingActivity_ShowsOneItem()
    {
        // set up weather data cache before starting the activity.
        when(mMocks.mInternalCache.GetWeatherStationsFromAllStates())
                .thenReturn(mMocks.mFakeStations.GetSingleStation(0));

        launchActivity();

        onView(withId(android.R.id.list)).check(matches(isDisplayed()));

        final int expectedNumberOfItems = 1;

        // this works but the error message is not so great, would be nice to get the actual value too!
        onView(withId(android.R.id.list))
                .check(matches(adapterHasCount(equalTo(expectedNumberOfItems))));

        onData(instanceOf(WeatherStation.class))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(expectedNumberOfItems-1).onChildView(isAssignableFrom(TextView.class))
                .check(matches(withText("test Station0 (WA)")));
    }

    public void test_withNoStations_CreatingActivity_ShowsNoItems()
    {
        when(mMocks.mInternalCache.GetWeatherStationsFromAllStates())
                .thenReturn(mMocks.mFakeStations.EmptyStationList());
        
        launchActivity();

        onView(withId(android.R.id.list)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.empty)).check(matches(isDisplayed()));

        onView(withId(android.R.id.empty)).check(matches(withText(R.string.no_stations_available)));
    }

    public void test_clickAbout_launchesAboutActivity()
    {
        launchActivity();

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.options_about)).perform(click());
        onView(withText(R.string.about_windcast)).check(matches(isDisplayed()));
    }

    public void test_withNoFavourites_OnFavouriteView_SearchUnavailable()
    {
        launchActivity();

        openDrawer(R.id.drawer_layout);
        onView(withText(R.string.favourites)).perform(click());

        onView(withId(R.id.search)).check(doesNotExist());
    }

    public void test_withFavourites_OnFavouriteView_SearchAvailable()
    {
        when(mMocks.mInternalCache.GetWeatherStationsFromAllStates())
                .thenReturn(mMocks.mFakeStations.GetAllStations());

        mMocks.SetInternalCache_ReturnAFavStation(
                mock(FavouriteStationCache.class));

        launchActivity();

        openDrawer(R.id.drawer_layout);
        onView(withText(R.string.favourites)).perform(click());

        onView(withId(R.id.search)).check(matches(isDisplayed()));
    }

    public void test_withFavourites_OnFavouriteViewAfterRotate_SearchAvailable()
    {
        FavouriteStationCache favouriteStationCache;
        favouriteStationCache = mock(FavouriteStationCache.class);
        when(mMocks.mDataCache.CreateNewFavouriteStationAccessor())
                .thenReturn(favouriteStationCache);
        when(mMocks.mInternalCache.GetWeatherStationsFromAllStates())
                .thenReturn(mMocks.mFakeStations.GetAllStations());

        final int STATION_INDEX = 4;
        WeatherStation oldFav = mMocks.mFakeStations.GetAllStations().get(STATION_INDEX);
        oldFav.IsFavourite = true;
        ArrayList<String> favUrls = new ArrayList<String>();
        favUrls.add(oldFav.GetURL().toString());
        when(favouriteStationCache.GetFavouriteURLs()).thenReturn(favUrls);

        launchActivity();

        openDrawer(R.id.drawer_layout);
        onView(withText(R.string.favourites)).perform(click());

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        onView(withId(R.id.search)).check(matches(isDisplayed()));
    }
}
