package com.feer.windcast;

import com.feer.windcast.backend.windcastdata.model.StationData;

/**
 * Created by Reef on 22/10/2015.
 */
public class GAE_WeatherStation extends AWeatherStation {
    final StationData mData;
    public GAE_WeatherStation(StationData data) {
        this.mData = data;
    }

    @Override
    public String GetURL() {
        return mData.getDataUrl();
    }

    @Override
    public String GetName() {
        return mData.getDisplayName();
    }

    @Override
    protected States GetState() {
        return States.valueOf(mData.getState());
    }
}
