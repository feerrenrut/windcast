package com.feer.windcast.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;

import static android.test.ViewAsserts.assertOnScreen;

/**
 * http://youtu.be/uHoB0KzQGRg?t=54s
 */
public class testStationFragment extends ActivityInstrumentationTestCase2<MainActivity>
{
    private MainActivity mMainActivity;
    private EditText mSearchInput;

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
        mSearchInput = (EditText) mMainActivity.findViewById(R.id.weather_station_search_box);
    }

    public void testSearchBoxOnScreen()
    {
        assertOnScreen(mMainActivity.getWindow().getDecorView(), mSearchInput);
    }


}
