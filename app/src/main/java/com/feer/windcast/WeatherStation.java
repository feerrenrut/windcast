package com.feer.windcast;

import java.net.URL;
import java.util.Locale;

public class WeatherStation implements Comparable
{
    public enum States
    {
        WA,
        SA,
        ACT,
        QLD,
        NT,
        TAS,
        NSW,
        VIC
    }

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

    public boolean IsFavourite = false;

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

    public String GetLongStateName()
    {
        switch (mState)
        {
            case WA: return "Western Australia";
            case SA: return "South Australia";
            case VIC: return "Victoria";
            case TAS: return "Tasmania";
            case NSW: return "New South Wales";
            case NT:  return "Northern Territory";
            case QLD: return "Queensland";
            case ACT: return "Aust Capital Territory";
        }
        return "";
    }

    public String GetStateAbbreviated()
    {
        return mState.toString();
    }

    public URL GetURL() { return mUrl; }

    public String GetName() { return mName; }

    @Override
    public String toString()
    {
        return String.format(Locale.US, "%s (%s)", mName, GetStateAbbreviated());
    }

    @Override
    public int compareTo(Object o)
    {
        try
        {
            WeatherStation other = (WeatherStation)o;
            return compareTo(other);
        }
        catch(Exception e)
        {
            return -1;
        }
    }

    private int compareTo(WeatherStation other)
    {
        return mName.compareTo(other.mName);
    }

    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof WeatherStation)
        {
            WeatherStation other = (WeatherStation) object;
            sameSame =
                    this.compareTo(other) == 0 &&
                    this.mUrl.toString().equals(other.mUrl.toString());
        }
        return sameSame;
    }

    @Override
    public int hashCode()
    {
        if(mHashCode ==0)
        {
            mHashCode = 42;

            mHashCode = 3 * mHashCode + toString().hashCode();
            mHashCode = 3 * mHashCode + mUrl.hashCode();
        }
        return mHashCode;
    }

    private int mHashCode =0;
}
