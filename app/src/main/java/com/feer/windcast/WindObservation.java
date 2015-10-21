package com.feer.windcast;

public class WindObservation
{
    /* The cardinal direction (n,s,e,w) the wind is coming from eg nnw
     */
    public String CardinalWindDirection;

    /* The compass bearing the wind is coming from
    *  0 = North, 90 = East, 180 = South, 270 = West
    */
    public Float WindBearing;

    /* The speed of the wind in KiloMeters per Hour
     */
    public Integer WindSpeed_KMH;

    /* The speed of the wind in knots
    */
    public Integer WindSpeed_KN;

    /* The wind gust speed in KiloMeters per Hour
     */
    public Integer WindGustSpeed_KMH;
}
