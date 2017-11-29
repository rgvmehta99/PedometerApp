package com.elite.pedometer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.elite.pedometer.util.PreferencesKeys;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Created by dhaval.mehta on 22-Sep-2017.
 */

public class StepTrackerService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    String TAG = "==StepTrackerService==";

    private SensorManager mSensorManager;
    private StepTrackerDetector mShakeDetector;

    private Sensor step_counter_sensor;
    private Sensor step_detector_sensor;
    private Sensor step_accelerometer;
    public static int mTempIndividualSteps;
    ArrayList<StepsDateModel> dbArrayDt = new ArrayList<>();

    public static double calory = 0;
    public static double distance = 0;
    //
    GoogleApiClient googleClient;
    // asyncSendSteps asynTask;

    public static NotificationCompat.Builder builder;
    public static  Notification notification;
    public static  NotificationManager notificationManager;

    public static String strNotificationTodayTitle = "Today: ";
    public static String strNotificationTodayTitleStepsTag = " steps";
    public static String strNotificationYdayTitle = "Yesterday: ";
    public static String strNotificationYdayTitleStayActive = "Stay active!";

    @Override
    public IBinder onBind(Intent intent) {
        App.showLog(TAG + "[[==onBind==]]");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        App.showLog(TAG + "[[==onStartCommand==]]");

        // asynTask = new asyncSendSteps();

        // Build a new GoogleApiClient that includes the Wearable API
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

//        if (googleClient == null && googleClient.isConnected() == false)
//        {
//            // Build a new GoogleApiClient that includes the Wearable API
//            googleClient = new GoogleApiClient.Builder(this)
//                    .addApi(Wearable.API)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .build();
//        }

        // After kill app, remain the total count of today's steps in Notification bar
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        builder.setContentTitle(strNotificationTodayTitle
                + App.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps)
                + strNotificationTodayTitleStepsTag);
        if (App.getDatabaseHelper().getSUMCountStepsDateWise(App.getPreviousDate(App.getCurrentDate())) == 0)
            builder.setContentText(strNotificationYdayTitleStayActive);
        else
            builder.setContentText(strNotificationYdayTitle
                    + App.getDatabaseHelper().getSUMCountStepsDateWise(App.getPreviousDate(App.getCurrentDate()))
                    + strNotificationTodayTitleStepsTag);

        notificationManager.notify(App.NOTIFICATION_ID, builder.build());

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        // super.onCreate();
        try
        {
            App.showLog(TAG + "==onCreate==[[**//**==Step Service Starts..==**//**]]==");

            // asynTask = new asyncSendSteps();
            // Build a new GoogleApiClient that includes the Wearable API
            googleClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            String temp = String.valueOf(App.TODAY_STEP_VALUE);
            if (temp == null && temp.equalsIgnoreCase(""))
                App.TODAY_STEP_VALUE = 0;
            App.showLog("==temp==" + temp);

            Intent notificationIntent = new Intent(getApplicationContext(), ActDashboard.class);
            PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0,
                    notificationIntent, 0);


            notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            builder = new NotificationCompat.Builder(getApplicationContext());
            notification = builder.setContentTitle(strNotificationTodayTitle
                    + temp
                    + strNotificationTodayTitleStepsTag)
                .setAutoCancel(false)
                .setWhen(0) // Hide time in notification bar
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_noti)
                .build();

            // For background color of notification icon - According to screen
            boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
            if (useWhiteIcon == false) {
                builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.clrMainBg));
            } else {
                builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.clrStatusBar2));
            }


            if (App.getDatabaseHelper().getSUMCountStepsDateWise(App.getPreviousDate(App.getCurrentDate())) == 0)
                builder.setContentText(strNotificationYdayTitleStayActive);
            else
                builder.setContentText(strNotificationYdayTitle
                        + App.getDatabaseHelper().getSUMCountStepsDateWise(App.getPreviousDate(App.getCurrentDate()))
                        + strNotificationTodayTitleStepsTag);
            builder.setContentIntent(intent);
            notificationManager.notify(App.NOTIFICATION_ID, notification);

            registerDetector();

        } catch (Exception e) {e.printStackTrace();}
    }


    private void registerDetector() {

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mShakeDetector = new StepTrackerDetector();

        step_counter_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        step_detector_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        step_accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (step_counter_sensor == null) // sensor not supported
        {
            App.showLogTAG(TAG + "==TYPE_STEP_COUNTER is not found in your device==");
            // App.showToastShort(getApplicationContext(), "TYPE_STEP_COUNTER is not found in your device");

            if (step_detector_sensor == null) // sensor not supported
            {
                App.showLogTAG(TAG + "==TYPE_STEP_DETECTOR is not found in your device==");
                // App.showToastShort(getApplicationContext(), "TYPE_STEP_DETECTOR is not found in your device");
            }
            else
            {
                mSensorManager.registerListener(mShakeDetector, step_detector_sensor, Sensor.REPORTING_MODE_ON_CHANGE);
            }
        }
        else
        {
            mSensorManager.registerListener(mShakeDetector, step_counter_sensor, Sensor.REPORTING_MODE_ON_CHANGE);
        }


//        if (step_detector_sensor == null) // sensor not supported
//        {
//            App.showLogTAG(TAG + "==TYPE_STEP_DETECTOR is not found in your device==");
//            // App.showToastShort(getApplicationContext(), "TYPE_STEP_DETECTOR is not found in your device");
//        }
//        else
//        {
//            mSensorManager.registerListener(mShakeDetector, step_detector_sensor, Sensor.REPORTING_MODE_ON_CHANGE);
//        }


        //
        if (step_counter_sensor == null && step_detector_sensor == null)
        {
            App.showLogTAG(TAG + "==TYPE_ACCELEROMETER register==");
            mSensorManager.registerListener(mShakeDetector, step_accelerometer, Sensor.REPORTING_MODE_ON_CHANGE);
        }


        mShakeDetector.setOnShakeListener(new StepTrackerDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */

                int mStepValue = 0;

                // New step count read
                ArrayList<StepsDateModel> arrayDt = new ArrayList<>();
                ArrayList<StepsCountModel> array = null;

                arrayDt = (ArrayList<StepsDateModel>) App.getDatabaseHelper().getAllStepsDates();
                if (arrayDt.size() == 0)
                {
                    mStepValue = mTempIndividualSteps + 1;
                }
                else
                {
                    if (App.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps).equalsIgnoreCase(""))
                        App.sharePrefrences.setPref(PreferencesKeys.strTodaysSteps, ""+1);

                    App.showLog("==0000==sharePrefCount==" + App.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps));
                    App.sharePrefrences.setPref(PreferencesKeys.strTodaysSteps, ""+(Integer.parseInt(App.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps)) + 1));

                    for (int i = 0; i < arrayDt.size() ; i++)
                    {
                        StepsDateModel stepsDtModel = new StepsDateModel();
                        stepsDtModel.steps_date = arrayDt.get(i).steps_date;
                        App.showLogTAG(TAG + "==0000==fromDB==stepCount==DATE_ONLY==" + "\n"
                                + i + " ==date==" + stepsDtModel.steps_date);
                        dbArrayDt.add(stepsDtModel); // i.e. 15-09-2017, 16-09-2017 etc

                        if (App.getCurrentDate().equalsIgnoreCase(stepsDtModel.steps_date))
                        {
                            long dtWiseCount = App.getDatabaseHelper().getSUMCountStepsDateWise(stepsDtModel.steps_date);
                            App.showLog("==0000==DateWise-totalCount==" + "\n"+
                                    "Date : " + stepsDtModel.steps_date +
                                    " //**// Count : " + dtWiseCount);
                            int stepCount = (int) dtWiseCount;

                            stepCount = stepCount + 1;
                            App.showLog("==0000==stepCount==" + stepCount);

                            calory = dtWiseCount / App.DEFAULT_STEPS_BY_CALORY;

                            App.showLog("==getBurnCalories==" + calory/* +
                                    "\n==Weight==" + App.sharePrefrences.getStringPref(PreferencesKeys.strWeight)*/);

                            distance += (float)(// kilometers
                                    Integer.parseInt(App.DEFAULT_STEP_LENGTH) // centimeters
                                            / 100000.0); // centimeters/kilometer
                            App.showLog("==getDistance==" + distance);

                            mStepValue = mTempIndividualSteps + stepCount;
                            App.showLog("==0000==mStepValue==" + mStepValue);
                        }
                    }
                }


