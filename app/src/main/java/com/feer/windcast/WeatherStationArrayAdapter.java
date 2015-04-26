package com.feer.windcast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 */
public class WeatherStationArrayAdapter extends ArrayAdapter<WeatherData>
{
    private static final String TAG = "WeatherStationArrayAdapter";

    public static interface OnFavouriteChangedListener
    {
        public void OnFavouriteChanged(WeatherStation station);
    }

    private final Context mContext;
    private final int mLayoutResourceID;
    private final OnFavouriteChangedListener mFavChangedListener;
    private boolean mUseKMH = true;

    public WeatherStationArrayAdapter(Context context, int layoutResourceID, ArrayList<WeatherData> objects, OnFavouriteChangedListener favChangedListner)
    {
        super(context, layoutResourceID, objects);

        mContext = context;
        mLayoutResourceID = layoutResourceID;
        mFavChangedListener = favChangedListner;
    }
    
    public void SetUseKMH(boolean shouldUseKMH)
    {
        mUseKMH = shouldUseKMH;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null)
        {
            convertView = inflater.inflate(mLayoutResourceID, parent, false);
        }

        final WeatherData stationData = super.getItem(position);

        CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.is_favourite_checkbox);
        
        checkbox.setOnCheckedChangeListener(null); // we may be reusing the view, we do not want to call the listener on the next line
        checkbox.setChecked(stationData.Station.IsFavourite);
        // now the listener can be called.
        checkbox.setOnCheckedChangeListener(new OnStarClicked(stationData.Station, mFavChangedListener));

        TextView textView = (TextView) convertView.findViewById(R.id.station_name);
        textView.setText(stationData.Station.toString());

        WindPreview windPreview = new WindPreview(convertView).invoke();
        windPreview.SetPreviewData(stationData);

        return convertView;
    }

    private class OnStarClicked implements CheckBox.OnCheckedChangeListener
    {
        private final WeatherStation mStation;
        private final OnFavouriteChangedListener mFavChangedListener;

        OnStarClicked(WeatherStation station, OnFavouriteChangedListener favChangedListener)
        {
            mStation = station;
            mFavChangedListener = favChangedListener;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mStation.IsFavourite = isChecked;
            mFavChangedListener.OnFavouriteChanged(mStation);
        }
    }

    private class WindPreview {
        private View convertView;
        private TextView windSpeed;
        private TextView readingTime;
        private ImageView direction;
        private ImageView errorIcon;
        private TextView errorText;
        private final SimpleDateFormat readingTimeFormat = new SimpleDateFormat("h:mm a");

        public WindPreview(View convertView) {
            this.convertView = convertView;
        }

        public WindPreview invoke() {
            windSpeed = (TextView) convertView.findViewById(R.id.preview_wind_speed);
            windSpeed.setVisibility(View.GONE);
            
            readingTime = (TextView) convertView.findViewById(R.id.reading_time);
            readingTime.setVisibility(View.GONE);

            direction = (ImageView) convertView.findViewById(R.id.preview_wind_dir);
            direction.setVisibility(View.GONE);
            
            errorIcon = (ImageView) convertView.findViewById(R.id.error_icon);
            errorIcon.setVisibility(View.GONE);

            errorText = (TextView) convertView.findViewById(R.id.error_text);
            errorText.setVisibility(View.GONE);
            
            return this;
        }

        public void SetPreviewData(WeatherData weatherData) {
            if(weatherData.ObservationData != null && !weatherData.ObservationData.isEmpty())
            {
                ObservationReading latestReading = weatherData.ObservationData.get(0);
                Integer speed = mUseKMH ? latestReading.Wind_Observation.WindSpeed_KMH : latestReading.Wind_Observation.WindSpeed_KN;
                String unit = mUseKMH ? "km/h" : "kn";

                if(speed != null) {
                    windSpeed.setText(String.format("%d %s", speed, unit));
                    windSpeed.setVisibility(View.VISIBLE);
                    Date now = new Date();
                    long tooLong = 75L * 60 * 1000;
                    if(latestReading.LocalTime != null && now.getTime() - latestReading.LocalTime.getTime() > tooLong)
                    {
                        readingTime.setText("At: " + readingTimeFormat.format(latestReading.LocalTime));
                        readingTime.setVisibility(View.VISIBLE);
                    }
                    direction.setVisibility(View.INVISIBLE);
                    if (latestReading.Wind_Observation.WindBearing != null) {
                        // arrow image points right, rotate by 90 to point down when wind comes
                        // FROM north with bearing 0, see ObservationReading.WindBearing
                        float arrowRotation = 90.f + latestReading.Wind_Observation.WindBearing;
                        direction.setRotation(arrowRotation);
                        direction.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    errorText.setVisibility(View.VISIBLE);
                    errorIcon.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
