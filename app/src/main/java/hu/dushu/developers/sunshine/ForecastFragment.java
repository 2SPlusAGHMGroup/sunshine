package hu.dushu.developers.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by renfeng on 12/12/14.
 */
public class ForecastFragment extends Fragment {

    public ArrayAdapter<String> adapter;

    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);

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

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String location = preferences
                .getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        String unit = preferences
                .getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_default));
        Log.d("unit", unit);

        new FetchWeatherTask(getAdapter()).execute(location, unit);

        return;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> list = new ArrayList<String>();
//        list.add("Today - Sunny - 88/63");
//        list.add("Tomorrow - Foggy - 70/46");
//        list.add("Weds - Cloudy - 72/63");
//        list.add("Thurs - Rainy - 46/51");
//        list.add("Friday - Foggy - 70/46");
//        list.add("Saturday - Sunny - 76/68");

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                list);
        setAdapter(adapter);

        ListView view = (ListView) rootView.findViewById(R.id.listView_forecast);
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = adapter.getItem(position);

//                Toast.makeText(getActivity(), item, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, item);
                getActivity().startActivity(intent);

                return;
            }
        });

        refresh();

        return rootView;
    }

    public ArrayAdapter<String> getAdapter() {
        return adapter;
    }

    public void setAdapter(ArrayAdapter<String> adapter) {
        this.adapter = adapter;
    }
}
