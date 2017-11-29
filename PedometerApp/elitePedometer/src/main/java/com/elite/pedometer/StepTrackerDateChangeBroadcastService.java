package com.elite.pedometer;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.Html;

import com.elite.pedometer.util.PreferencesKeys;

/**
 * Created by dhaval.mehta on 22-Sep-2017.
 */

public class StepTrackerDateChangeBroadcastService extends BroadcastReceiver {

    String TAG = "==StepTrackerDateChangeBroadcastService==";

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            App.showLogTAG(TAG + "==ALARM==[[ ** == onReceive == ** ]]==");
            App.showLog(TAG + "==Current24FormatHour==" + App.getCurrent24FormatHour() + "\n==CurrentTime==" + App.getCurrentTime());

            App.TODAY_STEP_VALUE = 0;
            App.TODAY_CALORIES_BURN = 0;
            App.TODAY_DISTANCE = 0.000;


            App.sharePrefrences.setPref(PreferencesKeys.strTodaysSteps, "" + App.TODAY_STEP_VALUE);

            StepTrackerService.notification.flags = Notification.FLAG_ONGOING_EVENT;
            StepTrackerService.builder.setContentTitle(StepTrackerService.strNotificationTodayTitle
                    + App.TODAY_STEP_VALUE + StepTrackerService.strNotificationTodayTitleStepsTag);

            if (App.getDatabaseHelper().getSUMCountStepsDateWise(App.getPreviousDate(App.getCurrentDate())) == 0)
                StepTrackerService.builder.setContentText(StepTrackerService.strNotificationYdayTitleStayActive);
            else
                StepTrackerService.builder.setContentText(StepTrackerService.strNotificationYdayTitle
                        + App.getDatabaseHelper().getSUMCountStepsDateWise(App.getPreviousDate(App.getCurrentDate()))
                        + StepTrackerService.strNotificationTodayTitleStepsTag);

            StepTrackerService.notificationManager.notify(App.NOTIFICATION_ID, StepTrackerService.builder.build());

            /*
            * Insert into DB
            * */
            StepsModel stepsModel = new StepsModel();
            stepsModel.steps_date = App.getCurrentDate();
            stepsModel.steps_count = 0;
            stepsModel.steps_date_time = App.getCurrentDateTime();
            App.getDatabaseHelper().addSteps(stepsModel);
            /////////////////////////


            /*
            * Insert into DB - Target Steps
            * */
            TargetStepsModel targetStepsModel = new TargetStepsModel();
            targetStepsModel.target_steps_date = App.getCurrentDate();
            targetStepsModel.target_steps_count = App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps);
            App.getDatabaseHelper().addTargetStepsCount(targetStepsModel);
            /////////////////////////


            StepTrackerService.calory = App.TODAY_CALORIES_BURN;
            StepTrackerService.distance = App.TODAY_DISTANCE;


            ActDashboard.tvSteps.setText("" + App.TODAY_STEP_VALUE);
//            ActDashboard.donutProgressView.setProgress(App.TODAY_STEP_VALUE);
            ActDashboard.arcProgressView.setProgress(App.TODAY_STEP_VALUE);
            ActDashboard.tvCaloriesBurn.setText(Html.fromHtml("<b>"+ (int) App.TODAY_CALORIES_BURN + "</b>"));
            ActDashboard.tvDistance.setText(Html.fromHtml("<b>"+ String.format("%.3f", App.TODAY_DISTANCE) + "</b>"));

//            if (ActDashboard.tvSteps != null)
//                ActDashboard.tvSteps.setText(""+ App.TODAY_STEP_VALUE);
//
//            if (ActDashboard.donutProgressView != null)
//                ActDashboard.donutProgressView.setProgress(App.TODAY_STEP_VALUE);
//
//            if (ActDashboard.tvCaloriesBurn != null)
//                // ActDashboard.tvCaloriesBurn.setText(Html.fromHtml("Calories Burn - <b>"+ String.format("%.2f", App.TODAY_CALORIES_BURN) + "</b>"));
//                ActDashboard.tvCaloriesBurn.setText(Html.fromHtml("Calories Burn - <b>"+ (int) App.TODAY_CALORIES_BURN + "</b>"));
//
//            if (ActDashboard.tvDistance != null)
//                ActDashboard.tvDistance.setText(Html.fromHtml("Distance - <b>"+ String.format("%.3f", App.TODAY_DISTANCE) + "</b> km"));


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
