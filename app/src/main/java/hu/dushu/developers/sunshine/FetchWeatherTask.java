package hu.dushu.developers.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

import hu.dushu.developers.sunshine.data.WeatherContract;

/**
 * Created by renfeng on 12/12/14.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    private static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    private final FragmentActivity activity;

    public FetchWeatherTask(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(String... params) {

        String[] result;

        String locationSetting = params[0];
//        String units = params[1];
        String units = "metric";
//            String days = "7";
//        String days = params[2];
        int days = 14;

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
//            URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

            Uri uri = Uri.parse("http://api.openweathermap.org/data/2.5/forecast/daily");
            Uri.Builder ub = uri.buildUpon();
            ub.appendQueryParameter("q", locationSetting);
            ub.appendQueryParameter("mode", "json");
            ub.appendQueryParameter("units", units);
            ub.appendQueryParameter("cnt", days + "");
            URL url = new URL(ub.build().toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                forecastJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                forecastJsonStr = null;
            }
            forecastJsonStr = buffer.toString();

            Log.d(LOG_TAG, forecastJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            forecastJsonStr = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
//            result = getWeatherDataFromJson(forecastJsonStr, 14, locationSetting);
            getWeatherDataFromJson(forecastJsonStr, days, locationSetting);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            result = null;
        }

        return null;
    }

//    @Override
//    protected void onPostExecute(String[] strings) {
//
////        ArrayList<String> list = new ArrayList<String>();
////        list.add("Today - Sunny - 88/63");
////        list.add("Tomorrow - Foggy - 70/46");
////        list.add("Weds - Cloudy - 72/63");
////        list.add("Thurs - Rainy - 46/51");
////        list.add("Friday - Foggy - 70/46");
////        list.add("Saturday - Sunny - 76/68");
//
////        adapter.clear();
////        for (String s : strings) {
////            adapter.add(s);
////        }
//
//        return;
//    }

//    /* The date/time conversion code is going to be moved outside the asynctask later,
//     * so for convenience we're breaking it out into its own method now.
//     */
//    private String getReadableDateString(long time) {
//        // Because the API returns a unix timestamp (measured in seconds),
//        // it must be converted to milliseconds in order to be converted to valid date.
//        Date date = new Date(time * 1000);
//        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
//        return format.format(date).toString();
//    }

//    /**
//     * Prepare the weather high/lows for presentation.
//     */
//    private String formatHighLows(double high, double low) {
//        // For presentation, assume the user doesn't care about tenths of a degree.
//        long roundedHigh = Math.round(high);
//        long roundedLow = Math.round(low);
//
//        String highLowStr = roundedHigh + "/" + roundedLow;
//        return highLowStr;
//    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson(String forecastJsonStr, int numDays, String locationSetting)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_PRESSURE = "pressure";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";
        final String OWM_WEATHER_ID = "id";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject("city");

        String cityName = cityJson.getString("name");
        JSONObject coordJson = cityJson.getJSONObject("coord");

        double lat = coordJson.getDouble("lat");
        double lon = coordJson.getDouble("lon");

        Log.v(LOG_TAG, cityName + ", with coord: " + lat + " " + lon);
        addLocation(locationSetting, cityName, lat, lon);

        // Get and insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

//        String[] resultStrs = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {
            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            long dateTime = dayForecast.getLong(OWM_DATETIME);
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            int humidity = dayForecast.getInt(OWM_HUMIDITY);
            double pressure = dayForecast.getDouble(OWM_PRESSURE);
            double windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            double windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            int weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            // description is in a child array called "weather", which is 1 element long.
            String description = weatherObject.getString(OWM_DESCRIPTION);

            // For now, using the format "Day, description, hi/low"
            String day;
            String highAndLow;

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
//            day = getReadableDateString(dateTime);


            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

//            highAndLow = formatHighLows(high, low);
//            resultStrs[i] = day + " - " + description + " - " + highAndLow;


            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationSetting);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                    WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cVVector.add(weatherValues);
        }

        activity.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,
                cVVector.toArray(new ContentValues[weatherArray.length()]));

//        return resultStrs;
        return;
    }

    private long addLocation(String locationSetting, String cityName, double lat, double lon) {

        Log.v(LOG_TAG, "inserting " + cityName + ", with coord: " + lat + ", " + lon);

        long id;
//        ContentValues values = new ContentValues();
//        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
//        values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
//        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
//        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);
//
//        WeatherDbHelper dbHelper = new WeatherDbHelper(activity);
//        try {
//            SQLiteDatabase db = dbHelper.getWritableDatabase();
//            id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
//        } finally {
//            dbHelper.close();
//        }
        Cursor cursor = activity.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

        if (cursor.moveToFirst()) {
            Log.v(LOG_TAG, "Found it in the database!");
            int locationIdIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            id = cursor.getLong(locationIdIndex);
        } else {
            Log.v(LOG_TAG, "Didn't find it in the database, inserting now!");
            ContentValues values = new ContentValues();
            values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri uri = activity.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI,
                    values);
            id = ContentUris.parseId(uri);
        }

        return id;
    }
}
