package hu.dushu.developers.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity
        implements ForecastFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("activity lifecycle", "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ForecastFragment fragment = findViewById(R.id.fragment_forecast);

        if (findViewById(R.id.weather_detail_container) != null) {
            Log.d(LOG_TAG, "tablet");

            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
//            twoPane = true;
            setTwoPane(true);

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                DetailFragment detailFragment = new DetailFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, detailFragment)
                        .commit();
            }
        } else {
            Log.d(LOG_TAG, "phone");

//            twoPane = false;
            setTwoPane(false);
//            setTwoPane(true);

//            if (savedInstanceState == null) {
//                getSupportFragmentManager().beginTransaction()
//                        .add(R.id.container, new ForecastFragment())
//                        .commit();
//            }
        }

        ForecastFragment fragment = (ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);
        fragment.setUseTodayLayout(!isTwoPane());

        return;
    }

    @Override
    protected void onStart() {
        Log.d("activity lifecycle", "onStart()");
        super.onStart();
        // The activity is about to become visible.
    }

    @Override
    protected void onResume() {
        Log.d("activity lifecycle", "onResume()");
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }

    @Override
    protected void onPause() {
        Log.d("activity lifecycle", "onPause()");
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }

    @Override
    protected void onStop() {
        Log.d("activity lifecycle", "onStop()");
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }

    @Override
    protected void onDestroy() {
        Log.d("activity lifecycle", "onDestroy()");
        super.onDestroy();
        // The activity is about to be destroyed.
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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
        }

        /*
         * TODO move locate on map here
         */

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String date) {

        if (isTwoPane()) {
            DetailFragment detailFragment = new DetailFragment();

            Bundle args = new Bundle();
            args.putString(DetailActivity.DATE_KEY, date);
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment)
                    .commit();
        } else {
            //                Toast.makeText(getActivity(), item, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, DetailActivity.class);
//                intent.putExtra(Intent.EXTRA_TEXT, item);
            intent.putExtra(DetailActivity.DATE_KEY, date);
//                intent.putExtra(Intent.EXTRA_TEXT, "placeholder");

//                getActivity().startActivity(intent);
            startActivity(intent);
        }

        return;
    }

    public boolean isTwoPane() {
        return twoPane;
//        return true;
    }

    public void setTwoPane(boolean twoPane) {
        this.twoPane = twoPane;
    }
}
