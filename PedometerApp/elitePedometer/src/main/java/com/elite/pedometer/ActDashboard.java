package com.elite.pedometer;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.elite.pedometer.util.DatabaseHelper;
import com.elite.pedometer.util.PreferencesKeys;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;


/**
 * Created by dhaval.mehta on 21-Sep-2017.
 */

public class ActDashboard extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    String TAG = "==ActDashboard==";

    //
    ImageView ivPlayPause;
    TextView tvSendData;
    TextView tvTagCaloriesBurn, tvTagCaloriesBurnUnit,
            tvTagTargetSteps, tvTagTargetStepsUnit,
            tvTagDistance, tvTagDistanceUnit;
    public static TextView tvSteps, tvTargetSteps, tvCaloriesBurn, tvDistance;
    // public static DonutProgress donutProgressView;
    public static ArcProgress arcProgressView;
    TextView tvTagWalkingSratus, tvTagTodaysSteps;

    DatabaseHelper dbHelper;
    ArrayList<StepsDateModel> dbArrayDt = new ArrayList<>();
    String isServiceStart = "false";

    //
    static GoogleApiClient googleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try
        {
            super.onCreate(savedInstanceState);
            App.showLogTAG(TAG);
            ViewGroup.inflate(this, R.layout.act_dashboard_new, llContainerSub);

            dbHelper = new DatabaseHelper(ActDashboard.this);

            initialisation();
            setFonts();
            setClickEvents();

            App.showLog(TAG + "==target==steps==db==" + App.getDatabaseHelper().getTargetStepsDateWise(App.getCurrentDate()));

        } catch (Exception e) {e.printStackTrace();}
    }

    @Override
    protected void onResume() {
        try
        {
            super.onResume();

            if (googleClient == null)
            {
                // Build a new GoogleApiClient that includes the Wearable API
                googleClient = new GoogleApiClient.Builder(this)
                        .addApi(Wearable.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
            }

            if (App.sharePrefrences.getStringPref(PreferencesKeys.isServiceStart) != null
                    && App.sharePrefrences.getStringPref(PreferencesKeys.isServiceStart).length() > 0)
            {
                isServiceStart = App.sharePrefrences.getStringPref(PreferencesKeys.isServiceStart);
            }
            App.showLog(TAG + "==onResume==isServiceStart==" + isServiceStart);

            //
            if (ivPlayPause != null && tvTagWalkingSratus != null)
            {
                if (isServiceStart.equalsIgnoreCase("false"))
                {
                    App.stopPedometerService(ActDashboard.this);
                    ivPlayPause.setImageResource(R.drawable.ic_play);
                    // tvTagWalkingSratus.setText("Walking - Pause/Stops");
                    tvTagWalkingSratus.setText("Walking mode OFF");
                }
                else
                {
                    App.startPedometerService(ActDashboard.this);
                    ivPlayPause.setImageResource(R.drawable.ic_pause);
                    // tvTagWalkingSratus.setText("Walking - Play");
                    tvTagWalkingSratus.setText("Walking mode ON");
                }
            }

            App.showLog(TAG + "==onResume==strTargetSteps==" + App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps));
            App.showLog(TAG + "==onResume==strWeight==" + App.sharePrefrences.getStringPref(PreferencesKeys.strWeight));
            //
            if (App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps) != null &&
                    App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps).length() > 0)
            {
                Spanned text = Html.fromHtml("<b>"+ App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps) + "</b>");
                tvTargetSteps.setText(text);
                // donutProgressView.setMax(Integer.parseInt(App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps)));
                arcProgressView.setMax(Integer.parseInt(App.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps)));
            }

            //
            showStepsDetail();

        } catch (Exception e) {e.printStackTrace();}
    }


    private void initialisation() {
        try
        {
            // Build a new GoogleApiClient that includes the Wearable API
            if (googleClient == null)
            {
                googleClient = new GoogleApiClient.Builder(this)
                        .addApi(Wearable.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
            }

            //
            tvTitle.setText(App.TITLE_DASHBOARD);
            ivHistory.setVisibility(View.VISIBLE);
            ivSetting.setVisibility(View.VISIBLE);

            ivPlayPause = (ImageView) findViewById(R.id.ivPlayPause);
            tvTagWalkingSratus = (TextView) findViewById(R.id.tvTagWalkingSratus);
            tvTagTodaysSteps = (TextView) findViewById(R.id.tvTagTodaysSteps);

            arcProgressView = (ArcProgress) findViewById(R.id.arcProgressView);
            arcProgressView.setArcAngle(270);
            arcProgressView.setStrokeWidth(14);
            arcProgressView.setFinishedStrokeColor(ContextCompat.getColor(this, R.color.clrStatusBar2));
            arcProgressView.setUnfinishedStrokeColor(ContextCompat.getColor(this, R.color.clrMainBg));

//            donutProgressView = (DonutProgress) findViewById(R.id.donutProgressView);
//            donutProgressView.setText("");
//            donutProgressView.setStartingDegree(270);
//            donutProgressView.setUnfinishedStrokeColor(ContextCompat.getColor(this, R.color.clrMainBg));
//            donutProgressView.setUnfinishedStrokeWidth(18);
//            donutProgressView.setFinishedStrokeColor(ContextCompat.getColor(this, R.color.clrStatusBar2));
//            donutProgressView.setFinishedStrokeWidth(20);

            tvSteps = (TextView) findViewById(R.id.tvSteps);
            tvTargetSteps = (TextView) findViewById(R.id.tvTargetSteps);
            tvCaloriesBurn = (TextView) findViewById(R.id.tvCaloriesBurn);
            tvDistance = (TextView) findViewById(R.id.tvDistance);

            tvSendData = (TextView) findViewById(R.id.tvSendData);

            tvTagCaloriesBurn = (TextView) findViewById(R.id.tvTagCaloriesBurn);
            tvTagCaloriesBurnUnit = (TextView) findViewById(R.id.tvTagCaloriesBurnUnit);

            tvTagTargetSteps = (TextView) findViewById(R.id.tvTagTargetSteps);
            tvTagTargetStepsUnit = (TextView) findViewById(R.id.tvTagTargetStepsUnit);

            tvTagDistance = (TextView) findViewById(R.id.tvTagDistance);
            tvTagDistanceUnit = (TextView) findViewById(R.id.tvTagDistanceUnit);

        } catch (Exception e) {e.printStackTrace();}
    }


    private void setFonts() {
        try
        {
            tvTagWalkingSratus.setTypeface(App.tfTribhuchet_ms);
            tvTagTodaysSteps.setTypeface(App.tfTribhuchet_ms);
            tvSteps.setTypeface(App.tfTribhuchet_ms);
            tvTargetSteps.setTypeface(App.tfTribhuchet_ms);
            tvCaloriesBurn.setTypeface(App.tfTribhuchet_ms);
            tvDistance.setTypeface(App.tfTribhuchet_ms);

            tvSendData.setTypeface(App.tfTribhuchet_ms);

            tvTagCaloriesBurn.setTypeface(App.tfTribhuchet_ms);
            tvTagCaloriesBurnUnit.setTypeface(App.tfTribhuchet_ms);

            tvTagTargetSteps.setTypeface(App.tfTribhuchet_ms);
            tvTagTargetStepsUnit.setTypeface(App.tfTribhuchet_ms);

            tvTagDistance.setTypeface(App.tfTribhuchet_ms);
            tvTagDistanceUnit.setTypeface(App.tfTribhuchet_ms);

        } catch (Exception e) {e.printStackTrace();}
    }


    private void setClickEvents() {
        try
        {
            //
            ivHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent iv = new Intent(ActDashboard.this, ActHistory.class);
                    iv.putExtra(App.ITAG_FROM, "ActDashboard");
                    App.myStartActivity(ActDashboard.this, iv);
                }
            });

            //
            ivSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent iv = new Intent(ActDashboard.this, ActSettings.class);
                    iv.putExtra(App.ITAG_FROM, "ActDashboard");
                    App.myStartActivity(ActDashboard.this, iv);
                }
            });

            //
            ivPlayPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (App.sharePrefrences.getStringPref(PreferencesKeys.isServiceStart)
                            .equalsIgnoreCase("true"))
                    {
                        App.stopPedometerService(ActDashboard.this);
                        App.sharePrefrences.setPref(PreferencesKeys.isServiceStart, "false");

                        ivPlayPause.setImageResource(R.drawable.ic_play);
                        // tvTagWalkingSratus.setText("Walking - Pause/Stops");
                        tvTagWalkingSratus.setText("Walking mode OFF");
                    }
                    else
                    {
                        App.startPedometerService(ActDashboard.this);
                        App.sharePrefrences.setPref(PreferencesKeys.isServiceStart, "true");

                        ivPlayPause.setImageResource(R.drawable.ic_pause);
                        // tvTagWalkingSratus.setText("Walking - Play");
                        tvTagWalkingSratus.setText("Walking mode ON");
                    }
                }
            });

            //
            tvSendData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String strMessage = arcProgressView.getProgress()
                            +"##"
                            + tvCaloriesBurn.getText().toString()
                            +"##"
                            + tvTargetSteps.getText().toString();

                    new SendToDataLayerThread("/message_path", strMessage).start();
                }
            });

        } catch (Exception e) {e.printStackTrace();}
    }


    public static class SendToDataLayerThread extends Thread {

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
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(),
                            path,
                            message.getBytes()).await();
                    if (result.getStatus().isSuccess()) {
                        Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
                    } else {
                        // Log an error
                        Log.v("myTag", "ERROR: failed to send Message");
                    }
                }

            } catch (Exception e) {e.printStackTrace();}
        }
    }


    public void showStepsDetail() {
        try
        {
            // New step count read
            ArrayList<StepsDateModel> arrayDt = new ArrayList<>();
            ArrayList<StepsCountModel> array = null;
            arrayDt = (ArrayList<StepsDateModel>) dbHelper.getAllStepsDates();

            if (arrayDt.size() == 0)
            {
                App.showLogTAG(TAG + "==arrayDt DB size is null==Calling simple api==");
            }
            else
            {
                final ArrayList<StepsDateModel> finalArrayDt = arrayDt;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //stuff that updates ui

                        for (int i = 0; i < finalArrayDt.size() ; i++)
                        {
                            StepsDateModel stepsDtModel = new StepsDateModel();
                            stepsDtModel.steps_date = finalArrayDt.get(i).steps_date;
                            App.showLogTAG(TAG + "==fromDB==stepCount==DATE_ONLY==" + "\n"
                                    + i + " ==date==" + stepsDtModel.steps_date);
                            dbArrayDt.add(stepsDtModel); // i.e. 15-09-2017, 16-09-2017 etc

                            long dtWiseCount = App.getDatabaseHelper().getSUMCountStepsDateWise(stepsDtModel.steps_date);
                            App.showLog("==DateWise-totalCount==" + "\n"+
                                    "Date : " + stepsDtModel.steps_date +
                                    " //**// Count : " + dtWiseCount);
                            App.showLog(TAG + "==countSharedPref==" + App.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps));

                            if (App.getCurrentDate().equalsIgnoreCase(stepsDtModel.steps_date))
                            {
                                App.TODAY_STEP_VALUE = (int) dtWiseCount;
                                App.showLogApi(TAG + "==App.TODAY_STEP_VALUE==" + App.TODAY_STEP_VALUE);
                                App.sharePrefrences.setPref(PreferencesKeys.strTodaysSteps, ""+ App.TODAY_STEP_VALUE);

                                if (App.TODAY_STEP_VALUE != 0)
                                {
                                    double calory = 0;
                                    double distance = 0;
                                    for (int k = 0 ; k<App.TODAY_STEP_VALUE ; k++)
                                    {
//                                        if (App.sharePrefrences.getStringPref(PreferencesKeys.isRunning).equalsIgnoreCase(App.TRUE))
//                                        {
//                                            calory +=
//                                                    (Integer.parseInt(App.sharePrefrences.getStringPref(PreferencesKeys.strWeight))
//                                                            * App.METRIC_RUNNING_FACTOR)// Distance:
//                                                            * Integer.parseInt(App.DEFAULT_STEP_LENGTH) // centimeters
//                                                            / 100000.0; // centimeters/kilometer
//                                        }
//                                        else
//                                        {
//                                            calory +=
//                                                    (Integer.parseInt(App.sharePrefrences.getStringPref(PreferencesKeys.strWeight))
//                                                            * App.METRIC_WALKING_FACTOR)// Distance:
//                                                            * Integer.parseInt(App.DEFAULT_STEP_LENGTH) // centimeters
//                                                            / 100000.0; // centimeters/kilometer
//                                        }

                                        distance += (float)(// kilometers
                                                Integer.parseInt(App.DEFAULT_STEP_LENGTH) // centimeters
                                                        / 100000.0); // centimeters/kilometer

                                    }

                                    App.showLog(TAG + "==isRunning==" + App.sharePrefrences.getStringPref(PreferencesKeys.isRunning)
                                            +"\n==getBurnCalories==" + calory);
                                    App.showLog(TAG + "==getDistance==" + distance);

                                    calory = dtWiseCount / App.DEFAULT_STEPS_BY_CALORY;
                                    App.TODAY_CALORIES_BURN = calory;
                                    StepTrackerService.calory = calory;

                                    App.TODAY_DISTANCE = distance;
                                    StepTrackerService.distance = distance;
                                }
                            } else {
                                // strNoOfSteps = "0";
                                // App.TODAY_STEP_VALUE = 0;
                                // setSharedPrefData();
                            }

                        } // for loop over here
                        App.showStepsValuesOnScreen();

                    }
                });
            }

        } catch (Exception e) {e.printStackTrace();}
    }


    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        App.showLog(TAG + "==onStart==");
        if (googleClient != null)
            googleClient.connect();
    }


    // Disconnect from the data layer when the Activity stops
//    @Override
//    protected void onStop() {
//        App.showLog(TAG + "==onStop==");
//        if (null != googleClient && googleClient.isConnected()) {
//            googleClient.disconnect();
//        }
//        super.onStop();
//    }


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



    @Override
    public void onBackPressed() {
        showExitDialog();
    }
}
