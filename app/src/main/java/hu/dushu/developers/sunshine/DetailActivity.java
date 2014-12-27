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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.dushu.developers.sunshine.data.WeatherContract;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);

//        MenuItem item = menu.findItem(R.id.action_share);
//        ShareActionProvider provider = new ShareActionProvider(this);
//        provider.setShareIntent(createShareIntent());
//        MenuItemCompat.setActionProvider(item, provider);

        return true;
    }

//    private Intent createShareIntent() {
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_TEXT,
//                this.getIntent().getExtras().getString(Intent.EXTRA_TEXT));
//        return shareIntent;
//    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            startActivity(new Intent(this, SettingsActivity.class));

            return true;
        } else if (id == R.id.action_share) {


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

//        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private static final int FORECAST_LOADER = 0;

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
            rootView = inflater.inflate(R.layout.list_item_forecast, container, false);

//            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
//
//            TextView view = (TextView) rootView.findViewById(R.id.textview_detail);
//            view.setText(forecast);

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
                    ForecastFragment.FORECAST_COLUMNS,
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
//            String date = "d";
//            String weather = "w";
//            double high = 10;
//            double low = 1;
            boolean isMetric = Utility.isMetric(getActivity());

            {
                TextView view = (TextView) rootView.findViewById(R.id.list_item_date_textview);
                view.setText(Utility.formatDate(date));
            }
            {
                TextView view = (TextView) rootView.findViewById(R.id.list_item_forecast_textview);
                view.setText(weather);
            }
            {
                TextView view = (TextView) rootView.findViewById(R.id.list_item_high_textview);
                view.setText(Utility.formatTemperature(high, isMetric));
            }
            {
                TextView view = (TextView) rootView.findViewById(R.id.list_item_low_textview);
                view.setText(Utility.formatTemperature(low, isMetric));
            }

            return;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

}
