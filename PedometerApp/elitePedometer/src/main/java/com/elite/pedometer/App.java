package com.elite.pedometer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.elite.pedometer.util.DatabaseHelper;
import com.elite.pedometer.util.PreferencesKeys;
import com.elite.pedometer.util.SharePrefrences;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by dhaval.mehta on 21-Sep-2017.
 */

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    //
    public static final String ITAG_FROM = "itag_from";
    public static int SPLASH_DELAY_TIME = 2000;
    public static boolean blnFullscreenActvitity = false;
    public static int TODAY_STEP_VALUE = 0;
    public static double TODAY_CALORIES_BURN = 0;
    public static double TODAY_DISTANCE = 0;

    public static int NOTIFICATION_ID = 100;
    public static String APP_FOLDERNAME = ".ElitePedometer";
    public static String APP_SD_CARD_PATH = Environment.getExternalStorageDirectory().toString();
    public static String DB_NAME = "elite_pedometer.db";
    public static String DB_NAME_WITH_PATH =  APP_SD_CARD_PATH
            + File.separator
            + APP_FOLDERNAME
            + File.separator
            + DB_NAME;
    public static String PREF_NAME = "elite_pedometer_app";
    public static String TITLE_DASHBOARD = "Pedometer";
    public static String TITLE_SETTINGS = "Settings";
    public static String TITLE_HISTORY = "History";
    public static String FALSE = "false", TRUE = "true";

