package com.feer.windcast.tests;

import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.testUtils.WindCastMocks;

/**
 * http://youtu.be/uHoB0KzQGRg?t=54s
 * see https://code.google.com/p/android-test-kit/wiki/EspressoStartGuide
 */
public class testStationFragment extends ActivityInstrumentationTestCase2<MainActivity>
{
    private WindCastMocks Mocks;
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

        Mocks = new WindCastMocks(
                PreferenceManager.getDefaultSharedPreferences(
                        this.getInstrumentation().getTargetContext()));
    }
    
    
}
