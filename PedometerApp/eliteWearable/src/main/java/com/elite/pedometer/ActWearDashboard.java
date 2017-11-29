package com.elite.pedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.elite.pedometer.R;
import com.example.android.wearable.datalayer.MainActivity;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.android.wearable.datalayer.DataLayerListenerService.LOGD;

/**
 * Created by dhaval.mehta on 03-Oct-2017.
 */

public class ActWearDashboard extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        MessageApi.MessageListener,
        CapabilityApi.CapabilityListener
    {

    String TAG = "==ActWearDashboard==";

    //
    TextView tvTagTodaySteps, tvTodaySteps, tvTagCalories, tvCalories, tvTagTargetSteps, tvTargetSteps;
    ArcProgress arcProgressView;

    //
    String strTodysSteps = "0", strTargetSteps = "0", strCaloriesBurned = "0";

    private GoogleApiClient mGoogleApiClient;
    private static final String CONNECTION_STATUS_CAPABILITY_NAME = "is_connection_lost";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.act_wear_dashboard_new);
            AppWear.showLogTAG(TAG);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            setAmbientEnabled();
            initialisation();
            setFonts();

            // Register the local broadcast receiver
            IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
            MessageReceiver messageReceiver = new MessageReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        } catch (Exception e) {e.printStackTrace();}
    }


    private void initialisation() {
        try
        {
            arcProgressView = (ArcProgress) findViewById(R.id.arcProgressView);
            arcProgressView.setArcAngle(270);
            arcProgressView.setStrokeWidth(6);
            arcProgressView.setFinishedStrokeColor(ContextCompat.getColor(this, R.color.clrStatusBar2));
            arcProgressView.setUnfinishedStrokeColor(ContextCompat.getColor(this, R.color.clrMainBg));

            // tvTagTodaySteps = (TextView) findViewById(R.id.tvTagTodaySteps);
            tvTodaySteps = (TextView) findViewById(R.id.tvTodaySteps);

            tvTagCalories = (TextView) findViewById(R.id.tvTagCalories);
            tvCalories = (TextView) findViewById(R.id.tvCalories);

            tvTagTargetSteps = (TextView) findViewById(R.id.tvTagTargetSteps);
            tvTargetSteps = (TextView) findViewById(R.id.tvTargetSteps);


        } catch (Exception e) {e.printStackTrace();}
    }


    @Override
    protected void onResume() {
        try
        {
            super.onResume();

            //
            if (AppWear.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps).equalsIgnoreCase("")) {
                AppWear.sharePrefrences.setPref(PreferencesKeys.strTargetSteps, "0");
            }
            //
            if (AppWear.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps).equalsIgnoreCase("")) {
                AppWear.sharePrefrences.setPref(PreferencesKeys.strTodaysSteps, "0");
            }
            //
            if (AppWear.sharePrefrences.getStringPref(PreferencesKeys.strCalories).equalsIgnoreCase("")) {
                AppWear.sharePrefrences.setPref(PreferencesKeys.strCalories, "0");
            }
            //
            if (AppWear.sharePrefrences.getStringPref(PreferencesKeys.strDistance).equalsIgnoreCase("")) {
                AppWear.sharePrefrences.setPref(PreferencesKeys.strDistance, "0");
            }

            ///////////////////////////////


            if (tvTodaySteps != null)
            {
                tvTodaySteps.setText(AppWear.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps));

            }

            if (arcProgressView != null)
            {
                arcProgressView.setMax(Integer.parseInt(AppWear.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps)));
                arcProgressView.setProgress(Integer.parseInt(AppWear.sharePrefrences.getStringPref(PreferencesKeys.strTodaysSteps)));
            }

            if (tvCalories != null)
            {
                tvCalories.setText(AppWear.sharePrefrences.getStringPref(PreferencesKeys.strCalories));
            }

            if (tvTargetSteps != null)
            {
                tvTargetSteps.setText(AppWear.sharePrefrences.getStringPref(PreferencesKeys.strTargetSteps));
            }

        } catch (Exception e) {e.printStackTrace();}
    }


    private void setFonts() {
        try
        {
            // tvTagTodaySteps.setTypeface(AppWear.getTribuchet_MS());
            tvTodaySteps.setTypeface(AppWear.getTribuchet_MS());
            tvTagCalories.setTypeface(AppWear.getTribuchet_MS());
            tvCalories.setTypeface(AppWear.getTribuchet_MS());
//            tvTagDistance.setTypeface(AppWear.getTribuchet_MS());
//            tvDistance.setTypeface(AppWear.getTribuchet_MS());
            tvTagTargetSteps.setTypeface(AppWear.getTribuchet_MS());
            tvTargetSteps.setTypeface(AppWear.getTribuchet_MS());

        } catch (Exception e) {e.printStackTrace();}
    }


    private void showNodes(final String... capabilityNames) {

        PendingResult<CapabilityApi.GetAllCapabilitiesResult> pendingCapabilityResult =
                Wearable.CapabilityApi.getAllCapabilities(
                        mGoogleApiClient,
                        CapabilityApi.FILTER_REACHABLE);

        pendingCapabilityResult.setResultCallback(
                new ResultCallback<CapabilityApi.GetAllCapabilitiesResult>() {
                    @Override
                    public void onResult(
                            CapabilityApi.GetAllCapabilitiesResult getAllCapabilitiesResult) {

                        if (!getAllCapabilitiesResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to get capabilities");
                            return;
                        }

                        Map<String, CapabilityInfo> capabilitiesMap =
                                getAllCapabilitiesResult.getAllCapabilities();
                        Set<Node> nodes = new HashSet<>();

                        if (capabilitiesMap.isEmpty()) {
                            showDiscoveredNodes(nodes);
                            return;
                        }
                        for (String capabilityName : capabilityNames) {
                            CapabilityInfo capabilityInfo = capabilitiesMap.get(capabilityName);
                            if (capabilityInfo != null) {
                                nodes.addAll(capabilityInfo.getNodes());
                            }
                        }
                        showDiscoveredNodes(nodes);
                    }

                    private void showDiscoveredNodes(Set<Node> nodes) {
                        List<String> nodesList = new ArrayList<>();
                        for (Node node : nodes) {
                            nodesList.add(node.getDisplayName());
                        }
                        LOGD(TAG, "Connected Nodes: " + (nodesList.isEmpty()
                                ? "No connected device was found for the given capabilities"
                                : TextUtils.join(",", nodesList)));
                        String msg;
                        if (!nodesList.isEmpty()) {
                            msg = getString(R.string.connected_nodes,
                                    TextUtils.join(", ", nodesList));
                        } else {
                            msg = getString(R.string.no_device);
                        }
                        Toast.makeText(ActWearDashboard.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }


        @Override
        public void onConnected(Bundle bundle) {
            AppWear.showLog(TAG, "onConnected(): Successfully connected to Google API client");
            Wearable.CapabilityApi.addListener(
                    mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);

            showNodes("capability_2");
        }

        @Override
        public void onConnectionSuspended(int i) {
            AppWear.showLog(TAG, "onConnectionSuspended()");
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            AppWear.showLog(TAG, "onConnectionFailed()");
        }

        @Override
        public void onCapabilityChanged(CapabilityInfo capabilityInfo) {

        }

        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {

        }

        @Override
        public void onMessageReceived(MessageEvent messageEvent) {

        }


        public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try
            {
                String message_original = intent.getStringExtra("message");
                List<String> msgList = Arrays.asList(message_original.split("##"));

                if (msgList.get(0) != null && msgList.get(0).length() > 0)
                {
                    strTodysSteps = msgList.get(0);
                    AppWear.sharePrefrences.setPref(PreferencesKeys.strTodaysSteps, strTodysSteps);
                    // AppWear.showLog(TAG + "** strTodysSteps **" + strTodysSteps);
                }

                if (msgList.get(1) != null && msgList.get(1).length() > 0)
                {
                    strCaloriesBurned = msgList.get(1);
                    AppWear.sharePrefrences.setPref(PreferencesKeys.strCalories, strCaloriesBurned);
                    // AppWear.showLog(TAG + "** strCaloriesBurned **" + strCaloriesBurned);
                }


                if (msgList.get(2) != null && msgList.get(2).length() > 0)
                {
                    strTargetSteps = msgList.get(2);
                    AppWear.sharePrefrences.setPref(PreferencesKeys.strTargetSteps, strTargetSteps);
                    // AppWear.showLog(TAG + "** strTargetSteps **" + strTargetSteps);
                }

                tvTodaySteps.setText(strTodysSteps);
                tvCalories.setText(strCaloriesBurned);
                arcProgressView.setProgress(Integer.parseInt(strTodysSteps));
                tvTargetSteps.setText(strTargetSteps);

            } catch (Exception e) {e.printStackTrace();}

        }
    }


    private void updateStatus() {
        try
        {
            List<Node> connectedNodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();

            for (int i = 0 ; i < connectedNodes.size() ; i++)
            {
                AppWear.showLog(TAG +"==updateStatus==" + connectedNodes.get(i).getDisplayName());
            }

            AppWear.showLog(TAG +"==updateStatus==");
            Wearable.CapabilityApi.getCapability(
                    mGoogleApiClient, CONNECTION_STATUS_CAPABILITY_NAME,
                    CapabilityApi.FILTER_REACHABLE).setResultCallback(
                    new ResultCallback<CapabilityApi.GetCapabilityResult>() {
                        @Override
                        public void onResult(CapabilityApi.GetCapabilityResult result) {
                            if (result.getStatus().isSuccess())
                            {
                                AppWear.showLog(TAG +"==updateStatus==result==success==");
                                updateConnectionCapability(result.getCapability());
                            }
                            else
                            {
                                AppWear.showLog(TAG +"==updateStatus==result==FAIL==");
                                AppWear.showLogTAG(TAG + "Failed to get capabilities, " + "status: " + result.getStatus().getStatusMessage());
                            }
                        }
                    });
        } catch (Exception e) {e.printStackTrace();}
    }

    private void updateConnectionCapability(CapabilityInfo capabilityInfo) {
        try
        {
            AppWear.showLog(TAG +"==updateConnectionCapability==");
            Set<Node> connectedNodes = capabilityInfo.getNodes();
            if (connectedNodes.isEmpty()) {
                // The connection is lost !
                AppWear.showLog(TAG +"==updateConnectionCapability==node==empty==");
            } else {
                for (Node node : connectedNodes) {
                    if (node.isNearby()) {
                        // The connection is OK !
                        AppWear.showLog(TAG +"==updateConnectionCapability==node==");
                        AppWear.showLog(TAG + "==nodeName==" + node.getDisplayName());
                    }
                }
            }
        } catch (Exception e) {e.printStackTrace();}
    }
}
