package hu.dushu.developers.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Date;

import hu.dushu.developers.sunshine.data.WeatherContract;

/**
 * Created by renfeng on 12/12/14.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;

    private static final int FORECAST_LOADER = 0;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
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
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    //    public ArrayAdapter<String> adapter;
//    public SimpleCursorAdapter adapter;
    public ForecastAdapter adapter;

    private String mLocation;
//    private View rootView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        getLoaderManager().initLoader(FORECAST_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        return;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refresh();
            return true;
        } else if (id == R.id.action_locate) {

            /*
             * TODO move to main activity
             */

            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            String location = preferences
                    .getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + location));
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast toast = new Toast(getActivity());
                toast.setText("no map app");
                toast.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {

//        SharedPreferences preferences = PreferenceManager
//                .getDefaultSharedPreferences(getActivity());
//        String location = preferences
//                .getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
//        String unit = preferences
//                .getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_default));
//        Log.d("unit", unit);
        String location = Utility.getPreferredLocation(getActivity());
        new FetchWeatherTask(getActivity()).execute(location);

        return;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        ArrayList<String> list = new ArrayList<String>();
//        list.add("Today - Sunny - 88/63");
//        list.add("Tomorrow - Foggy - 70/46");
//        list.add("Weds - Cloudy - 72/63");
//        list.add("Thurs - Rainy - 46/51");
//        list.add("Friday - Foggy - 70/46");
//        list.add("Saturday - Sunny - 76/68");

//        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
//                R.layout.list_item_forecast,
//                R.id.list_item_forecast_textview,
//                list);
        // The SimpleCursorAdapter will take data from the database through the
        // Loader and use it to populate the ListView it's attached to.
//        adapter = new SimpleCursorAdapter(
//                getActivity(),
//                R.layout.list_item_forecast,
//                null,
//                // the column names to use to fill the textviews
//                new String[]{WeatherContract.WeatherEntry.COLUMN_DATETEXT,
//                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
//                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
//                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
//                },
//                // the textviews to fill with the data pulled from the columns above
//                new int[]{R.id.list_item_date_textview,
//                        R.id.list_item_forecast_textview,
//                        R.id.list_item_high_textview,
//                        R.id.list_item_low_textview
//                },
//                0
//        );
        ForecastAdapter adapter = new ForecastAdapter(
                getActivity(),
                /*TODO*/
                null,
                0
        );
        setAdapter(adapter);

        /*
         * this was missing!
         */
//        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
//            @Override
//            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//                boolean isMetric = Utility.isMetric(getActivity());
//                switch (columnIndex) {
//                    case COL_WEATHER_MAX_TEMP:
//                    case COL_WEATHER_MIN_TEMP: {
//                        // we have to do some formatting and possibly a conversion
//                        ((TextView) view).setText(Utility.formatTemperature(
//                                cursor.getDouble(columnIndex), isMetric));
//                        return true;
//                    }
//                    case COL_WEATHER_DATE: {
//                        String dateString = cursor.getString(columnIndex);
//                        TextView dateView = (TextView) view;
//                        dateView.setText(Utility.formatDate(dateString));
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView view = (ListView) rootView.findViewById(R.id.listView_forecast);
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                String item = adapter.getItem(position);
//                SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();

//                ForecastAdapter adapter = (ForecastAdapter) parent.getAdapter();
//                Cursor cursor = adapter.getCursor();
                Cursor cursor = getAdapter().getCursor();

//                String date = cursor.getString(cursor.getColumnIndex(
//                        WeatherContract.WeatherEntry.COLUMN_DATETEXT));
                String date = cursor.getString(COL_WEATHER_DATE);
//                String weather = cursor.getString(cursor.getColumnIndex(
//                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
//                double high = cursor.getDouble(cursor.getColumnIndex(
//                        WeatherEntry.COLUMN_MAX_TEMP));
//                double low = cursor.getDouble(cursor.getColumnIndex(
//                        WeatherEntry.COLUMN_MIN_TEMP));
//
//                boolean isMetric = Utility.isMetric(getActivity());
//                String item = Utility.formatDate(date)
//                        + " - " + weather
//                        + " - " + Utility.formatTemperature(high, isMetric)
//                        + "/" + Utility.formatTemperature(low, isMetric);
                String item = date;

                Callback callback = (Callback) getActivity();
                callback.onItemSelected(item);


                return;
            }
        });

//        refresh();

        return rootView;
    }

    public ForecastAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(ForecastAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        getAdapter().swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getAdapter().swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        String newLocation = Utility.getPreferredLocation(getActivity());
        if (mLocation != null && !mLocation.equals(newLocation)) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
//            mLocation = newLocation;
//            refresh();
        }
    }
}
