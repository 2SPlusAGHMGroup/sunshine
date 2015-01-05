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
import android.util.Log;
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
import hu.dushu.developers.sunshine.sync.SunshineSyncAdapter;

/**
 * Created by renfeng on 12/12/14.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_LOCATION_LAT = 7;
    public static final int COL_LOCATION_LONG = 8;

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
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    private static final String POSITION_KEY = "position";

    //    public ArrayAdapter<String> adapter;
//    public SimpleCursorAdapter adapter;
    private ForecastAdapter adapter;

    private String mLocation;
//    private View rootView;

    private int position;
    private ListView listView;
//    private boolean useTodayLayout;

    private double lat;
    private double lon;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        getLoaderManager().initLoader(FORECAST_LOADER, null, this);

        /*
         * must be placed in this lifecycle phase
         */
        //        adapter.setTodayLayout(!((Layout) getActivity()).isTwoPane());
//        getAdapter().setTodayLayout(isUseTodayLayout());
        boolean useTodayLayout = !getResources().getBoolean(R.bool.two_pane_layout);
        getAdapter().setTodayLayout(useTodayLayout);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
        }

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
        /*if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        } else */
        if (id == R.id.action_locate) {

            /*
             * TODO move to main activity
             */

            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            String location = preferences.getString(
                    getString(R.string.pref_location_key),
                    getString(R.string.pref_location_default));

            /*
             * TODO get lat and long coordinates
             */
            Uri uri;
            if (getLat() == 0 && getLon() == 0) {
                uri = Uri.parse("geo:0,0?q=" + location);
            } else {
                uri = Uri.parse("geo:" + getLat() + "," + getLon() + "");
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
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

    private void updateWeather() {

////        SharedPreferences preferences = PreferenceManager
////                .getDefaultSharedPreferences(getActivity());
////        String location = preferences
////                .getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
////        String unit = preferences
////                .getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_default));
////        Log.d("unit", unit);
//        String location = Utility.getPreferredLocation(getActivity());
//
////        new FetchWeatherTask(getActivity()).execute(location);
////        Intent service = new Intent(getActivity(), SunshineService.class);
//        Intent service = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        service.putExtra(SunshineService.LOCATION_QUERY_EXTRA, location);
////        getActivity().startService(service);
//
//        PendingIntent broadcast = PendingIntent.getBroadcast(
//                getActivity(), 0, service, PendingIntent.FLAG_ONE_SHOT);
//
//        AlarmManager alarmManager = (AlarmManager) getActivity()
//                .getSystemService(Context.ALARM_SERVICE);
////        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000, broadcast);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, broadcast);
//
//        /*
//         * TODO ?
//         */
////        getActivity().sendBroadcast(service);
        SunshineSyncAdapter.syncImmediately(getActivity());

        return;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        /*
         * TODO validate position (ListView.INVALID_POSITION)
         */
        outState.putInt(POSITION_KEY, getPosition());

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

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
//                String date = cursor.getString(COL_WEATHER_DATE);

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

                /*
                 * must move cursor for two-panel layout
                 */
                if (cursor != null && cursor.moveToPosition(position)) {
                    String date = cursor.getString(COL_WEATHER_DATE);
                    Callback callback = (Callback) getActivity();
                    callback.onItemSelected(date);
                    Log.d(LOG_TAG, date);
                }
//                Callback callback = (Callback) getActivity();
//                callback.onItemSelected(date);
//                Log.d(LOG_TAG, date);
                Log.d(LOG_TAG, position + "");

                setPosition(position);

                return;
            }
        });

//        updateWeather();

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            int position = savedInstanceState.getInt(POSITION_KEY);
            setPosition(position);
        }

        setListView(view);

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

        /*
         * TODO validate position?
         */
        getListView().smoothScrollToPosition(getPosition());

        getAdapter().swapCursor(data);


        /*
         * on the first run, there is no data
         */
        if (data.moveToPosition(getPosition())) {
        /*
         * read lat and long
         */
            double lat = data.getDouble(
                    data.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LAT));
            double lon = data.getDouble(
                    data.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LONG));

            setLat(lat);
            setLon(lon);
        }

        return;
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
//            updateWeather();
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ListView getListView() {
        return listView;
    }

    public void setListView(ListView view) {
        this.listView = view;
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String date);
    }

//    public interface Layout {
//        boolean isTwoPane();
//    }

//    public boolean isUseTodayLayout() {
//        return useTodayLayout;
//    }
//
//    public void setUseTodayLayout(boolean useTodayLayout) {
//        this.useTodayLayout = useTodayLayout;
//    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
