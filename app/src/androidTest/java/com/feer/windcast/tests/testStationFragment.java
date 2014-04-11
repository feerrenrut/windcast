package com.feer.windcast.tests;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.WeatherDataCache;
import com.feer.windcast.WeatherStation;
import com.feer.windcast.testUtils.FakeWeatherStationData;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.feer.windcast.testUtils.AdapterMatchers.adapterHasCount;
import static com.feer.windcast.testUtils.ItemHintMatchers.withItemHint;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * http://youtu.be/uHoB0KzQGRg?t=54s
 * see https://code.google.com/p/android-test-kit/wiki/EspressoStartGuide
 */
public class testStationFragment extends ActivityInstrumentationTestCase2<MainActivity>
{
    private FakeWeatherStationData mFakeStations;
    private static final String PACKAGE_TO_TEST = "com.feer.windcast";

    @SuppressWarnings("deprecation")
    public testStationFragment()
    {
        super(PACKAGE_TO_TEST, MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mFakeStations = new FakeWeatherStationData("testStation");
    }

    public void test_searchBoxOnScreen()
    {
        Activity act = getActivity();
        ViewInteraction searchBox = onView(withId(R.id.weather_station_search_box));
        searchBox.check(matches(isDisplayed()));
        searchBox.check(matches(withItemHint(R.string.weather_station_search_text)));
    }

    public void test_withASingleStation_CreatingActivity_ShowsOneItem()
    {
        // set up weather data cache before starting the activity.
        WeatherDataCache cache = mock(WeatherDataCache.class);
        when(cache.GetWeatherStationsFromAllStates()).thenReturn(mFakeStations.GetSingleStation());
        WeatherDataCache.SetsWeatherDataCache(cache);

        // Activity is not created until get activity is called
        Activity act = getActivity();

        onView(withId(android.R.id.list)).check(matches(isDisplayed()));

        final int expectedNumberOfItems = 1;

        // this works but the error message is not so great, would be nice to get the actual value too!
        onView(withId(android.R.id.list))
                .check(matches(adapterHasCount(equalTo(expectedNumberOfItems))));

        onData(instanceOf(WeatherStation.class))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(expectedNumberOfItems-1)
                .check(matches(withText("testStation0 (null)")));
    }


}
