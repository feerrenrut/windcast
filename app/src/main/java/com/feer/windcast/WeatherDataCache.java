package com.feer.windcast;

import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 */
public class WeatherDataCache
{
    Resources m_res;
    WeatherDataCache(Resources res)
    {
        m_res = res;
    }

    public WeatherData GetWeatherData()
    {
        WeatherData wd = null;
        try
        {
            boolean getFromNet = false;
            BufferedInputStream bis;
            if(getFromNet)
            {
                URL url = new URL("http://www.bom.gov.au/fwo/IDW60801/IDW60801.94603.json");
                URLConnection ucon = url.openConnection();
                InputStream is = ucon.getInputStream();
                bis = new BufferedInputStream(is);
            }
            else
            {
                InputStream is = m_res.openRawResource(R.raw.test_data);
                bis = new BufferedInputStream(is);
            }
            wd = ObservationReader.ReadJsonStream(bis);

        } catch (MalformedURLException e)
        {
            Log.e("DATA", e.getMessage());
        } catch (IOException e)
        {
            Log.e("DATA", e.getMessage());
        }
        return wd;
    }
}
