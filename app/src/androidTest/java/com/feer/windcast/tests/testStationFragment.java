package com.feer.windcast.tests;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.WeatherStation;
import com.feer.windcast.dataAccess.WeatherDataCache;
import com.feer.windcast.testUtils.FakeWeatherStationData;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.feer.windcast.testUtils.AdapterMatchers.adapterHasCount;
import static com.feer.windcast.testUtils.ItemHintMatchers.withItemHint;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
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
    private FakeWeatherStationData mFakeStations;
    private WeatherDataCache mCache;

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
        mFakeStations = new FakeWeatherStationData("test Station");

        // set up weather data cache before starting the activity.
        mCache = mock(WeatherDataCache.class);
        when(mCache.GetWeatherStationsFromAllStates()).thenReturn(mFakeStations.EmptyStationList());
        when(mCache.CreateNewFavouriteStationAccessor()).thenCallRealMethod();
        WeatherDataCache.SetsWeatherDataCache(mCache);
    }

    public void test_initialTitleBar()
    {
        launchActivity();
        Activity act = getActivity();
        final String expectedTitle = act.getResources().getString(R.string.app_name);
        assertEquals(expectedTitle, act.getActionBar().getTitle().toString());
    }

    public void test_searchBoxOnScreen()
    {
        launchActivity();
        ViewInteraction searchBox = onView(withId(R.id.weather_station_search_box));
        searchBox.check(matches(isDisplayed()));
        searchBox.check(matches(withItemHint(R.string.weather_station_search_text)));
    }

    public void test_enteringTextIntoSearchBox_FiltersStations()
    {
        when(mCache.GetWeatherStationsFromAllStates())
                .thenReturn(mFakeStations.GetAllStations());

        launchActivity();

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
        when(mCache.GetWeatherStationsFromAllStates()).thenReturn(mFakeStations.GetSingleStation(0));

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
        launchActivity();

        onView(withId(android.R.id.list)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.empty)).check(matches(isDisplayed()));

        //todo: Show something GOOD when there are no items
    }
}
