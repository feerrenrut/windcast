package com.feer.windcast;

import java.util.Locale;

/**
 * Created by Reef on 21/10/2015.
 */
public abstract class AWeatherStation implements Comparable {
    public enum States {
        WA,
        SA,
        ACT,
        QLD,
        NT,
        TAS,
        NSW,
        VIC
    }

    public boolean IsFavourite = false;

    public abstract String GetURL();

    public abstract String GetName();

    protected abstract States GetState();

    public String GetLongStateName() {
        switch (GetState())
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

    public String GetStateAbbreviated() {
        return GetState().toString();
    }

    @Override
    public String toString()
    {
        return String.format(Locale.US, "%s (%s)", GetName(), GetStateAbbreviated());
    }

    @Override
    public int compareTo(Object o)
    {
        try
        {
            AWeatherStation other = (AWeatherStation)o;
            return compareTo(other);
        }
        catch(Exception e)
        {
            return -1;
        }
    }

    private int compareTo(AWeatherStation other)
    {
        return GetName().compareTo(other.GetName());
    }

    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof AWeatherStation)
        {
        AWeatherStation other = (AWeatherStation) object;
        sameSame =
        this.compareTo(other) == 0 &&
                this.GetURL().equals(other.GetURL());
        }
        return sameSame;
    }

    private int mHashCode =0;
    @Override
    public int hashCode()
    {
        if(mHashCode ==0)
        {
            mHashCode = 42;

            mHashCode = 3 * mHashCode + toString().hashCode();
            mHashCode = 3 * mHashCode + GetURL().hashCode();
        }
        return mHashCode;
    }
}