//    public static double METRIC_RUNNING_FACTOR = 1.02784823;
//    public static double METRIC_WALKING_FACTOR = 0.708;
    public static int DEFAULT_STEPS_BY_CALORY = 20;
    public static String DEFAULT_TARGET_STEPS = "10000";
    public static String DEFAULT_WEIGHT = "54"; // in kg
    public static String DEFAULT_STEP_LENGTH = "20"; // in cm

    //
    static Typeface tfTribhuchet_ms;
    static DatabaseHelper databaseHelper;
    private SQLiteDatabase sqlDBHelper;
    public static SharePrefrences sharePrefrences;
    static Context mContext;



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            MultiDex.install(this);
            mContext = getApplicationContext();
            sharePrefrences = new SharePrefrences(App.this);
            databaseHelper = new DatabaseHelper(mContext);

            getTribuchet_MS();

            createAppFolder();
            createDBToSdCard();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDBToSdCard() {
        try
        {
            sqlDBHelper = openOrCreateDatabase(DB_NAME_WITH_PATH ,Context.MODE_PRIVATE, null);

        } catch (Exception e) {e.printStackTrace();}
    }


    public static Typeface getTribuchet_MS() {
        tfTribhuchet_ms = Typeface.createFromAsset(mContext.getAssets(), "fonts/trebuchet_ms.ttf");
        return tfTribhuchet_ms;
    }


    public static void startPedometerService(Context ctx) {
        try
        {
            stopPedometerService(ctx);
            App.showLog(TAG + "==[[ ** Pedometer Serivce Starts ** ]]==");

            Intent myIntent = new Intent(ctx, StepTrackerService.class);
            ctx.startService(myIntent);

            //
            setReminder(ctx, StepTrackerDateChangeBroadcastService.class);

        } catch (Exception e) {e.printStackTrace();}
    }


    public static void stopPedometerService(Context ctx) {
        try
        {
            App.showLog(TAG + "==[[ ** Pedometer Serivce STOPS ** ]]==");

            Intent myIntent = new Intent(ctx, StepTrackerService.class);
            ctx.stopService(myIntent);
            //
            cancelReminder(ctx, StepTrackerDateChangeBroadcastService.class);

        } catch (Exception e) {e.printStackTrace();}
    }


    public static void setReminder(Context context,Class<?> cls) {
        App.showLog("==ALARM== ** setReminder ** ==");

        Calendar calendar = Calendar.getInstance();

        Calendar setcalendar = Calendar.getInstance();
        setcalendar.set(Calendar.HOUR_OF_DAY, 23);
        setcalendar.set(Calendar.MINUTE, 59);
        setcalendar.set(Calendar.SECOND, 55);

        // cancel already scheduled reminders
        cancelReminder(context,cls);

        if(setcalendar.before(calendar))
            setcalendar.add(Calendar.DATE,1);

        // Enable a receiver

        ComponentName receiver = new ComponentName(context, cls);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);


        Intent intent1 = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, setcalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        // am.setRepeating(AlarmManager.RTC_WAKEUP, setcalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }


    public static void cancelReminder(Context context,Class<?> cls) {
        // Disable a receiver
        App.showLog("==ALARM== ** cancelReminder ** ==");

        ComponentName receiver = new ComponentName(context, cls);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Intent intent1 = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }


    public static void showStepsValuesOnScreen() {
        try
        {
            if (ActDashboard.tvSteps != null)
                ActDashboard.tvSteps.setText(""+ App.TODAY_STEP_VALUE);

//            if (ActDashboard.donutProgressView != null)
//                ActDashboard.donutProgressView.setProgress(App.TODAY_STEP_VALUE);

            if (ActDashboard.arcProgressView != null)
                ActDashboard.arcProgressView.setProgress(App.TODAY_STEP_VALUE);

            if (ActDashboard.tvCaloriesBurn != null)
                // ActDashboard.tvCaloriesBurn.setText(Html.fromHtml("Calories Burn - <b>"+ String.format("%.2f", App.TODAY_CALORIES_BURN) + "</b>"));
                ActDashboard.tvCaloriesBurn.setText(Html.fromHtml("<b>"+ (int) App.TODAY_CALORIES_BURN + "</b>"));

            if (ActDashboard.tvDistance != null)
                ActDashboard.tvDistance.setText(Html.fromHtml("<b>"+ String.format("%.3f", App.TODAY_DISTANCE) + "</b>"));

//            if (ActDashboard.tvSteps != null && ActDashboard.donutProgressView != null)
//            {
//                ActDashboard.tvSteps.setText(""+ App.TODAY_STEP_VALUE);
//                ActDashboard.donutProgressView.setProgress(App.TODAY_STEP_VALUE);
//            }
        } catch (Exception e) {e.printStackTrace();}
    }


    public static DatabaseHelper getDatabaseHelper()
    {
        return databaseHelper;
    }


    private void createAppFolder() {
        try {
            APP_SD_CARD_PATH = Environment.getExternalStorageDirectory().toString();
            File file2 = new File(APP_SD_CARD_PATH + "/" + APP_FOLDERNAME + "");
            if (!file2.exists()) {
                if (!file2.mkdirs()) {
                    System.out.println("==Create Directory " + App.APP_FOLDERNAME + "====");
                } else {
                    System.out.println("==No--1Create Directory " + App.APP_FOLDERNAME + "====");
                }
            } else {
                System.out.println("== already created---No--2Create Directory " + App.APP_FOLDERNAME + "====");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getddMMMyyyy(String inputDate) {
        String outputDate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:MM:SS");
            Date testDate = null;
            try {
                testDate = sdf.parse(inputDate);
            }catch(Exception ex){
                ex.printStackTrace();
            }
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:mm a");
            // SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy,mm/dd/yyyy hh:mm:ss a");
            outputDate = formatter.format(testDate);
            System.out.println(".....Date...." + outputDate);
        } catch (Exception e) {e.printStackTrace();}

        return outputDate;
    }



    public static String getyyyyMMdd(String inputDate) {
        String outputDate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date testDate = null;
            try {
                testDate = sdf.parse(inputDate);
            }catch(Exception ex){
                ex.printStackTrace();
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            // SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy,mm/dd/yyyy hh:mm:ss a");
            outputDate = formatter.format(testDate);
            System.out.println(".....Date...." + outputDate);
        } catch (Exception e) {e.printStackTrace();}

        return outputDate;
    }



    //1 minute = 60 seconds
    //1 hour = 60 x 60 = 3600
    //1 day = 3600 x 24 = 86400
    // public void printDifference(Date startDate, Date endDate) {
    public static String printDifference(String startDt, String endDt) {
        String daysCount = "";
        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date startDate = null;
            Date endDate = null;
            try
            {
                startDate = dateFormat.parse(startDt);
                endDate = dateFormat.parse(endDt);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            long different = endDate.getTime() - startDate.getTime();

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            long elapsedDays = different / daysInMilli;
            different = different % daysInMilli;

            long elapsedHours = different / hoursInMilli;
            different = different % hoursInMilli;

            long elapsedMinutes = different / minutesInMilli;
            different = different % minutesInMilli;

            long elapsedSeconds = different / secondsInMilli;

            showLog(TAG + "==printDifference==days==" + elapsedDays
                    + "\n==hours==" + elapsedHours
                    + "\n==mins==" + elapsedMinutes
                    + "\n==second==" + elapsedSeconds);
            System.out.printf(
                    "%d days, %d hours, %d minutes, %d seconds%n",
                    elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);

            daysCount = "" + elapsedDays;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return daysCount;
    }


    public static String getDayOfWeek() {
        String weekDay = "";
        try {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.US);

            Calendar calendar = Calendar.getInstance();
            weekDay = dayFormat.format(calendar.getTime());
        } catch (Exception e) {e.printStackTrace();}
        return weekDay;
    }


    public static String getddMMMyy(String convert_date_string) {
        String final_date = "";
        if (convert_date_string != null) {
            try {

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                String inputDateStr = convert_date_string;
                Date dt = null;
                dt = inputFormat.parse(inputDateStr);

                SimpleDateFormat format = new SimpleDateFormat("d");
                String date = format.format(dt);

                if(date.endsWith("1") && !date.endsWith("11"))
                    format = new SimpleDateFormat("d'st' MMMM yyyy");
                else if(date.endsWith("2") && !date.endsWith("12"))
                    format = new SimpleDateFormat("d'nd' MMMM yyyy");
                else if(date.endsWith("3") && !date.endsWith("13"))
                    format = new SimpleDateFormat("d'rd' MMMM yyyy");
                else
                    format = new SimpleDateFormat("d'th' MMMM yyyy");

                final_date = format.format(dt);


            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return final_date;
    }




    public static String getdd_RD_TH_MMM(String convert_date_string) {
        String final_date = "";
        if (convert_date_string != null) {
            try {

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                String inputDateStr = convert_date_string;
                Date dt = null;
                dt = inputFormat.parse(inputDateStr);

                SimpleDateFormat format = new SimpleDateFormat("d");
                String date = format.format(dt);

                if(date.endsWith("1") && !date.endsWith("11"))
                    format = new SimpleDateFormat("d'st' MMM");
                else if(date.endsWith("2") && !date.endsWith("12"))
                    format = new SimpleDateFormat("d'nd' MMM");
                else if(date.endsWith("3") && !date.endsWith("13"))
                    format = new SimpleDateFormat("d'rd' MMM");
                else
                    format = new SimpleDateFormat("d'th' MMM");

                final_date = format.format(dt);


            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return final_date;
    }



    public static String getYYYY(String convert_date_string) {
        String final_date = "";
        if (convert_date_string != null) {
            try {

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                String inputDateStr = convert_date_string;
                Date dt = null;
                dt = inputFormat.parse(inputDateStr);

                SimpleDateFormat format = new SimpleDateFormat("yyyy");
                final_date = format.format(dt);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return final_date;
    }



    public static String getddMMMyyExpiry(String convert_date_string) {
        String final_date = "";
        if (convert_date_string != null) {
            try {

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String inputDateStr = convert_date_string;
                Date dt = null;
                dt = inputFormat.parse(inputDateStr);

                SimpleDateFormat format = new SimpleDateFormat("d");
                String date = format.format(dt);

                if(date.endsWith("1") && !date.endsWith("11"))
                    format = new SimpleDateFormat("d'st' MMMM yyyy hh:mm a");
                else if(date.endsWith("2") && !date.endsWith("12"))
                    format = new SimpleDateFormat("d'nd' MMMM yyyy hh:mm a");
                else if(date.endsWith("3") && !date.endsWith("13"))
                    format = new SimpleDateFormat("d'rd' MMMM yyyy hh:mm a");
                else
                    format = new SimpleDateFormat("d'th' MMMM yyyy hh:mm a");

                final_date = format.format(dt);


            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return final_date;
    }


    public static String getTimeAgo(/*long time,*/ Context ctx, String convert_date_string) {
        String finalDate = "";
        try {

            long timeStamp = 0l;
            if (convert_date_string != null && convert_date_string.length() > 0) {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = (Date)formatter.parse(convert_date_string);
                timeStamp = date.getTime();
                System.out.println("....timeStamp...." + timeStamp);
            }

            if (timeStamp < 1000000000000L) {
                // if timestamp given in seconds, convert to millis
                timeStamp *= 1000;
            }

            long now = System.currentTimeMillis();
            //App.showLog("==now==" + now);
            if (timeStamp > now || timeStamp <= 0) {
                return null;
            }

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String inputDateStr = convert_date_string;
            Date dt = null;
            dt = inputFormat.parse(inputDateStr);

            long diff = new Date().getTime() - timeStamp;
            App.showLog("==diff==" + diff);
            App.showLog("==timeStamp==" + timeStamp);
            double seconds = Math.abs(diff) / 1000;
            double minutes = seconds / 60;
            double hours = minutes / 60;
            double days = hours / 24;
            double years = days / 365;

            App.showLog("==seconds==" + seconds);
            App.showLog("==minutes==" + minutes);
            App.showLog("==hours==" + hours);


            if (seconds > 2 && seconds < 60)
            {
                finalDate =  "Just now";
            }
            else if (seconds > 60 && minutes < 2)
            {
                finalDate = "1 minute ago";
            }
            else if (minutes > 2 && minutes < 60)
            {
                finalDate =  ((int)minutes) + " minutes ago";
            }
            else if (minutes == 60)
            {
                finalDate =  ((int)minutes) + " hour ago";
            }
            else if (minutes > 60 && minutes < 61)
            {
                finalDate =  ((int)minutes) + " hour ago";
            }
            else if (minutes > 61 && hours < 24)
            {
                //App.showLog("==hours==" + hours);
                if (((int)hours) == 1) {
                    finalDate =  ((int)hours) + " hour ago";
                } else {
                    finalDate =  ((int)hours) + " hours ago";
                }
                // finalDate =  ((int)hours) + " hours ago";
            }
            else if (hours > 24 && hours < 36)
            {
                SimpleDateFormat format1 = new SimpleDateFormat("hh:mm a");
                finalDate = format1.format(dt);
                finalDate =  "Yesterday at " + finalDate;
            }
            else
            {
                SimpleDateFormat format1 = new SimpleDateFormat("d");
                String date = format1.format(dt);

                if(date.endsWith("1") && !date.endsWith("11"))
                    format1 = new SimpleDateFormat("d'st' MMMM yyyy, hh:mm a");
                else if(date.endsWith("2") && !date.endsWith("12"))
                    format1 = new SimpleDateFormat("d'nd' MMMM yyyy, hh:mm a");
                else if(date.endsWith("3") && !date.endsWith("13"))
                    format1 = new SimpleDateFormat("d'rd' MMMM yyyy, hh:mm a");
                else
                    format1 = new SimpleDateFormat("d'th' MMMM yyyy, hh:mm a");

                finalDate = format1.format(dt);
                finalDate =  "" + finalDate;
            }

        } catch (Exception e) {e.printStackTrace();}

        return finalDate;
    }


    public static long getTimeStampFromDate (String convertDateString) {
        long timeStamp = 0l;
        try {

            if (convertDateString != null && convertDateString.length() > 0) {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date date = (Date)formatter.parse(convertDateString);
                timeStamp = date.getTime();
                //System.out.println("....timeStamp...." + timeStamp);
            }
        } catch (Exception e) {e.printStackTrace();}

        return timeStamp;
    }


    public static String getCurrentDateTime() {
        String current_date = "";
        try {
            Calendar c = Calendar.getInstance();
            //System.out.println("Current time => " + c.getTime());

            SimpleDateFormat postFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            current_date = postFormater.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return current_date;
    }


    public static String getCurrentTime() {
        String current_time = "";
        try {
            Calendar c = Calendar.getInstance();
            //System.out.println("Current time => " + c.getTime());

            SimpleDateFormat postFormater = new SimpleDateFormat("HH:mm:ss");
            current_time = postFormater.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return current_time;
    }


    public static String getCurrent24FormatHour() {
        String current_hour = "";
        try {
            Calendar c = Calendar.getInstance();
            //System.out.println("Current time => " + c.getTime());

            SimpleDateFormat postFormater = new SimpleDateFormat("HH");
            current_hour = postFormater.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return current_hour;
    }


    public static String getCurrentDate() {
        String current_date = "";
        try {
            Calendar c = Calendar.getInstance();
            //System.out.println("Current time => " + c.getTime());

            SimpleDateFormat postFormater = new SimpleDateFormat("yyyy-MM-dd");
            current_date = postFormater.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return current_date;
    }


    public static String getPreviousDate(String inputDate){
        // inputDate = "15-12-2015"; // for example
        SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            Date date = format.parse(inputDate);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, -1);
            inputDate = format.format(c.getTime());
            // Log.d("asd", "selected date : "+inputDate);
            // System.out.println(date);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            inputDate ="";
        }
        return inputDate;
    }


    public static void showToastLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }


    public static void showToastShort(Context context, String strMessage) {
        Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
    }



    public static void showLog(String strMessage) {
        Log.v("==MSG==", "--strMessage--" + strMessage);
    }

    public static void showLogTAG(String strMessage) {
        Log.e("==TAG==", "--screen--" + strMessage);
    }

    public static void showLogResponce(String strTag, String strMessage) {
        Log.w("==RESPONSE==" + strTag, "--strResponse--" + strMessage);
    }


    public static void showLogApi(String strMessage) {
        //Log.v("==App==", "--strMessage--" + strMessage);
        System.out.println("--API-MESSAGE--" + strMessage);
    }


    public static void showLog(String strTag, String strMessage) {
        Log.v("==App==strTag==" + strTag, "--strMessage--" + strMessage);
    }


    public static boolean isInternetAvail(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }


    public static boolean isGPSAvailable(Context context) {
        boolean gps_enabled = false;

        try {

            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(!gps_enabled) {
                App.showLog("==APP==" + "==0000==GPS is not enalble==false==");
            } else {
                App.showLog("==APP==" + "==1111==GPS enable==true==");
            }
        } catch (Exception e) {e.printStackTrace();}

        return gps_enabled;
    }


    public void hideKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static void hideSoftKeyboardMy(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


    public static void myStartActivity(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    public static void myFinishActivityRefresh(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    public static void myFinishActivity(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    public static void myFinishStartActivity(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }


    public static String convertTo24Hour(String Time) {
        DateFormat f1 = new SimpleDateFormat("hh:mm a"); //11:00 pm
        Date d = null;
        try {
            d = f1.parse(Time);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DateFormat f2 = new SimpleDateFormat("HH:mm:ss");
        String x = f2.format(d); // "23:00"

        return x;
    }


    public static String convertTo12Hour(String Time) {
        DateFormat f1 = new SimpleDateFormat("HH:mm:ss"); // "23:00:00"
        Date d = null;
        try {
            d = f1.parse(Time);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DateFormat f2 = new SimpleDateFormat("hh:mm a");
        String x = f2.format(d); //11:00 pm

        return x;
    }

}
