package com.feer.windcast.tests;

import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.WeatherStation;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.feer.windcast.testUtils.ItemHintMatchers.withItemHint;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

/**
 * http://youtu.be/uHoB0KzQGRg?t=54s
 * see https://code.google.com/p/android-test-kit/wiki/EspressoStartGuide
 */
public class testStationFragment extends ActivityInstrumentationTestCase2<MainActivity>
{
    private MainActivity mMainActivity;

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

        mMainActivity = getActivity();
    }

    public void testSearchBoxOnScreen()
    {
        ViewInteraction searchBox = onView(withId(R.id.weather_station_search_box));
        searchBox.check(matches(isDisplayed()));
        searchBox.check(matches(withItemHint(R.string.weather_station_search_text)));
    }

    public void testWeatherStationItemsExit()
    {
        onData(instanceOf(WeatherStation.class))
            .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
            .atPosition(15)
            .check(matches(isDisplayed()));

        onData(instanceOf(WeatherStation.class))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(2)
                .check(matches(withText("Adele Island (WA)")));
    }


}
