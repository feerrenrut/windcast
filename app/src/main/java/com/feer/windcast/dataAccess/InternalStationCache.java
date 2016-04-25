package com.feer.windcast.dataAccess;

import com.feer.windcast.WeatherData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Reef on 20/10/2015.
 */

class InternalStationCache implements LoadedWeatherStationCache {

    protected InternalStationCache()
    {}

    private final ArrayList<WeatherData> mStations = new ArrayList<WeatherData>();
    private final ArrayList<String> mStatesLoaded = new ArrayList<String>();

    @Override
    public ArrayList<WeatherData> GetWeatherStationsFrom(String state) {
        ArrayList<WeatherData> stationsForState = new ArrayList<WeatherData>();
        for (WeatherData station : mStations) {
            if (station.Station.GetStateAbbreviated().equals(state)) {
                stationsForState.add(station);
            }
        }
        return stationsForState;
    }

    @Override
    public ArrayList<WeatherData> GetWeatherStationsFromAllStates() {
        return mStations;
    }

    @Override
    public boolean AreAllStatesFilled() {
        return StationsForAllStatesAdded();
    }

    Date mInternalStationCacheTime = null;
    @Override
    public boolean IsStale() {
        final long cacheTimeout = 15L * 60 * 1000; // 15 min
        final long currentTime = new Date().getTime();
        return mInternalStationCacheTime != null &&
                cacheTimeout <= // elapsed time
                        ( currentTime - mInternalStationCacheTime.getTime() );
    }

    public void AddStationsForState(ArrayList<WeatherData> stations, String state, Date loadedAt) {
        mStations.addAll(stations);
        mStatesLoaded.add(state);
        Collections.sort(mStations);
        mInternalStationCacheTime = loadedAt;
    }

    public boolean StationsForAllStatesAdded() {
        return mStatesLoaded.size() == mAllStationsInState_UrlList.length;
    }

    static class AllStationsURLForState {
        public AllStationsURLForState(String urlString, String state)
        {mUrlString = urlString; mState = state;}

        public String mUrlString;
        public String mState;
    }

    static final AllStationsURLForState[] mAllStationsInState_UrlList =
            {
                    new AllStationsURLForState("http://www.bom.gov.au/wa/observations/waall.shtml", "WA"),
                    new AllStationsURLForState("http://www.bom.gov.au/nsw/observations/nswall.shtml", "NSW"), //Strangely some the stations for ACT (inc. Canberra) are on this page!
                    new AllStationsURLForState("http://www.bom.gov.au/vic/observations/vicall.shtml", "VIC"),
                    new AllStationsURLForState("http://www.bom.gov.au/qld/observations/qldall.shtml", "QLD"),
                    new AllStationsURLForState("http://www.bom.gov.au/sa/observations/saall.shtml", "SA"),
                    new AllStationsURLForState("http://www.bom.gov.au/tas/observations/tasall.shtml", "TAS"),
                    new AllStationsURLForState("http://www.bom.gov.au/act/observations/canberra.shtml", "ACT"),
                    new AllStationsURLForState("http://www.bom.gov.au/nt/observations/ntall.shtml", "NT")
            };
}
