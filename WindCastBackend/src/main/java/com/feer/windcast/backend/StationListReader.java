package com.feer.windcast.backend;

/**
 * Created by Reef on 14/10/2015.
 */

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Reads weather stations embedded as links in html
 * */
public class StationListReader {
    private static final Logger log = Logger.getLogger(StationListReader.class.getName());

    /* idUrlAndNamePattern get the URL and Station Name.

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
    static final Pattern obsTimeZone = Pattern.compile("<th id=\".*-datetime.*><acronym title=\".*\">([A-Z]+)</acronym>");
    static final Pattern obsDayAndTime = Pattern.compile("<td headers=\".*-datetime.*>(.*)</td>"); // get the day and time of a reading
    static final Pattern obsWindDir = Pattern.compile("<td headers=\".*-wind.dir .*\">(.*)</td>"); // get the wind direction in cardinal notation
    static final Pattern obsWindSpdKmh = Pattern.compile("<td headers=\".*-wind.spd.k[mp]h.*\">(.*)</td>"); // the the wind speed in Kmh

    // cheap way to exclude areas that do not contain data we care about:
    static final Pattern inTR = Pattern.compile("<tr"); // start of a table row
    static final Pattern endOfStationDetails = Pattern.compile("</tr>"); // end of a table row

    // parse the day time reading of the latest observation, the timezone must be added
    static final DateTimeFormatter full_date_parse_format = DateTimeFormat.forPattern("dd/hh:mmaa Z").withOffsetParsed();

    // To convert from a 4 letter timezone code to a DateTime compatible offset.
    private static final HashMap<String, String> timezoneAbToOffsetMap = new HashMap<String, String>(){{
        put("ACDT", "+10:30");
        put("ACST", "+09:30");
        put("ACWST","+08:45");
        put("AEDT", "+11:00");
        put("AEST", "+10:00");
        put("AWDT", "+09:00");
        put("AWST", "+08:00");
        put("CXT",  "+07:00");
        put("LHDT", "+11:00");
        put("LHST", "+10:30");
        put("NFT",  "+11:00");
    }};

    public static void GetWeatherStationsFromURL(URL fromUrl, String state) throws Exception {
        BufferedReader buf;
        try {
            buf = new BufferedReader(
                    new InputStreamReader(
                            fromUrl.openStream()));
        } catch (IOException e) {
            log.severe("Unable to get content from url: " + fromUrl.toString());
            throw new Exception("Cant get content", e);
        }

        String inputLine;
        boolean inTr = false;
        StationData d = null;
        LatestReading r = null;

        String lastFoundTimeZone = null;
        Matcher m;
        HashMap<String, DateTime> parsedDatesMap = new HashMap<String, DateTime>(20);

        while( (inputLine = buf.readLine()) != null)  {
            if(!inTr && (inTR.matcher(inputLine).find())) {
                inTr = true;
            }

            // time zones are always outside of station data, so a station record will not be currently being built
            if( inTr && d == null && (m = obsTimeZone.matcher(inputLine)).find()) {
                lastFoundTimeZone = timezoneAbToOffsetMap.get(
                        "A" + // added since BOM time zone acronym's and title exclude the A (Australia) see  http://www.timeanddate.com/time/zones/au
                        m.group(1));
                continue;
            }

            if( inTr && (m = idUrlAndNamePattern.matcher(inputLine)).find()) {
                r = new LatestReading();

                d = new StationData();
                d.setState(StationData.States.valueOf(state));
                d.setDataUrl("http://www.bom.gov.au" + StationListReader.ConvertToJSONURL(m.group(1)));
                d.setDisplayName(m.group(2));
                d.setStationID(state + "_" + d.getDisplayName());
                r.setStationID(d.getStationID());

                ofy().save().entity(d);
                continue;
            }

            if(d == null || r == null) {
                continue;
            }

            if (r.getLocalTime() == null && (m = obsDayAndTime.matcher(inputLine)).find()) {
                String day_hour_min = m.group(1); // in "dd/hh:mmam" form

                // We need to prefix day_hour_string with year and month, and append the timezone
                // to get it into the form required by full_date_parse_format
                String dateTime = day_hour_min + ' ' +  lastFoundTimeZone;

                DateTime localTime;
                if(parsedDatesMap.containsKey(dateTime)) {
                    localTime =  parsedDatesMap.get(dateTime);
                }else{
                    localTime = full_date_parse_format.parseDateTime(dateTime);
                    // localTime does not have the year or month set. So we take the server time,
                    // convert it to the same timezone as the local time. And then set the year and
                    // month.
                    DateTime serverTme = new DateTime();
                    DateTime localYearMonth = serverTme.withZone(localTime.getZone());
                    localTime = localTime.withYear(localYearMonth.getYear()).withMonthOfYear(localYearMonth.getMonthOfYear());

                    parsedDatesMap.put(dateTime, localTime);
                }
                LatestReading.setLocalTime(r, localTime);
                continue;
            }

            if (r.getCardinalWindDirection() == null && (m = obsWindDir.matcher(inputLine)).find()) {
                r.setCardinalWindDirection( m.group(1).toLowerCase(Locale.US));
                continue;
            }

            if  (r.getWindSpeed_KMH() == null
                && ( m = obsWindSpdKmh.matcher(inputLine)).find() && !m.group(1).equals("-")) {
                r.setWindSpeed_KMH(Integer.parseInt(m.group(1)));
                continue;
            }

            if (r != null && endOfStationDetails.matcher(inputLine).find()) {
                ofy().save().entity(r);

                d = null;
                r = null;
                inTr = false;
                continue;
            }
        }
        buf.close();
    }

    public static String ConvertToJSONURL(String urlString) {
        urlString = urlString.replaceAll("shtml", "json");
        urlString = urlString.replaceAll("products", "fwo");
        return urlString;
    }
}
