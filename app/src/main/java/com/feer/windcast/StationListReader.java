package com.feer.windcast;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.feer.windcast.WeatherStation.States;

/**
 * Reads weather stations embedded as links in html
 * */
public class StationListReader
{

    /* because we have to escape the slashes and quotes in the regex this is hard to read.

   to break this down:
   we are trying to match a line like one of the following (in html)
   <th id="tKIM-station-broome-port" class="rowleftcolumn"><a href="/products/IDW60801/IDW60801.95202.shtml">Broome Port</a></th>
   <th id="obs-station-braidwood" class="rowleftcolumn"><a href="/products/IDN60903/IDN60903.94927.shtml">Braidwood</a></th>

   we want two groups:
        - one for the url (group 1)
        - one for the full station name (group 2)

   Group 1: (.*shtml)
   this is the relative link to the station page.
    - It comes after <a href="
    - It ends with 'shtml'

   Group 2: >(.*)</a>
   is all characters inside the anchor tag
*/
    static final Pattern idUrlAndNamePattern = Pattern.compile("<th id=\".*\" class.*<a href=\"(.*shtml)\">(.*)</a>.*");

    // get timezone from string eg:
    // <th id="tKIM-datetime" rowspan="2">Date/Time<br /><acronym title="Western Standard Time">WST</acronym></th>
    static final Pattern obsTimeZone = Pattern.compile("<th id=\".*-datetime.*><acronym title=\"(.*)\">");
    static final Pattern obsDayAndTime = Pattern.compile("<td headers=\".*-datetime.*>(.*)</td>");
    static final Pattern obsWindDir = Pattern.compile("<td headers=\".*-wind.dir .*\">(.*)</td>");
    static final Pattern obsWindSpdKmh = Pattern.compile("<td headers=\".*-wind.spd.kmh.*\">(.*)</td>");
    static final Pattern obsWindSpdKts = Pattern.compile("<td headers=\".*-wind.spd.kts.*\">(.*)</td>"); // could be -wind-spd-kts or -wind-spd_kts
    static final Pattern inTR = Pattern.compile("<tr");
    static final Pattern endOfStationDetails = Pattern.compile("</tr>");

    static final SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyyMM", Locale.US);
    static final String yearMonthString = yearMonthFormat.format(Calendar.getInstance().getTime());
    static final SimpleDateFormat full_date_parse_format = new SimpleDateFormat("yyyyMMdd/hh:mmaa zzzz",  Locale.US);

    static final String TAG = "StationListReader";
    public static ArrayList<WeatherData> GetWeatherStationsFromURL(URL fromUrl, String state) throws Exception
    {
        BufferedReader buf;
        try
        {
            buf = new BufferedReader(
                    new InputStreamReader(
                            fromUrl.openStream()));
        } catch (IOException e)
        {
            Log.e(TAG, "Unable to get content from url: " + fromUrl.toString());
            throw new Exception("Cant get content", e);
        }

        ArrayList<WeatherData> weatherStations = new ArrayList<WeatherData>();

        String str;
        boolean inTr = false;
        WeatherData d = null;
        ObservationReading r = null;
        final String source = fromUrl.toString().replaceFirst("http://www.bom.gov.au/", "");
        String lastFoundTimeZone = null;
        Matcher m = null;
        Hashtable<String, Date> parsedDatesmap = new Hashtable<String, Date>(20);
        while((str = buf.readLine()) != null)
        {
            if(!inTr && (inTR.matcher(str).find()))
            {
                inTr = true;
            }

            // time zones are always outside of station data, so a station record will not be currently being built
            if(inTr && d == null && (m = obsTimeZone.matcher(str)).find())
            {
                lastFoundTimeZone = "australian " + // added since BOM time zone acronym's and title exclude the A (Australia) see  http://www.timeanddate.com/time/zones/au
                        m.group(1).toLowerCase(Locale.US).replace("savings ", ""); // removed 'savings ' since SimpleDateFormat does not parse it.
                continue;
            }

            if(inTr && (m = idUrlAndNamePattern.matcher(str)).find())
            {
                d = new WeatherData();
                d.Source = source;
                r = new ObservationReading();
                r.Wind_Observation = new WindObservation();
                d.ObservationData = new ArrayList<ObservationReading>(1);
                d.ObservationData.add(r);

                d.Station = new WeatherStation.WeatherStationBuilder()
                        .WithState(States.valueOf(state))
                        .WithURL(new URL("http://www.bom.gov.au" + StationListReader.ConvertToJSONURL(m.group(1))))
                        .WithName(m.group(2))
                        .Build();

                continue;
            }
            
            if(d == null || r == null)
            {
                continue;
            }

            if (r.LocalTime == null && (m = obsDayAndTime.matcher(str)).find())
            {
                String day_hour_min = m.group(1); // in "dd/hh:mmam" form

                // We need to prefix day_hour_string with year and month, and append the timezone
                // to get it into the form required by full_date_parse_format eg "yyyyMMdd/hh:mmaa zzzz"
                String dateTime = yearMonthString  + day_hour_min + ' ' +  lastFoundTimeZone;

                try
                {
                    if(parsedDatesmap.containsKey(dateTime)) {
                        r.LocalTime = parsedDatesmap.get(dateTime);
                    }else{
                        r.LocalTime = full_date_parse_format.parse(dateTime);
                        parsedDatesmap.put(dateTime, r.LocalTime);
                    }
                } catch (ParseException e)
                {
                    Log.e("StationListReader", "Getting LocalTime: " + e.toString());
                    parsedDatesmap.put(dateTime, null); // dont want to try to parse again
                }
                continue;
            }

            if (r.Wind_Observation.WindBearing == null && (m = obsWindDir.matcher(str)).find())
            {
                r.Wind_Observation.CardinalWindDirection = m.group(1).toLowerCase(Locale.US);
                r.Wind_Observation.WindBearing = ObservationReader.ConvertCardinalCharsToBearing(r.Wind_Observation.CardinalWindDirection);
                continue;
            }

            if ( r.Wind_Observation.WindSpeed_KMH == null &&
                    (m = obsWindSpdKmh.matcher(str)).find() &&
                    !m.group(1).equals("-"))
            {
                r.Wind_Observation.WindSpeed_KMH = Integer.parseInt(m.group(1));
                continue;
            }

            if (r.Wind_Observation.WindSpeed_KN == null &&
                    (m = obsWindSpdKts.matcher(str)).find() &&
                    !m.group(1).equals("-"))
            {
                r.Wind_Observation.WindSpeed_KN = Integer.parseInt(m.group(1));
                continue;
            }

            if(d.Station != null && endOfStationDetails.matcher(str).find()) {
                weatherStations.add(d);
                d = null;
                r = null;
                inTr = false;
                continue;
            }
        }

        buf.close();
        return weatherStations;
    }

    public static String ConvertToJSONURL(String urlString)
    {
        urlString = urlString.replaceAll("shtml", "json");
        urlString = urlString.replaceAll("products", "fwo");
        return urlString;
    }
}
