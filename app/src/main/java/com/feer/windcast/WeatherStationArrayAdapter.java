package com.feer.windcast;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.feer.windcast.dataAccess.WeatherDataCache;

import java.util.ArrayList;

import static com.feer.windcast.SettingsActivity.WindSpeedUnitPref.UnitType;

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
    private final boolean mUseKMH;

    public WeatherStationArrayAdapter(Context context, int layoutResourceID, ArrayList<WeatherData> objects, OnFavouriteChangedListener favChangedListner, boolean useKMH)
    {
        super(context, layoutResourceID, objects);

        mContext = context;
        mLayoutResourceID = layoutResourceID;
        mFavChangedListener = favChangedListner;
        mUseKMH = useKMH;
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


        TextView windSpeed = (TextView) convertView.findViewById(R.id.preview_wind_speed);
        windSpeed.setVisibility(View.GONE);

        ImageView direction = (ImageView) convertView.findViewById(R.id.preview_wind_dir);
        direction.setVisibility(View.GONE);


        ImageView errorIcon = (ImageView) convertView.findViewById(R.id.error_icon);
        errorIcon.setVisibility(View.GONE);

        TextView errorText = (TextView) convertView.findViewById(R.id.error_text);
        errorText.setVisibility(View.GONE);
        
        if(stationData.Station.IsFavourite && stationData.ObservationData == null)
        {
            // load latest reading
            new AsyncTask<Void, Void, Boolean>()
            {
                WeatherData wd;

                @Override
                protected Boolean doInBackground(Void... params)
                {
                    if(stationData == null || stationData.Station == null)
                    {
                        Log.w(TAG, "No weather station!");
                        return false;
                    }

                    Log.i("WindCast", "Getting data from: " + stationData.Station.GetURL().toString());
                    wd = WeatherDataCache.GetInstance().GetWeatherDataFor(stationData.Station);
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean result)
                {
                    if (stationData == null || wd == null || !result) return;
                    stationData.ObservationData = wd.ObservationData;
                    WeatherStationArrayAdapter.this.notifyDataSetChanged();
                }
            }.execute();
        }

        if(stationData.Station.IsFavourite && stationData.ObservationData != null && stationData.ObservationData.isEmpty() == false)
        {
            ObservationReading latestReading = stationData.ObservationData.get(0);
            Integer speed = mUseKMH ? latestReading.WindSpeed_KMH : latestReading.WindSpeed_KN;
            String unit = mUseKMH ? "km/h" : "kn";
            
            if(speed != null) {
                windSpeed.setText(String.format("%d %s", speed, unit));
                windSpeed.setVisibility(View.VISIBLE);
                direction.setVisibility(View.INVISIBLE);
                if (latestReading.WindBearing != null) {
                    // arrow image points right, rotate by 90 to point down when wind comes 
                    // FROM north with bearing 0, see ObservationReading.WindBearing
                    float arrowRotation = 90.f + latestReading.WindBearing;
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
}
