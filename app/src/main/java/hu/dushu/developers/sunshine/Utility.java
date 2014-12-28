package hu.dushu.developers.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import hu.dushu.developers.sunshine.data.WeatherContract;

/**
 * Created by renfeng on 12/25/14.
 */
public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, String dateStr) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Date todayDate = new Date();
        String todayStr = WeatherContract.getDbDateString(todayDate);
        Date inputDate = WeatherContract.getDateFromDb(dateStr);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (todayStr.equals(dateStr)) {
            String today = context.getString(R.string.today);
            return context.getString(
                    R.string.format_full_friendly_date,
                    today,
                    getFormattedMonthDay(context, dateStr));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 7);
            String weekFutureString = WeatherContract.getDbDateString(cal.getTime());

            if (dateStr.compareTo(weekFutureString) < 0) {
                // If the input date is less than a week in the future, just return the day name.
                return getDayName(context, dateStr);
            } else {
                // Otherwise, use the form "Mon Jun 3"
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return
     */
    public static String getDayName(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.
            if (WeatherContract.getDbDateString(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                // If the date is set for tomorrow, the format is "Tomorrow".
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                Date tomorrowDate = cal.getTime();
                if (WeatherContract.getDbDateString(tomorrowDate).equals(
                        dateStr)) {
                    return context.getString(R.string.tomorrow);
                } else {
                    // Otherwise, the format is just the day of the week (e.g "Wednesday".
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // It couldn't process the date correctly.
            return "";
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
            String monthDayString = monthDayFormat.format(inputDate);
            return monthDayString;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    /*
     * the following three methods were missing from the lecture
     */

    /**
     * Returns true if metric unit should be used, or false if
     * imperial units should be used.
     */
    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric)).equals(
                context.getString(R.string.pref_units_metric));
    }

    //    static String formatTemperature(double temperature, boolean isMetric) {
//        double temp;
//        if (!isMetric) {
//            temp = 9 * temperature / 5 + 32;
//        } else {
//            temp = temperature;
//        }
//        return String.format("%.0f", temp);
//    }
    static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        return context.getString(R.string.format_temperature, temp);
    }

    static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 || degrees < 22.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            /*
             * freezing raining
             */
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 522 || weatherId == 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
//            return R.drawable.art_rain;
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    public static int getResourceForWeatherCondition(int weatherId, boolean art, String description) {

        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        switch (weatherId) {
            case 200:
            case 201:
            case 202:
            case 210:
            case 211:
            case 212:
            case 221:
                if (art) {
                    return R.drawable.art_storm;
                } else {
                    return R.drawable.ic_storm;
                }
//            case 225:
//            case 226:
//            case 227:
//            case 228:
//                /*
//                 * experience
//                 */
//                if (art) {
//                    return R.drawable.art_clear;
//                } else {
//                    return R.drawable.ic_clear;
//                }
//            case 229:
//                /*
//                 * experience
//                 */
//                if (art) {
//                    return R.drawable.art_rain;
//                } else {
//                    return R.drawable.ic_rain;
//                }
            case 230:
            case 231:
            case 232:
                if (art) {
                    return R.drawable.art_storm;
                } else {
                    return R.drawable.ic_storm;
                }
//            case 239:
//            case 240:
//            case 241:
//            case 242:
//                /*
//                 * experience
//                 */
//                if (art) {
//                    return R.drawable.art_clear;
//                } else {
//                    return R.drawable.ic_clear;
//                }
//            case 243:
//            case 244:
//                /*
//                 * experience
//                 */
//                if (art) {
//                    return R.drawable.art_rain;
//                } else {
//                    return R.drawable.ic_rain;
//                }
//            case 253:
//            case 254:
//            case 255:
//            case 256:
//                /*
//                 * experience
//                 */
//                if (art) {
//                    return R.drawable.art_clear;
//                } else {
//                    return R.drawable.ic_clear;
//                }
//            case 257:
//            case 258:
//                /*
//                 * experience
//                 */
//                if (art) {
//                    return R.drawable.art_rain;
//                } else {
//                    return R.drawable.ic_rain;
//                }
            case 300:
            case 301:
            case 302:
            case 310:
            case 311:
            case 312:
            case 313:
            case 314:
            case 321:
                if (art) {
                    return R.drawable.art_light_rain;
                } else {
                    return R.drawable.ic_light_rain;
                }
            case 500:
            case 501:
            case 502:
            case 503:
            case 504:
            case 511:
            case 520:
            case 521:
            case 522:
            case 531:
                if (art) {
                    return R.drawable.art_rain;
                } else {
                    return R.drawable.ic_rain;
                }
            case 600:
            case 601:
            case 602:
            case 611:
            case 612:
            case 615:
            case 616:
            case 620:
            case 621:
            case 622:
                if (art) {
                    return R.drawable.art_snow;
                } else {
                    return R.drawable.ic_snow;
                }
            case 701:
                if (art) {
                    return R.drawable.art_fog;
                } else {
                    return R.drawable.ic_fog;
                }
            case 711:
                /*
                 * smoke
                 */
                break;
            case 721:
                if (art) {
                    return R.drawable.art_fog;
                } else {
                    return R.drawable.ic_fog;
                }
            case 731:
                /*
                 * sand/dust whirls
                 */
                break;
            case 741:
                if (art) {
                    return R.drawable.art_fog;
                } else {
                    return R.drawable.ic_fog;
                }
            case 751:
                /*
                 * sand
                 */
                break;
            case 761:
                /*
                 * dust
                 */
                break;
            case 762:
                /*
                 * volcanic ash
                 */
                break;
            case 771:
            case 781:
                if (art) {
                    return R.drawable.art_storm;
                } else {
                    return R.drawable.ic_storm;
                }
            case 800:
                if (art) {
                    return R.drawable.art_clear;
                } else {
                    return R.drawable.ic_clear;
                }
            case 801:
                if (art) {
                    return R.drawable.art_light_clouds;
                } else {
                    return R.drawable.ic_light_clouds;
                }
            case 802:
            case 803:
            case 804:
                if (art) {
                    return R.drawable.art_clouds;
                } else {
                    return R.drawable.ic_cloudy;
                }
            case 900:
            case 901:
            case 902:
                if (art) {
                    return R.drawable.art_storm;
                } else {
                    return R.drawable.ic_storm;
                }
            case 903:
            case 904:
            case 905:
            case 906:
                /*
                 * extreme
                 */
                break;
            case 950:
            case 951:
            case 952:
            case 953:
            case 954:
            case 955:
            case 956:
            case 957:
            case 958:
            case 959:
                /*
                 * additional
                 */
                break;
            case 960:
                if (art) {
                    return R.drawable.art_storm;
                } else {
                    return R.drawable.ic_storm;
                }
            case 961:
            case 962:
                /*
                 * additional
                 */
                break;
        }

        Log.d(LOG_TAG, "unknown weatherId (" + description + "): " + weatherId);
//        return R.mipmap.ic_launcher;
        if (art) {
            return R.drawable.art_light_clouds;
        } else {
            return R.drawable.ic_light_clouds;
        }
    }

}
