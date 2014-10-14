package com.feer.windcast.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.About;
import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.dataAccess.WeatherDataCache;
import com.feer.windcast.testUtils.FakeWeatherStationData;

import junit.framework.Assert;

import static android.app.Instrumentation.ActivityMonitor;
import static com.feer.windcast.testUtils.ItemHintMatchers.withItemHint;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Reef on 12/10/2014.
 */
public class testAboutScreen extends ActivityInstrumentationTestCase2<About>
{
    public testAboutScreen()
    {
        super(About.class);
    }

    // Activity is not created until get activity is called
    private void launchActivity()
    {
        getActivity();
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void test_noAction_BOMAttributionShown()
    {
        launchActivity();
        onView(withId(R.id.bom_attrib_imageView))
                .check(matches(isDisplayed()));
    }

    public void test_landscape_BOMAttributionShown()
    {
        launchActivity();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        onView(withId(R.id.bom_attrib_imageView))
                .check(matches(isDisplayed()));
    }

    /* Ideally we would test that we can launch a browser to the website... or test that the correct
    intent was launched. This is apparently not possible:
    http://stackoverflow.com/questions/21855540/android-espresso-web-browser

    public void test_clickBomAttribution_launchesBomWebsite()
    { } */

}
