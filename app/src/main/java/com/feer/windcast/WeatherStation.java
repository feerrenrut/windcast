package com.feer.windcast;

import java.net.URL;

public class WeatherStation extends AWeatherStation {

    public static class WeatherStationBuilder
    {
        private URL url;
        private String name;
        private States state;
        private boolean isFav;

        public WeatherStationBuilder WithURL(URL url)
        {
            this.url = url;
            return this;
        }

        public WeatherStationBuilder WithName(String name)
        {
            this.name = name;
            return this;
        }

        public WeatherStationBuilder WithState(States state)
        {
            this.state = state;
            return this;
        }

        public void IsFavourite(boolean isFav) {
            this.isFav = isFav;
        }

        public WeatherStation Build()
        {
            if (url == null) throw new IllegalArgumentException("Can not create station where URL is NULL");
            if (name == null) throw new IllegalArgumentException("Can not create station where Name is NULL");
            if (state == null) throw new IllegalArgumentException("Can not create station where State is NULL");

            return new WeatherStation(this);
        }
        public WeatherStationBuilder(){}
    }

    /* The name of the observation station
     */
    private final String mName;
    private final URL mUrl;
    private States mState;

    private WeatherStation(WeatherStationBuilder builder)
    {
        mName = builder.name;
        mUrl = builder.url;
        mState = builder.state;
        IsFavourite = builder.isFav;
    }

    public WeatherStation(WeatherStation other)
    {
        mName = other.mName;
        mUrl = other.mUrl;
        mState = other.mState;
        IsFavourite = other.IsFavourite;
    }

    @Override
    protected States GetState(){
        return mState;
    }

    @Override
    public String GetURL() { return mUrl.toString(); }

    @Override
    public String GetName() { return mName; }

}
