package com.feer.windcast.testUtils;

import com.feer.windcast.WeatherData;
import com.feer.windcast.WeatherStation;
import com.feer.windcast.dataAccess.dependencyProviders.SpecificStationWeatherDataProvider;

/**
 * Created by Reef on 16/03/2016.
 */
public class MockSpecificStationWeatherDataProvider extends SpecificStationWeatherDataProvider {

    MockSpecificStationWeatherDataProvider() {
        sInstance = this;
    }
    WeatherData mWeatherData = null;
    void SetWeatherData(WeatherData data) {
        mWeatherData = data;
    }

    @Override
    protected WeatherData InternalGetWeatherDataFor(WeatherStation station) {
        return mWeatherData;
    }
}
