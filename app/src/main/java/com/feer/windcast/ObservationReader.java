package com.feer.windcast;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ObservationReader
{
    public static WeatherData ReadJsonStream(InputStream in) throws IOException
    {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try{
            WeatherData wd = null;

            reader.beginObject();
            while(reader.hasNext())
            {
                String name = reader.nextName();
                if(name.equals("observations"))
                    wd = ReadObservations(reader);
                else
                    reader.skipValue();
            }
            reader.endObject();
            return wd;
        }finally
        {
            reader.close();
        }
    }

    public static WeatherData ReadObservations(JsonReader reader)  throws IOException
    {
        WeatherData weatherData = null;
        List observationData = null;

        reader.beginObject();
        while(reader.hasNext())
        {
            String name = reader.nextName();
            if(name.equals("header"))
            {
                weatherData = ReadHeaderInfo(reader);
            }
            else if(name.equals("data"))
            {
                observationData = ReadAllObservationData(reader);
            } else
            {
                reader.skipValue();
            }
        }

        reader.endObject();

        if(weatherData != null)
        {
            weatherData.ObservationData = observationData;
        }
        else
        {
            throw new IOException("No header found in JSON stream");
        }

        return weatherData;
    }

    private static List ReadAllObservationData(JsonReader reader) throws IOException
    {
        List obs = new ArrayList<ObservationReading>();
        reader.beginArray();
        while (reader.hasNext()) {
            obs.add(ReadObservation(reader));
        }
        reader.endArray();
        return obs;
    }

    private static ObservationReading ReadObservation(JsonReader reader) throws IOException
    {
        ObservationReading ob = new ObservationReading();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("local_date_time_full")) {
                String dateString = reader.nextString();
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss",  Locale.US);
                try
                {
                    ob.LocalTime = df.parse(dateString);
                } catch (ParseException e)
                {
                    Log.e("WindCast", "unable to parse date string: " + dateString);
                }

            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return null;
    }

    private static WeatherData ReadHeaderInfo(JsonReader reader) throws IOException
    {
        WeatherData wd = new WeatherData();
        reader.beginArray();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                wd.WeatherStationName = reader.nextString();
            } else if (name.equals("time_zone")) {
                wd.TimeZone = reader.nextString();
            } else if (name.equals("state")) {
                wd.State = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        reader.endArray();

        return wd;
    }
}
