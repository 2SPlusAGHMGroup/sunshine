package hu.dushu.developers.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class DetailActivity extends ActionBarActivity {

    public static final String DATE_KEY = "forecast_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {

            Intent intent = getIntent();
//        if (intent == null || !intent.hasExtra(DetailActivity.DATE_KEY)) {
//            return null;
//        }
//            String date = intent.getExtras().getString(DetailActivity.DATE_KEY);
            String date = intent.getStringExtra(DetailActivity.DATE_KEY);

            Bundle args = new Bundle();
            args.putString(DetailActivity.DATE_KEY, date);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, detailFragment)
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
//        } else if (id == R.id.action_share) {
//            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
