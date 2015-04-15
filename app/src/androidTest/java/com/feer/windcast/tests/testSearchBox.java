package com.feer.windcast.tests;

import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.WeatherData;
import com.feer.windcast.WeatherStation;
import com.feer.windcast.testUtils.WindCastMocks;

import org.hamcrest.CoreMatchers;

import static com.feer.windcast.testUtils.AdapterMatchers.adapterHasCount;
import static com.feer.windcast.testUtils.ItemHintMatchers.withItemHint;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * http://youtu.be/uHoB0KzQGRg?t=54s
 * see https://code.google.com/p/android-test-kit/wiki/EspressoStartGuide
 */
public class testSearchBox extends ActivityInstrumentationTestCase2<MainActivity>
{
    private WindCastMocks Mocks;

    // Activity is not created until get activity is called
    private void launchActivity()
    {
        getActivity();
    }

    public testSearchBox()
    {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        Mocks = new WindCastMocks(
                PreferenceManager.getDefaultSharedPreferences(
                        this.getInstrumentation().getTargetContext()));
    }

    public void test_noAction_searchBoxNotShown()
    {
        // depends on:
        // testSearchIcon.test_withStationsButNoFavourites_onAllStationsView_searchIconShown();
        Mocks.Fakes.HasStations(10).HasFavourites(0);
        Mocks.JustUseMocksWithFakeData();

        launchActivity();

        onView(withId(R.id.search)).check(matches(isDisplayed()));
        
        onView(withId(R.id.weather_station_search_box))
                .check(matches(not(isDisplayed())));
    }

    public void test_clickSearchOption_searchBoxShown()
    {
        test_noAction_searchBoxNotShown();
        onView(withId(R.id.search)).perform(click());
        
        onView(withId(R.id.weather_station_search_box))
        .check(matches(
                allOf(
                    isDisplayed(),
                    withItemHint(R.string.weather_station_search_text))));
    }

    // perhaps this test should work like the others and just rely on 
    // test_clickSearchOption_searchBoxShown. If it fails regularly try this to make it more robust.
    //
    // Failure log:
    // * failed on nexus 4 API 19 on 9/04/15 failure due to ui change
    // * failed on nexus 4 API 19 on 12/04/15 failure due to ui change
    public void test_enteringTextIntoSearchBox_FiltersStations()
    {
        Mocks.Fakes.HasStations(11).HasFavourites(0);
        Mocks.JustUseMocksWithFakeData();
        int EXPECTED_NUM_STATIONS = Mocks.Fakes.Stations().size();
        
        launchActivity();

        onView(withId(android.R.id.list))
                .check(matches(
                        adapterHasCount(CoreMatchers.equalTo(EXPECTED_NUM_STATIONS))));

        onView(withId(R.id.search)).perform(click());
        WeatherStation expected = Mocks.Fakes.Stations().get(2);
        
        // just use the last few characters of the name;
        String searchTerm = expected.GetName();
        searchTerm = searchTerm.substring(
                searchTerm.lastIndexOf("Station"),
                searchTerm.length());

        onView(withId(R.id.weather_station_search_box))
                .perform(typeText(searchTerm  + "\n")); // '\n' is interpreted as an enter press

        onView(withId(android.R.id.list))
               .check(matches(adapterHasCount(equalTo(1))));

        onData(instanceOf(WeatherData.class))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).onChildView(withId(R.id.station_name))
                .check(matches(withText(expected.toString())));
    }
}
