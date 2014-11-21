package com.feer.windcast;

import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class ObservationReader
{
    private final WeatherData mWeatherData = new WeatherData();

    public ObservationReader(WeatherStation station)
    {
        mWeatherData.Station = station;
    }

    public WeatherData GetWeatherData() throws IOException
    {
        BufferedInputStream bis;

        URLConnection urlConnection = mWeatherData.Station.GetURL().openConnection();
        InputStream is = urlConnection.getInputStream();
        bis = new BufferedInputStream(is);
        ReadJsonStream(bis);

        return mWeatherData;
    }

    private void ReadJsonStream(InputStream in) throws IOException
    {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try{
            reader.beginObject();
            while(reader.hasNext())
            {
                String name = reader.nextName();
                if(name.equals("observations"))
                    ReadObservations(reader);
                else
                    reader.skipValue();
            }
            reader.endObject();
        }finally
        {
            reader.close();
        }
    }

    private void ReadObservations(JsonReader reader)  throws IOException
    {
        reader.beginObject();
        while(reader.hasNext())
        {
            String name = reader.nextName();
            if(name.equals("header"))
            {
                reader.skipValue();
            }
            else if(name.equals("data"))
            {
                mWeatherData.ObservationData = ReadAllObservationData(reader);
            } else
            {
                reader.skipValue();
            }
        }
        reader.endObject();

        if (mWeatherData.ObservationData == null)
        {
            throw new IOException("No observations found in JSON stream");
        }
    }

    private static List<ObservationReading> ReadAllObservationData(JsonReader reader) throws IOException
    {
        List<ObservationReading> obs = new ArrayList<ObservationReading>();
        reader.beginArray();
        while (reader.hasNext()) {
            obs.add(ReadObservation(reader));
        }
        reader.endArray();
        return obs;
    }

    private static ObservationReading ReadObservation(JsonReader reader) throws IOException {
        ObservationReading ob = new ObservationReading();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();

            if(reader.peek().name().equals("NULL"))
            {
                reader.skipValue();
            }else{
                if (name.equals("local_date_time_full")) {
                    String dateString = reader.nextString();
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss",  Locale.US);
                    try
                    {
                        ob.LocalTime = df.parse(dateString);
                    } catch (ParseException e)
                    {
                        Log.e("WindCast", "unable to parse date string: " + dateString);
                        throw new IOException("Unable to parse date string", e);
                    }
                // read wind info
                } else if (name.equals("wind_spd_kmh") ) {
                    ob.WindSpeed_KMH = reader.nextInt();
                } else if (name.equals("wind_spd_kt") ) {
                    ob.WindSpeed_KN = reader.nextInt();
                } else if (name.equals("wind_dir") ) {
                    ob.CardinalWindDirection = reader.nextString().toLowerCase(Locale.US);
                    ob.WindBearing = ConvertCardinalCharsToBearing(ob.CardinalWindDirection);
                }else if (name.equals("gust_kmh") ) {
                    ob.WindGustSpeed_KMH = reader.nextInt();
                }
                // read temp info
                else if (name.equals("air_temp")) {
                    ob.AirTemp_DegCel = (float)reader.nextDouble();
                }else if (name.equals("apparent_t")) {
                    ob.ApparentTemp_DegCel = (float)reader.nextDouble();
                }
                // read swell info
                else if (name.equals("swell_dir_worded")) {
                    ob.CardinalSwellDirection = reader.nextString().toLowerCase(Locale.US);
                    ob.SwellBearing = ConvertCardinalCharsToBearing(ob.CardinalSwellDirection);
                }else if (name.equals("swell_height") ) {
                    ob.SwellHeight_meters = reader.nextDouble();
                }else if (name.equals("swell_period")) {
                    ob.SwellPeriod_seconds = reader.nextInt();
                }
                else {
                    reader.skipValue();
                }
            }
        }
        reader.endObject();
        return ob;
    }

    /* Convert a string of lowercase chars representing a cardinal direction
    *  into a bearing. 0 = North, 90 = East, 180 = South, 270 = West
    *  Handles empty String and "-" by returning null
     */
    private static Float ConvertCardinalCharsToBearing(String dirs)
    {
        Float bearing = null;
        final int len = dirs.length();
        if(len < 1 || dirs.equals("-"))
        {
            return null;
        }

        if(len == 1) {
            if(dirs.equals("n"))
                bearing = 0.0f;
            else if(dirs.equals("e"))
                bearing = 90.0f;
            else if(dirs.equals("s"))
                bearing = 180.0f;
            else if(dirs.equals("w"))
                bearing = 270.0f;
        }
        else
        if(len == 2) {
            if(dirs.equals("ne"))
                bearing = 45.0f;
            else if(dirs.equals("se"))
                bearing = 135.0f;
            else if(dirs.equals("sw"))
                bearing = 225.0f;
            else if(dirs.equals("nw"))
                bearing = 315.0f;
        } else
        if (len == 3) {
            if(dirs.equals("nne"))
                bearing = 22.5f;
            else if(dirs.equals("ene"))
                bearing = 64.5f;
            else if(dirs.equals("ese"))
                bearing = 112.5f;
            else if(dirs.equals("sse"))
                bearing = 157.5f;
            else if(dirs.equals("ssw"))
                bearing = 202.5f;
            else if(dirs.equals("wsw"))
                bearing = 247.5f;
            else if(dirs.equals("wnw"))
                bearing = 292.5f;
            else if(dirs.equals("nnw"))
                bearing = 337.5f;
        }
        else if (dirs.equals("calm"))
        {
            bearing = 0.f;
        }
        if(bearing == null)
        {
            throw new IllegalArgumentException("Unknown cardinal direction: " + dirs);
        }
        return bearing;
    }
}
