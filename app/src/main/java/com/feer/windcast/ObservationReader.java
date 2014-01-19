package com.feer.windcast;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 *
 */
public class ObservationReader
{
    static List ReadJsonStream(InputStream in) throws IOException
    {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try{
            return ReadObservationData(reader);
        }finally
        {
            reader.close();
        }
    }

    static List ReadObservationData(JsonReader reader)  throws IOException
    {
        List observations
    }
}