//                App.showToastShort(getApplicationContext(), "Steps ::" + App.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps)
//                + "\nDB Steps ::" + (App.getDatabaseHelper().getSUMCountStepsDateWise(App.getCurrentDate()) + 1));

                App.TODAY_STEP_VALUE = mStepValue;
                App.TODAY_CALORIES_BURN = calory;
                App.TODAY_DISTANCE = distance;


                String strMessage = App.TODAY_STEP_VALUE // 0
                        +"##"
                        + ""+ (int) App.TODAY_CALORIES_BURN // 1
                        +"##"
//                        + ""+ App.TODAY_DISTANCE // 2
//                        +"##"
                        + App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps); // 2

                // new asyncSendSteps().execute("/message_path", strMessage);
                // asyncSendSteps asynTask = new asyncSendSteps();
                // asynTask.execute("/message_path", strMessage);

                new SendToDataLayerThread("/message_path", strMessage).start();

                //
                notification.flags = Notification.FLAG_ONGOING_EVENT;
                builder.setContentTitle(strNotificationTodayTitle + App.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps) + strNotificationTodayTitleStepsTag);

                if (App.getDatabaseHelper().getSUMCountStepsDateWise(App.getPreviousDate(App.getCurrentDate())) == 0)
                    builder.setContentText(strNotificationYdayTitleStayActive);
                else
                    builder.setContentText(strNotificationYdayTitle
                            + App.getDatabaseHelper().getSUMCountStepsDateWise(App.getPreviousDate(App.getCurrentDate()))
                            + strNotificationTodayTitleStepsTag);
                notificationManager.notify(App.NOTIFICATION_ID, builder.build());

                //
                App.showStepsValuesOnScreen();

                App.showLog("====insert to database==");
                /*
                * Insert into DB
                * */
                StepsModel stepsModel = new StepsModel();
                stepsModel.steps_date = App.getCurrentDate();
                stepsModel.steps_count = 1;
                stepsModel.steps_date_time = App.getCurrentDateTime();
                App.getDatabaseHelper().addSteps(stepsModel);
                ///////////////////////////
            }
        });
    }


    @Override
    public void onConnected(Bundle bundle) {
        App.showLog(TAG + "==onConnected==");
    }


    @Override
    public void onConnectionSuspended(int i) {
        App.showLog(TAG + "==onConnectionSuspended==");
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        App.showLog(TAG + "==onConnectionFailed==");
    }



    class asyncSendSteps extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try
            {
                // does the hard work
                App.showLog("==asyncSendSteps==" + params[0] + " ** "  + params[1]);

                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(),
                            params[0],
                            params[1].getBytes()).await();
                    if (result.getStatus().isSuccess()) {
                        cancel(true);
                        App.showLog("myTag", "Message: {" + params[1] + "} sent to: " + node.getDisplayName());
                    } else {
                        // Log an error
                        App.showLog("myTag", "ERROR: failed to send Message");
                    }
                }

            } catch (Exception e) {e.printStackTrace();}

            return "Executed";
        }
    }


    class SendToDataLayerThread extends Thread {

        String path;
        String message;

        // Constructor to send a message to the data layer
        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            try
            {
                ConnectionResult connectionResult =
                        googleClient.blockingConnect(2, TimeUnit.SECONDS);

                if (!connectionResult.isSuccess()) {
                    App.showLogApi(TAG + "==GoogleClient CONNECTED==");
                } else {
                    App.showLogApi(TAG + "==GoogleClient NOT CONNECTED==");
                }

                App.showLog(TAG + "==SendToDataLayerThread==");
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(),
                            path,
                            message.getBytes()).await();
                    if (result.getStatus().isSuccess()) {
                        App.showLog(TAG + "==0000==SendToDataLayerThread==");
                        App.showLog("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
                    } else {
                        // Log an error
                        App.showLog("myTag", "ERROR: failed to send Message");
                    }
                }

            } catch (Exception e) {e.printStackTrace();}
        }
    }

    @Override
    public ComponentName startForegroundService(Intent service) {

        return super.startForegroundService(service);
    }

    private void unregisterDetector() {

        // Working
//        notification.flags = Notification.FLAG_AUTO_CANCEL;
//        builder.setAutoCancel(true);
//        builder.setOngoing(false);
//        builder.setContentText(strNotificationTitle + App.TODAY_STEP_VALUE);
//        notificationManager.notify(App.NOTIFICATION_ID, builder.build());
        notificationManager.cancelAll();

        mSensorManager.unregisterListener(mShakeDetector);
    }


    @Override
    public void onDestroy() {
        App.showLog(TAG + "[[==onDestroy==]]");
        unregisterDetector();
        super.onDestroy();
    }
}
