package hu.dushu.developers.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import hu.dushu.developers.sunshine.data.WeatherContract;


/**
 * Created by renfeng on 12/27/14.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

//        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final int FORECAST_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    private String forecast;
    private View rootView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        provider.setShareIntent(createShareIntent());

        return;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        forecast = getActivity().getIntent().getExtras().getString(Intent.EXTRA_TEXT);
//        rootView = inflater.inflate(R.layout.list_item_forecast, container, false);
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
//
//            TextView view = (TextView) rootView.findViewById(R.id.textview_detail);
//            view.setText(forecast);

        /*
         * no benefit on using view holder pattern
         */
//        rootView.setTag(new ViewHolder(rootView));

        return rootView;
    }

    private Intent createShareIntent() {

        Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, forecast + " #SunshineApp");

        return intent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationDateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                mLocation, forecast);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationDateUri,
                FORECAST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        getLoaderManager().initLoader(FORECAST_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();
        String date = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT));
        String weather = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
        double high = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
        double low = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
        double humidity = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY));
        float wind = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
        float degrees = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));
        double pressure = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));
        int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
//            String date = "d";
//            String weather = "w";
//            double high = 10;
//            double low = 1;
        boolean isMetric = Utility.isMetric(getActivity());

        {
            TextView view = (TextView) rootView.findViewById(R.id.date_textview);
            view.setText(Utility.formatDate(date));
        }
        {
            ImageView view = (ImageView) rootView.findViewById(R.id.icon_imageview);
            view.setImageResource(Utility.getResourceForWeatherCondition(weatherId, true, weather));
        }
        {
            TextView view = (TextView) rootView.findViewById(R.id.weather_textview);
            view.setText(weather);
        }
        {
            TextView view = (TextView) rootView.findViewById(R.id.high_textview);
            view.setText(Utility.formatTemperature(getActivity(), high, isMetric));
        }
        {
            TextView view = (TextView) rootView.findViewById(R.id.low_textview);
            view.setText(Utility.formatTemperature(getActivity(), low, isMetric));
        }
        {
            TextView view = (TextView) rootView.findViewById(R.id.humidity_textview);
            view.setText("Humidity: " + humidity + "%");
        }
        {
            TextView view = (TextView) rootView.findViewById(R.id.wind_textview);
            view.setText(Utility.getFormattedWind(getActivity(), wind, degrees));
        }
        {
            TextView view = (TextView) rootView.findViewById(R.id.pressure_textview);
            view.setText("Pressure: " + pressure + " hPa");
        }

        return;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

//    static class ViewHolder {
//
//        final TextView date;
//        final TextView weather;
//        final TextView high;
//        final TextView low;
//
//        public ViewHolder(View rootView) {
//            date = (TextView) rootView.findViewById(R.id.list_item_date_textview);
//            weather = (TextView) rootView.findViewById(R.id.list_item_forecast_textview);
//            high = (TextView) rootView.findViewById(R.id.list_item_high_textview);
//            low = (TextView) rootView.findViewById(R.id.list_item_low_textview);
//        }
//
//    }
}