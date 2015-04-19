package com.feer.windcast;

import java.util.Date;
/**
 * Holds data for a weather observation at a particular time
 */
public class ObservationReading
{
    /* The time of the observation, in local time
     */
    Date LocalTime;

    TempObservation Temp_Observation;
    
    WindObservation Wind_Observation;
    
    SwellObservation Swell_Observation;
}

class SwellObservation
{
    /* The cardinal direction (n,s,e,w) of the ocean swell eg nnw
     */
    String CardinalSwellDirection;

    /* The compass bearing for the swell direction
    *  0 = North, 90 = East, 180 = South, 270 = West
    */
    Float SwellBearing;

    /* The height in meters of the ocean swell
     */
    Double SwellHeight_meters;

    /* The swell period in seconds
     */
    Integer SwellPeriod_seconds;
}

class WindObservation
{
    /* The cardinal direction (n,s,e,w) the wind is coming from eg nnw
     */
    String CardinalWindDirection;

    /* The compass bearing the wind is coming from
    *  0 = North, 90 = East, 180 = South, 270 = West
    */
    Float WindBearing;

    /* The speed of the wind in KiloMeters per Hour
     */
    Integer WindSpeed_KMH;

    /* The speed of the wind in knots
    */
    Integer WindSpeed_KN;

    /* The wind gust speed in KiloMeters per Hour
     */
    Integer WindGustSpeed_KMH;
}

class TempObservation
{
    /* The temperature of the air, in degrees celsius
     */
    Float AirTemp_DegCel;

    /* The apparent temperature, in degrees celsius
     */
    Float ApparentTemp_DegCel;   
}
