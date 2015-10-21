package com.feer.windcast;

import java.util.Date;
/**
 * Holds data for a weather observation at a particular time
 */
public class ObservationReading
{
    /* The time of the observation, in local time
     */
    public Date LocalTime;

    public TempObservation Temp_Observation;

    public WindObservation Wind_Observation;

    public SwellObservation Swell_Observation;
}

class SwellObservation
{
    /* The cardinal direction (n,s,e,w) of the ocean swell eg nnw
     */
    public String CardinalSwellDirection;

    /* The compass bearing for the swell direction
    *  0 = North, 90 = East, 180 = South, 270 = West
    */
    public Float SwellBearing;

    /* The height in meters of the ocean swell
     */
    public Double SwellHeight_meters;

    /* The swell period in seconds
     */
    public Integer SwellPeriod_seconds;
}

class TempObservation
{
    /* The temperature of the air, in degrees celsius
     */
    public Float AirTemp_DegCel;

    /* The apparent temperature, in degrees celsius
     */
    public Float ApparentTemp_DegCel;
}
